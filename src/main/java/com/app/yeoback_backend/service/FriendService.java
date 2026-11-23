package com.app.yeoback_backend.service;

import com.app.yeoback_backend.common.enumeration.FriendState;
import com.app.yeoback_backend.common.enumeration.RelStatus;
import com.app.yeoback_backend.common.enumeration.Status;
import com.app.yeoback_backend.common.exception.ApiException;
import com.app.yeoback_backend.domain.MemberRelVO;
import com.app.yeoback_backend.dto.FriendDTO;
import com.app.yeoback_backend.repository.MemberDAO;
import com.app.yeoback_backend.repository.MemberRelDAO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 친구 신청 · 수락 · 삭제와 목록
 *  - 두 회원 사이의 관계는 tbl_member_rel 한 행 (pending: 신청 중, done: 친구)
 *  - 상대가 이미 나에게 신청한 상태에서 내가 신청하면 바로 친구가 된다 (서로 신청 = 수락)
 *  - 거절 · 취소 · 친구 끊기는 행을 지운다 (다시 신청할 수 있다)
 *  - 동시에 들어온 요청은 uq_member_rel_pair 인덱스가 막고, 그때는 409 로 돌려준다
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FriendService {
    private final MemberRelDAO memberRelDAO;
    private final MemberDAO memberDAO;

    /** 이 회원과 나의 관계 */
    public FriendDTO getState(Long me, Long other) {
        checkTarget(me, other);
        return FriendDTO.state(other, stateOf(me, memberRelDAO.findBetween(me, other)));
    }

    /** 친구 신청. 상대가 이미 나에게 신청했으면 바로 친구 */
    @Transactional
    public FriendDTO request(Long me, Long other) {
        checkTarget(me, other);
        Optional<MemberRelVO> rel = memberRelDAO.findBetween(me, other);
        switch (stateOf(me, rel)) {
            case NONE -> {
                try {
                    memberRelDAO.saveRequest(me, other);
                } catch (DuplicateKeyException e) {
                    // 조회와 저장 사이에 같은 쌍이 먼저 저장됨 (더블클릭 · 서로 동시 신청)
                    throw new ApiException(HttpStatus.CONFLICT, "conflict", "방금 친구 관계가 바뀌었어요. 다시 확인해 주세요.");
                }
                return FriendDTO.state(other, FriendState.REQUESTED);
            }
            case RECEIVED -> {
                memberRelDAO.accept(rel.orElseThrow().getId());
                return FriendDTO.state(other, FriendState.FRIEND);
            }
            case REQUESTED -> throw new ApiException(HttpStatus.CONFLICT, "already_requested", "이미 친구 신청을 보냈어요.");
            default -> throw new ApiException(HttpStatus.CONFLICT, "already_friend", "이미 친구예요.");
        }
    }

    /** 받은 신청 수락 */
    @Transactional
    public FriendDTO accept(Long me, Long other) {
        checkTarget(me, other);
        Optional<MemberRelVO> rel = memberRelDAO.findBetween(me, other);
        switch (stateOf(me, rel)) {
            case RECEIVED -> {
                memberRelDAO.accept(rel.orElseThrow().getId());
                return FriendDTO.state(other, FriendState.FRIEND);
            }
            case FRIEND -> throw new ApiException(HttpStatus.CONFLICT, "already_friend", "이미 친구예요.");
            case REQUESTED -> throw new ApiException(HttpStatus.CONFLICT, "own_request", "내가 보낸 신청은 상대가 수락해야 해요.");
            default -> throw new ApiException(HttpStatus.NOT_FOUND, "no_request", "받은 친구 신청이 없어요.");
        }
    }

    /** 신청 취소 · 받은 신청 거절 · 친구 끊기 (관계가 없으면 그대로) */
    @Transactional
    public FriendDTO remove(Long me, Long other) {
        checkTarget(me, other);
        memberRelDAO.findBetween(me, other).ifPresent(r -> memberRelDAO.delete(r.getId()));
        return FriendDTO.state(other, FriendState.NONE);
    }

    /** 내 친구 목록 */
    public List<FriendDTO> getFriends(Long me) {
        return withState(memberRelDAO.findFriends(me), FriendState.FRIEND);
    }

    /** 받은 신청(received, 기본) · 보낸 신청(sent) 목록 */
    public List<FriendDTO> getRequests(Long me, String box) {
        if ("sent".equals(box)) return withState(memberRelDAO.findSent(me), FriendState.REQUESTED);
        if (box == null || "received".equals(box)) return withState(memberRelDAO.findReceived(me), FriendState.RECEIVED);
        throw new ApiException(HttpStatus.BAD_REQUEST, "invalid", "box 는 received 또는 sent 예요.");
    }

    private static final int SEARCH_LIMIT = 20;
    private static final int KEYWORD_MAX = 12;

    /**
     * 닉네임으로 회원 찾기 (친구 목록 화면의 검색)
     *  - 닉네임에 검색어가 들어간 활동 중인 회원 (나 제외), 최대 20명
     *  - 실명 · 연락처 · 이메일로는 찾지 않는다 (공개 범위를 지키기 위해)
     *  - 결과마다 나와의 관계(state)를 함께 준다
     */
    public List<FriendDTO> search(Long me, String keyword) {
        String k = keyword == null ? "" : keyword.trim();
        if (k.isEmpty()) return List.of();
        if (k.length() > KEYWORD_MAX) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "invalid", "검색어는 " + KEYWORD_MAX + "자 이내로 적어 주세요.");
        }
        // LIKE 특수문자(\ % _)는 글자 그대로 찾도록 이스케이프
        String escaped = k.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
        return memberRelDAO.searchByAlias(me, k, "%" + escaped + "%", SEARCH_LIMIT);
    }

    /** 받은 신청 수 (알림 표시) */
    public Map<String, Integer> countReceived(Long me) {
        return Map.of("count", memberRelDAO.countReceived(me));
    }

    /** 친구인지 (프로필 공개 범위 '친구만' 판단) */
    public boolean areFriends(Long me, Long other) {
        return !me.equals(other) && memberRelDAO.areFriends(me, other);
    }

    private FriendState stateOf(Long me, Optional<MemberRelVO> rel) {
        if (rel.isEmpty()) return FriendState.NONE;
        MemberRelVO r = rel.get();
        if (r.getRelStatus() == RelStatus.DONE) return FriendState.FRIEND;
        return me.equals(r.getRelRequesterId()) ? FriendState.REQUESTED : FriendState.RECEIVED;
    }

    /** 나 자신은 안 되고, 상대는 활동 중인 회원이어야 한다 */
    private void checkTarget(Long me, Long other) {
        if (me.equals(other)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "self", "나 자신과는 친구를 맺을 수 없어요.");
        }
        memberDAO.findById(other)
                .filter(m -> m.getMemberStatus() == Status.ACTIVE)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "not_found", "회원을 찾지 못했어요."));
    }

    private List<FriendDTO> withState(List<FriendDTO> list, FriendState state) {
        list.forEach(f -> f.setState(state));
        return list;
    }
}

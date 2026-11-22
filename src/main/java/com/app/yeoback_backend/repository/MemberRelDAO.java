package com.app.yeoback_backend.repository;

import com.app.yeoback_backend.common.enumeration.RelStatus;
import com.app.yeoback_backend.domain.MemberRelVO;
import com.app.yeoback_backend.dto.FriendDTO;
import com.app.yeoback_backend.mapper.MemberRelMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MemberRelDAO {
    private final MemberRelMapper memberRelMapper;

    // 두 회원 사이의 관계
    public Optional<MemberRelVO> findBetween(Long me, Long other) {
        return memberRelMapper.selectBetween(me, other);
    }
    // 친구 신청
    public void saveRequest(Long requesterId, Long receiverId) {
        memberRelMapper.insert(requesterId, receiverId);
    }
    // 수락
    public void accept(Long relId) {
        memberRelMapper.updateStatus(relId, RelStatus.DONE);
    }
    // 삭제 (취소 · 거절 · 친구 끊기)
    public void delete(Long relId) {
        memberRelMapper.delete(relId);
    }
    // 친구인지
    public boolean areFriends(Long me, Long other) {
        return findBetween(me, other).map(r -> r.getRelStatus() == RelStatus.DONE).orElse(false);
    }
    // 목록
    public List<FriendDTO> findFriends(Long me) {
        return memberRelMapper.selectFriends(me);
    }
    public List<FriendDTO> findReceived(Long me) {
        return memberRelMapper.selectReceived(me);
    }
    public List<FriendDTO> findSent(Long me) {
        return memberRelMapper.selectSent(me);
    }
    public int countReceived(Long me) {
        return memberRelMapper.countReceived(me);
    }
    // 닉네임 검색
    public List<FriendDTO> searchByAlias(Long me, String keyword, String pattern, int limit) {
        return memberRelMapper.searchByAlias(me, keyword, pattern, limit);
    }
}

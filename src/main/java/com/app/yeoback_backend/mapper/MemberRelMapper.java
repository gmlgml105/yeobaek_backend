package com.app.yeoback_backend.mapper;

import com.app.yeoback_backend.common.enumeration.RelStatus;
import com.app.yeoback_backend.domain.MemberRelVO;
import com.app.yeoback_backend.dto.FriendDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

/** tbl_member_rel (친구 관계) */
@Mapper
public interface MemberRelMapper {
    // 두 회원 사이의 관계 (방향 상관없이 하나)
    Optional<MemberRelVO> selectBetween(@Param("me") Long me, @Param("other") Long other);
    // 친구 신청 (pending)
    void insert(@Param("requesterId") Long requesterId, @Param("receiverId") Long receiverId);
    // 상태 변경 (수락 → done)
    void updateStatus(@Param("id") Long id, @Param("status") RelStatus status);
    // 관계 삭제 (취소 · 거절 · 친구 끊기)
    void delete(Long id);

    // 내 친구 목록 (done)
    List<FriendDTO> selectFriends(Long me);
    // 받은 신청 (상대 → 나, pending)
    List<FriendDTO> selectReceived(Long me);
    // 보낸 신청 (나 → 상대, pending)
    List<FriendDTO> selectSent(Long me);
    // 받은 신청 수 (알림 표시용)
    int countReceived(Long me);
    // 닉네임으로 회원 찾기 (나 제외, 활동 중인 회원) + 나와의 관계
    List<FriendDTO> searchByAlias(@Param("me") Long me, @Param("keyword") String keyword,
                                  @Param("pattern") String pattern, @Param("limit") int limit);
}

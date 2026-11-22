package com.app.yeoback_backend.dto;

import com.app.yeoback_backend.common.enumeration.FriendState;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * 친구 API(/api/friends) 응답. 요청 바디는 없다 (상대 회원은 URL 의 memberId, 나는 로그인 회원)
 *
 *  목록 (친구 · 받은 신청 · 보낸 신청) : memberId · alias · avatarUrl · state · since
 *  관계 조회 · 신청 · 수락 · 삭제        : memberId · state (바뀐 뒤의 관계)
 *
 *  state : none · requested(내가 신청) · received(나에게 신청) · friend
 *  since : 신청 목록이면 신청한 시각, 친구 목록이면 친구가 된 시각
 */
@Getter @Setter @ToString
@NoArgsConstructor
public class FriendDTO {
    private Long memberId;          // 상대 회원 id
    private String alias;           // 상대 닉네임
    private String avatarUrl;       // 상대 프로필 사진 (/api/files/{id}), 없으면 null
    private FriendState state;
    private String since;

    public static FriendDTO state(Long memberId, FriendState state) {
        FriendDTO dto = new FriendDTO();
        dto.memberId = memberId;
        dto.state = state;
        return dto;
    }
}

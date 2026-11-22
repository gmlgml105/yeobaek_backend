package com.app.yeoback_backend.domain;

import com.app.yeoback_backend.audit.Period;
import com.app.yeoback_backend.common.enumeration.RelStatus;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * tbl_member_rel 한 행 — 두 회원 사이의 친구 관계 (방향과 상관없이 한 쌍에 한 행)
 *  - relStatus pending : relRequesterId 가 relReceiverId 에게 신청한 상태
 *  - relStatus done    : 친구 (updatedDatetime = 친구가 된 시각)
 *  거절 · 취소 · 친구 끊기는 행을 지운다
 */
@Getter
@ToString(callSuper = true)
@EqualsAndHashCode(of = "id", callSuper = false)
@SuperBuilder
@NoArgsConstructor
public class MemberRelVO extends Period {
    private Long id;
    private Long relRequesterId;
    private Long relReceiverId;
    private RelStatus relStatus;

    /** 이 관계에서 me 의 상대 회원 id */
    public Long otherOf(Long me) {
        return me.equals(relRequesterId) ? relReceiverId : relRequesterId;
    }
}

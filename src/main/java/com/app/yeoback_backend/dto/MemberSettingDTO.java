package com.app.yeoback_backend.dto;

import com.app.yeoback_backend.common.enumeration.Visibility;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * tbl_member_setting 한 행 (id = 회원 id)
 * 수정할 때는 null 인 항목은 바꾸지 않는다 (MemberSettingMapper.update)
 */
@Getter @Setter @ToString
@NoArgsConstructor
public class MemberSettingDTO {
    private Long id;
    private String themaColor;
    private Visibility nameVisibility;   // 실명 공개 범위
    private Visibility phoneVisibility;  // 연락처 공개 범위
    private Boolean noteAutoSave;
}

package com.app.yeoback_backend.dto;

import com.app.yeoback_backend.common.enumeration.Visibility;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

/**
 * 개인 프로필 API(/api/profile) 요청 · 응답 공용
 *
 *  GET /api/profile/{memberId}
 *    - 본인이면  내 프로필: 아래 항목 전부
 *    - 타인이면  회원 프로필: id · alias · bio · avatarUrl · preferences · vibes · nameVisibility · phoneVisibility
 *               + name · phone 은 보는 사람에게 공개된 경우만 (공개, 또는 친구만 + 친구 사이)
 *               + 보는 사람에게 공개되지 않았으면 그 항목의 visibility 는 private 로 보낸다
 *               + email · noteAutoSave · themeColor 는 null
 *  수정 API 응답 : 수정 후의 내 프로필 전부
 *  PUT   /api/profile          요청에서 alias · bio 만 읽는다
 *  PATCH /api/profile/settings 요청에서 preferences · vibes · nameVisibility · phoneVisibility · noteAutoSave · themeColor 만 읽는다
 *                              (null = 그대로, 취향 · 키워드는 보낼 때 목록 전체로 교체)
 *
 *  id · name · email · phone · avatarUrl 은 응답 전용이다. 요청에 와도 ProfileService 가 읽지 않는다.
 *  avatarUrl  : 프로필 사진 주소(/api/files/{id}), 없으면 null (화면은 닉네임 첫 글자)
 *  themeColor : 팔레트 id(sky · aqua · blossom · lavender · mint) 또는 직접 고른 #rrggbb
 */
@Getter @Setter @ToString
@NoArgsConstructor
public class ProfileDTO {
    // 응답 전용
    private Long id;
    private String name;
    private String email;
    private String phone;
    private String avatarUrl;

    // PUT /api/profile (프로필 수정 화면)
    private String alias;               // 2~12자
    private String bio;                 // 80자 이내, 비워도 됨

    // PATCH /api/profile/settings (세부 설정 화면)
    private List<String> preferences;   // 여행 취향 이름 목록
    private List<String> vibes;         // 여행 키워드 이름 목록
    private Visibility nameVisibility;  // 실명 공개 범위 (private · friends · public)
    private Visibility phoneVisibility; // 연락처 공개 범위
    private Boolean noteAutoSave;       // 수첩 대화 자동 저장
    private String themeColor;
}

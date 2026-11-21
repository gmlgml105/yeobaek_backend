package com.app.yeoback_backend.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

/**
 * 개인 프로필 API(/api/profile) 요청 · 응답 공용
 *
 *  응답 (모든 API 공통) : 아래 항목 전부를 채워 돌려준다
 *  PUT   /api/profile          요청에서 alias · bio 만 읽는다
 *  PATCH /api/profile/settings 요청에서 preferences · vibes · aliasPublic · phonePublic · noteAutoSave · themeColor 만 읽는다
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
    private Boolean aliasPublic;        // 닉네임 공개
    private Boolean phonePublic;        // 연락처 공개
    private Boolean noteAutoSave;       // 수첩 대화 자동 저장
    private String themeColor;
}

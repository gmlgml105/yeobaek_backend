package com.app.yeoback_backend.dto;

import com.app.yeoback_backend.common.enumeration.CodePurpose;
import com.app.yeoback_backend.common.enumeration.ContactMethod;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * 인증 API(/api/auth) 요청 · 응답 공용. 값이 null 인 항목은 응답 JSON 에 넣지 않는다.
 *
 *  요청
 *   POST /login           loginId · memberPassword
 *   POST /code/send       purpose · method · contact
 *   POST /code/verify     purpose · method · contact · code
 *   POST /password/reset  method · contact · memberPassword(새 비밀번호)
 *
 *  응답 (아래 정적 메서드로 만든다)
 *   /login · /refresh     accessToken · member                 → login(...)
 *   /code/send            expiresAt · resendAvailableAt · testCode → sentCode(...)
 *   /code/verify          ok · reason · attemptsLeft           → verified() · verifyFail(...) · mismatch(...)
 *
 *  시간은 epoch milliseconds. testCode 는 verification.expose-code=true(개발용)일 때만 채워진다.
 *  verify reason: mismatch · expired · too_many · no_code
 */
@Getter @Setter
@ToString(exclude = "memberPassword")
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthDTO {
    // ── 요청 ──
    private String loginId;             // 이메일 또는 휴대폰 번호
    // 받기만 하고 응답에는 절대 넣지 않는다
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String memberPassword;      // 로그인 비밀번호 · 재설정할 새 비밀번호
    private CodePurpose purpose;
    private ContactMethod method;
    private String contact;
    private String code;

    // ── 응답: 로그인 ──
    private String accessToken;
    private MemberDTO member;

    // ── 응답: 인증코드 발송 ──
    private Long expiresAt;
    private Long resendAvailableAt;
    private String testCode;

    // ── 응답: 인증코드 확인 ──
    private Boolean ok;
    private String reason;
    private Integer attemptsLeft;

    public static AuthDTO login(String accessToken, MemberDTO member) {
        AuthDTO dto = new AuthDTO();
        dto.accessToken = accessToken;
        dto.member = member;
        return dto;
    }

    public static AuthDTO sentCode(long expiresAt, long resendAvailableAt, String testCode) {
        AuthDTO dto = new AuthDTO();
        dto.expiresAt = expiresAt;
        dto.resendAvailableAt = resendAvailableAt;
        dto.testCode = testCode;
        return dto;
    }

    public static AuthDTO verified() {
        AuthDTO dto = new AuthDTO();
        dto.ok = true;
        return dto;
    }

    public static AuthDTO verifyFail(String reason) {
        AuthDTO dto = new AuthDTO();
        dto.ok = false;
        dto.reason = reason;
        return dto;
    }

    public static AuthDTO mismatch(int attemptsLeft) {
        AuthDTO dto = verifyFail("mismatch");
        dto.attemptsLeft = attemptsLeft;
        return dto;
    }
}

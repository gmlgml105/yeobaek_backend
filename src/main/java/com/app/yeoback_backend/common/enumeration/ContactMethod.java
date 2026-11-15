package com.app.yeoback_backend.common.enumeration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

/** 가입·로그인 수단 (이메일 · 휴대폰) */
public enum ContactMethod {
    EMAIL("email"), PHONE("phone");

    private final String value;

    ContactMethod(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static ContactMethod from(String value) {
        return Arrays.stream(values()).filter(m -> m.value.equals(value)).findFirst().orElse(null);
    }

    /** 프론트 normalizeContact 와 같은 규칙: 이메일은 소문자, 휴대폰은 숫자만 */
    public String normalize(String contact) {
        if (contact == null) return null;
        return this == EMAIL ? contact.trim().toLowerCase() : contact.replaceAll("\\D", "");
    }

    /** 로그인 아이디(이메일 또는 휴대폰)를 보고 수단을 고른다 */
    public static ContactMethod guess(String loginId) {
        return loginId != null && loginId.contains("@") ? EMAIL : PHONE;
    }
}

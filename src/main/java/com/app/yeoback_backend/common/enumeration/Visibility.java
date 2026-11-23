package com.app.yeoback_backend.common.enumeration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/** 공개 범위 (tbl_member_setting.name_visibility · phone_visibility): 비공개 · 친구만 · 공개 */
public enum Visibility {
    PRIVATE("private"), FRIENDS("friends"), PUBLIC("public");

    private final String value;

    private static final Map<String, Visibility> VALUE_MAP =
            Arrays.stream(Visibility.values()).collect(Collectors.toMap(Visibility::getValue, Function.identity()));

    Visibility(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    /** 소문자 값 → enum (없으면 null). JSON 요청 값도 이걸로 바꾼다 */
    @JsonCreator
    public static Visibility from(String value) {
        return value == null ? null : VALUE_MAP.get(value.trim().toLowerCase());
    }
}

package com.app.yeoback_backend.common.enumeration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/** 보는 사람 기준의 친구 관계 (응답 전용): 없음 · 내가 신청함 · 나에게 신청함 · 친구 */
public enum FriendState {
    NONE("none"), REQUESTED("requested"), RECEIVED("received"), FRIEND("friend");

    private final String value;

    private static final Map<String, FriendState> VALUE_MAP =
            Arrays.stream(FriendState.values()).collect(Collectors.toMap(FriendState::getValue, Function.identity()));

    FriendState(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    /** 소문자 값 → enum (없으면 null). JSON 요청 값도 이걸로 바꾼다 */
    @JsonCreator
    public static FriendState from(String value) {
        return value == null ? null : VALUE_MAP.get(value.trim().toLowerCase());
    }
}

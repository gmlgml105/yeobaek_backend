package com.app.yeoback_backend.common.enumeration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/** 친구 관계 상태 (tbl_member_rel.rel_status): 신청 중 · 친구 */
public enum RelStatus {
    PENDING("pending"), DONE("done");

    private final String value;

    private static final Map<String, RelStatus> VALUE_MAP =
            Arrays.stream(RelStatus.values()).collect(Collectors.toMap(RelStatus::getValue, Function.identity()));

    RelStatus(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    /** 소문자 값 → enum (없으면 null). JSON 요청 값도 이걸로 바꾼다 */
    @JsonCreator
    public static RelStatus from(String value) {
        return value == null ? null : VALUE_MAP.get(value.trim().toLowerCase());
    }
}

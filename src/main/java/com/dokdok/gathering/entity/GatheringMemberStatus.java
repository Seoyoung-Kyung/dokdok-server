package com.dokdok.gathering.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum GatheringMemberStatus {
    PENDING("PENDING", "가입요청"),
    ACTIVE("ACTIVE", "가입승인"),
    REJECTED("REJECTED", "가입거절");

    private final String code;
    private final String description;

    // 소문자 -> 대문자로 변환합니다.
    @JsonCreator
    public static GatheringMemberStatus from(String value) {
        return GatheringMemberStatus.valueOf(value.toUpperCase());
    }
}

package com.dokdok.gathering.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum GatheringStatus {
    ACTIVE("ACTIVE","활성"),
    INACTIVE("INACTIVE","비활성");

    private final String code;
    private final String description;
}

package com.dokdok.gathering.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum GatheringRole {
    LEADER("LEADER","모임장"),
    MEMBER("MEMBER","모임원");

    private final String code;
    private final String description;
}

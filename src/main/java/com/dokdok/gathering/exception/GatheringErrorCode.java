package com.dokdok.gathering.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum GatheringErrorCode {
    GATHERING_NOT_FOUND("GA001","모임을 찾을 수 없습니다."),
    NOT_GATHERING_MEMBER("GA002", "모임 멤버가 아닙니다.");

    private final String code;
    private final String message;
}

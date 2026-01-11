package com.dokdok.gathering.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum GatheringErrorCode {
    GATHERING_NOT_FOUND("GA001","모임을 찾을 수 없습니다."),
    INVALID_GATHERING_STATUS("GA002","유효하지 않은 모임 상태입니다.");

    private final String code;
    private final String message;
}

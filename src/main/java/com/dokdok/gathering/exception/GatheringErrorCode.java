package com.dokdok.gathering.exception;

import com.dokdok.global.exception.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
public enum GatheringErrorCode implements BaseErrorCode {

    GATHERING_NOT_FOUND("G001", "모임을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    NOT_GATHERING_MEMBER("G002", "모임의 멤버가 아닙니다.", HttpStatus.FORBIDDEN);

    private final String code;
    private final String message;
    private final HttpStatus status;
}

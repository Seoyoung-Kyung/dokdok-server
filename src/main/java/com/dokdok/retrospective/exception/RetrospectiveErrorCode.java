package com.dokdok.retrospective.exception;

import com.dokdok.global.exception.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum RetrospectiveErrorCode implements BaseErrorCode {

    RETROSPECTIVE_ALREADY_EXISTS("R101", "이미 해당 약속에 대한 회고가 존재합니다.", HttpStatus.CONFLICT);

    private final String code;
    private final String message;
    private final HttpStatus status;
}

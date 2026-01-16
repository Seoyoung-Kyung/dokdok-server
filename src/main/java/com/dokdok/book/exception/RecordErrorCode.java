package com.dokdok.book.exception;

import com.dokdok.global.exception.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
public enum RecordErrorCode implements BaseErrorCode {
    INVALID_RECORD_REQUEST("R001", "기록 타입에 필요한 입력값이 누락되었습니다.", HttpStatus.BAD_REQUEST),
    INVALID_RECORD_TYPE("R002", "존재하지 않는 타입입니다.", HttpStatus.BAD_REQUEST);

    private final String code;
    private final String message;
    private final HttpStatus status;
}

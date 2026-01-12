package com.dokdok.user.exception;

import com.dokdok.global.exception.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum UserErrorCode implements BaseErrorCode {

    USER_NOT_FOUND("U001", "존재하지 않는 사용자입니디.", HttpStatus.NOT_FOUND),
    NICKNAME_ALREADY_EXISTS("U002", "이미 존재하는 사용자 닉네임입니다.", HttpStatus.CONFLICT),
    NICKNAME_EMPTY("U003", "닉네임은 필수 입력 항목입니다.", HttpStatus.BAD_REQUEST),
    NICKNAME_LENGTH_INVALID("U004", "닉네임은 2자 이상 20자 이하로 입력해주세요.", HttpStatus.BAD_REQUEST),
    NICKNAME_FORMAT_INVALID("U005", "닉네임은 한글, 영문, 숫자만 사용 가능합니다.", HttpStatus.BAD_REQUEST);

    private final String code;
    private final String message;
    private final HttpStatus status;
}

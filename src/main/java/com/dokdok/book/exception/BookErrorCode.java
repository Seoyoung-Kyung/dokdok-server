package com.dokdok.book.exception;

import com.dokdok.global.exception.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
public enum BookErrorCode implements BaseErrorCode {

    BOOK_NOT_FOUND("B001", "책을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    BOOK_ALREADY_EXISTS("B002", "이미 등록된 책입니다.", HttpStatus.CONFLICT),
    BOOK_NOT_IN_SHELF("B003", "책장에 해당 책이 존재하지 않습니다.", HttpStatus.NOT_FOUND);

    private final String code;
    private final String message;
    private final HttpStatus status;
}

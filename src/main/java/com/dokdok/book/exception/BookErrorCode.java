package com.dokdok.book.exception;

import com.dokdok.global.exception.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
public enum BookErrorCode implements BaseErrorCode {

    BOOK_NOT_FOUND("B001", "책을 찾을 수 없습니다.",HttpStatus.NOT_FOUND),
    BOOK_REVIEW_NOT_FOUND("B002", "책 리뷰를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    BOOK_REVIEW_ALREADY_EXISTS("B003", "이미 책 리뷰가 존재합니다.", HttpStatus.CONFLICT),
    KEYWORD_NOT_FOUND("B004", "키워드를 찾을 수 없습니다.", HttpStatus.NOT_FOUND);

    private final String code;
    private final String message;
    private final HttpStatus status;
}

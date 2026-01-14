package com.dokdok.book.exception;

import com.dokdok.global.exception.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
public enum BookErrorCode implements BaseErrorCode {

    BOOK_NOT_FOUND("B001", "책을 찾을 수 없습니다.",HttpStatus.NOT_FOUND),
    BOOK_ALREADY_EXISTS("B002", "이미 책이 존재합니다.", HttpStatus.CONFLICT),
    BOOK_REVIEW_NOT_FOUND("B003", "책 리뷰를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    BOOK_REVIEW_ALREADY_EXISTS("B004", "이미 책 리뷰가 존재합니다.", HttpStatus.CONFLICT),
    KEYWORD_NOT_FOUND("B005", "키워드를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    KEYWORD_NOT_SELECTABLE("B006", "선택할 수 없는 키워드입니다.", HttpStatus.BAD_REQUEST);

    private final String code;
    private final String message;
    private final HttpStatus status;
}

package com.dokdok.book.exception;

import com.dokdok.global.exception.BaseException;

public class BookException extends BaseException {

    public BookException(BookErrorCode errorCode) {super(errorCode);}
    public BookException(BookErrorCode errorCode, String message) {super(errorCode, message);}
    public BookException(BookErrorCode errorCode, Throwable cause) {super(errorCode, cause);}
}

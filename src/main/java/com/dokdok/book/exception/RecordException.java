package com.dokdok.book.exception;

import com.dokdok.global.exception.BaseException;

public class RecordException extends BaseException {

    public RecordException(RecordErrorCode errorCode) {super(errorCode);}
    public RecordException(RecordErrorCode errorCode, String message) {super(errorCode, message);}
    public RecordException(RecordErrorCode errorCode, Throwable cause) {super(errorCode, cause);}
}

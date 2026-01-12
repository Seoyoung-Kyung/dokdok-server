package com.dokdok.global.exception;

public class GlobalException extends BaseException {

    public GlobalException(GlobalErrorCode errorCode) {
        super(errorCode);
    }

    public GlobalException(GlobalErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    public GlobalException(GlobalErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }
}

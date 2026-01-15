package com.dokdok.retrospective.exception;

import com.dokdok.global.exception.BaseException;


public class RetrospectiveException extends BaseException {
    public RetrospectiveException(RetrospectiveErrorCode errorCode) {
        super(errorCode);
    }

    public RetrospectiveException(RetrospectiveErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    public RetrospectiveException(RetrospectiveErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }
}

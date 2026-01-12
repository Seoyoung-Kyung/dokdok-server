package com.dokdok.gathering.exception;

import com.dokdok.global.exception.BaseException;

public class GatheringException extends BaseException {

    public GatheringException(GatheringErrorCode errorCode) {
        super(errorCode);
    }

    public GatheringException(GatheringErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    public GatheringException(GatheringErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }
}

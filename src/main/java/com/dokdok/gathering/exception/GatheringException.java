package com.dokdok.gathering.exception;

import com.dokdok.global.exception.GlobalErrorCode;
import com.dokdok.global.exception.GlobalException;

public class GatheringException extends GlobalException {
    private final GatheringErrorCode gatheringErrorCode;

    public GatheringException(GatheringErrorCode errorCode){
        super(GlobalErrorCode.INVALID_INPUT_VALUE, errorCode.getMessage());
        this.gatheringErrorCode = errorCode;
    }
}

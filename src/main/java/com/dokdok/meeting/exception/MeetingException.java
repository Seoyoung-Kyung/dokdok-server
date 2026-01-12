package com.dokdok.meeting.exception;

import com.dokdok.global.exception.BaseException;

public class MeetingException extends BaseException {

    public MeetingException(MeetingErrorCode errorCode) {
        super(errorCode);
    }

    public MeetingException(MeetingErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    public MeetingException(MeetingErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }
}

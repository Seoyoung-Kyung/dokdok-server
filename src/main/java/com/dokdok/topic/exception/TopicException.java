package com.dokdok.topic.exception;

import com.dokdok.global.exception.BaseException;

public class TopicException extends BaseException {
    public TopicException(TopicErrorCode errorCode) {
        super(errorCode);
    }
    public TopicException(TopicErrorCode errorCode, String message) {
        super(errorCode, message);
    }
    public TopicException(TopicErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }
}

package com.dokdok.user.exception;

import com.dokdok.global.exception.BaseException;

public class UserException extends BaseException {

    public UserException(UserErrorCode errorCode) { super(errorCode); }

    public UserException(UserErrorCode errorCode, String message) { super(errorCode, message); }

    public UserException(UserErrorCode errorCode, Throwable cause) { super(errorCode, cause); }
}

package com.dokdok.storage.exception;

import com.dokdok.global.exception.BaseException;

public class StorageException extends BaseException {

	public StorageException(StorageErrorCode errorCode) {
		super(errorCode);
	}

	public StorageException(StorageErrorCode errorCode, String message) {
		super(errorCode, message);
	}

	public StorageException(StorageErrorCode errorCode, Throwable cause) {
		super(errorCode, cause);
	}
}
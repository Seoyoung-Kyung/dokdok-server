package com.dokdok.storage.exception;

import com.dokdok.global.exception.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum StorageErrorCode implements BaseErrorCode {

	FILE_UPLOAD_FAILED("S001", "파일 업로드에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
	FILE_DELETE_FAILED("S002", "파일 삭제에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
	INVALID_FILE_TYPE("S003", "지원하지 않는 파일 형식입니다.", HttpStatus.BAD_REQUEST),
	FILE_SIZE_EXCEEDED("S004", "파일 크기가 제한을 초과했습니다.", HttpStatus.BAD_REQUEST),
	BUCKET_NOT_FOUND("S005", "스토리지 버킷을 찾을 수 없습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
	PRESIGNED_URL_GENERATION_FAILED("S006", "Presigned URL 생성에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);

	private final String code;
	private final String message;
	private final HttpStatus status;
}

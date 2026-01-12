package com.dokdok.global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
public enum GlobalErrorCode implements BaseErrorCode {

    // 공통 에러
    INVALID_ENUM_VALUE("E001", "유효하지 않은 값입니다.", HttpStatus.BAD_REQUEST),
    INVALID_REQUEST_FORMAT("E002", "잘못된 요청 형식입니다.", HttpStatus.BAD_REQUEST),
    STATUS_ALREADY_SET("E003", "이미 해당 상태입니다.", HttpStatus.BAD_REQUEST),
    JSON_SERIALIZATION_ERROR("E004", "JSON 직렬화 처리 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    NOT_FOUND("E005", "요청한 리소스를 찾을 수 없습니다"),

    // 공통 시스템 에러 (G0xx)
    INTERNAL_SERVER_ERROR("G001", "서버 내부 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_INPUT_VALUE("G002", "입력값이 올바르지 않습니다.", HttpStatus.BAD_REQUEST),
    INVALID_TYPE_VALUE("G003", "타입이 올바르지 않습니다.", HttpStatus.BAD_REQUEST),
    METHOD_NOT_ALLOWED("G004", "지원하지 않는 HTTP 메서드입니다.", HttpStatus.METHOD_NOT_ALLOWED),

    // 인증/인가 (G1xx)
    ACCESS_DENIED("G101", "접근 권한이 없습니다.", HttpStatus.FORBIDDEN),
    UNAUTHORIZED("G102", "인증이 필요합니다.", HttpStatus.UNAUTHORIZED),
    INVALID_TOKEN("G103", "유효하지 않은 토큰입니다.", HttpStatus.UNAUTHORIZED),
    EXPIRED_TOKEN("G104", "만료된 토큰입니다.", HttpStatus.UNAUTHORIZED),
    REFRESH_TOKEN_NOT_FOUND("G105", "리프레시 토큰을 찾을 수 없습니다.", HttpStatus.UNAUTHORIZED),
    NOT_GATHERING_MEMBER("G106", "해당 모임의 멤버가 아닙니다."),
    NOT_MEETING_MEMBER("G107", "해당 약속의 멤버가 아닙니다."),
    NOT_GATHERING_MEETING("G108", "해당 모임의 약속이 아닙니다."),

    // 파일 처리 (G2xx)
    FILE_UPLOAD_FAILED("G201", "파일 업로드에 실패했습니다.", HttpStatus.BAD_REQUEST),
    INVALID_FILE_TYPE("G202", "지원하지 않는 파일 형식입니다.", HttpStatus.BAD_REQUEST),
    FILE_SIZE_EXCEEDED("G203", "파일 크기가 제한을 초과했습니다.", HttpStatus.BAD_REQUEST),

    // 외부 API (G3xx)
    EXTERNAL_API_ERROR("G301", "외부 API 호출에 실패했습니다.", HttpStatus.BAD_GATEWAY),
    KAKAO_API_ERROR("G302", "카카오 API 호출에 실패했습니다.", HttpStatus.BAD_GATEWAY);

    private final String code;
    private final String message;
}

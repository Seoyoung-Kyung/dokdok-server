package com.dokdok.topic.exception;

import com.dokdok.global.exception.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum TopicErrorCode implements BaseErrorCode {
    // 리소스 에러
    TOPIC_NOT_FOUND("E101", "토픽을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    TOPIC_ANSWER_NOT_FOUND("E102", "답변을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    TOPIC_ANSWER_ALREADY_SUBMITTED("E103", "이미 제출된 답변입니다.", HttpStatus.CONFLICT),
    TOPIC_ANSWER_ALREADY_EXISTS("E104", "이미 답변이 존재합니다.", HttpStatus.CONFLICT);

    private final String code;
    private final String message;
    private final HttpStatus status;
}

package com.dokdok.meeting.exception;

import com.dokdok.global.exception.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
public enum MeetingErrorCode implements BaseErrorCode {

    MEETING_NOT_FOUND("M001", "약속을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    GATHERING_NOT_FOUND("M002", "모임을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    BOOK_NOT_FOUND("M003", "책을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    USER_NOT_FOUND("M004", "사용자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND);

    private final String code;
    private final String message;
    private final HttpStatus status;
}

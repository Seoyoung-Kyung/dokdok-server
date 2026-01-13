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
    USER_NOT_FOUND("M004", "사용자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    NOT_GATHERING_MEETING("M005", "모임에 속한 약속이 아닙니다.", HttpStatus.FORBIDDEN),
    NOT_MEETING_MEMBER("M006", "약속의 멤버가 아닙니다.", HttpStatus.FORBIDDEN),
    MEETING_ALREADY_CONFIRMED("M007", "약속이 확정된 경우에는 주제를 제안할 수 없습니다.", HttpStatus.BAD_REQUEST),
    MEETING_FULL("M008", "약속 정원이 마감되었습니다.", HttpStatus.BAD_REQUEST),
    INVALID_MEETING_STATUS_CHANGE("M009", "약속 상태를 변경할 수 없습니다.", HttpStatus.BAD_REQUEST);

    private final String code;
    private final String message;
    private final HttpStatus status;
}

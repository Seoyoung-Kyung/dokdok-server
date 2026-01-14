package com.dokdok.gathering.exception;

import com.dokdok.global.exception.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
public enum GatheringErrorCode implements BaseErrorCode {

    GATHERING_NOT_FOUND("G001", "모임을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    NOT_GATHERING_MEMBER("G002", "모임의 멤버가 아닙니다.", HttpStatus.FORBIDDEN),
    NOT_GATHERING_LEADER("G003","리더만 가능한 작업입니다.", HttpStatus.FORBIDDEN),
    ALREADY_INACTIVE("G004","이미 비활성 상태인 모임은 삭제할 수 없습니다.",HttpStatus.CONFLICT),
    CANNOT_REMOVE_LEADER("G005", "유일한 리더는 강퇴할 수 없습니다.", HttpStatus.FORBIDDEN),
    ALREADY_REMOVED_MEMBER("G006", "이미 제거된 멤버입니다.", HttpStatus.CONFLICT);

    private final String code;
    private final String message;
    private final HttpStatus status;
}
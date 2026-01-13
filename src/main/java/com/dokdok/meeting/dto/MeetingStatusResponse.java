package com.dokdok.meeting.dto;

import com.dokdok.meeting.entity.Meeting;
import com.dokdok.meeting.entity.MeetingStatus;

import java.time.LocalDateTime;

public record MeetingStatusResponse(
        Long meetingId,
        MeetingStatus meetingStatus,
        LocalDateTime confirmedAt
) {
    public static MeetingStatusResponse from(Meeting meeting) {
        if (meeting == null) {
            return null;
        }
        LocalDateTime confirmedAt = meeting.getMeetingStatus() == MeetingStatus.CONFIRMED
                ? LocalDateTime.now()
                : null;
        return new MeetingStatusResponse(
                meeting.getId(),
                meeting.getMeetingStatus(),
                confirmedAt
        );
    }
}

package com.dokdok.meeting.dto;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record MeetingUpdateRequest(
        String meetingName,
        LocalDateTime startDate,
        LocalDateTime endDate,
        String place,
        Integer maxParticipants
) {
}

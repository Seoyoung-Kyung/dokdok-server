package com.dokdok.meeting.dto;

import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record MeetingCreateRequest(
        Long gatheringId,
        Long bookId,
        @Size(min = 1, max = 24) String meetingName,
        LocalDateTime meetingStartDate,
        LocalDateTime meetingEndDate,
        Integer maxParticipants,
        String place
) {
}

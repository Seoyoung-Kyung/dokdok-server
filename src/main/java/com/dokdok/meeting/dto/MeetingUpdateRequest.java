package com.dokdok.meeting.dto;

import jakarta.validation.constraints.Min;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record MeetingUpdateRequest(
        String meetingName,
        LocalDateTime startDate,
        LocalDateTime endDate,
        String place,
        @Min(1)
        Integer maxParticipants
) {
}

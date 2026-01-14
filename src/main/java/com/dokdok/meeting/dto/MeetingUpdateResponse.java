package com.dokdok.meeting.dto;

import com.dokdok.meeting.entity.Meeting;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record MeetingUpdateResponse(
        Long meetingId,
        String meetingName,
        LocalDateTime startDate,
        LocalDateTime endDate,
        String place,
        Integer maxParticipants
) {
    public static MeetingUpdateResponse from(Meeting meeting) {
        return MeetingUpdateResponse.builder()
                .meetingId(meeting.getId())
                .meetingName(meeting.getMeetingName())
                .startDate(meeting.getMeetingStartDate())
                .endDate(meeting.getMeetingEndDate())
                .place(meeting.getPlace())
                .maxParticipants(meeting.getMaxParticipants())
                .build();
    }
}

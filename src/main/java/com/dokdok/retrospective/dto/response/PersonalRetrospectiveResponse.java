package com.dokdok.retrospective.dto.response;

import com.dokdok.retrospective.entity.PersonalMeetingRetrospective;

public record PersonalRetrospectiveResponse(
        Long personalMeetingRetrospectiveId,
        Long meetingId,
        Long userId,
        Boolean isShared
) {
    public static PersonalRetrospectiveResponse from(PersonalMeetingRetrospective retrospective) {
        return new PersonalRetrospectiveResponse(
                retrospective.getId(),
                retrospective.getMeeting().getId(),
                retrospective.getUser().getId(),
                retrospective.getIsShared()
        );
    }
}
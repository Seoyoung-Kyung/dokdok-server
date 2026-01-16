package com.dokdok.meeting.dto;

import lombok.Builder;

@Builder
public record MeetingTabCountsResponse(
        int all,
        int upcoming,
        int done,
        int joined
) {
}

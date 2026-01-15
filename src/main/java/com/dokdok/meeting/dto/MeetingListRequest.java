package com.dokdok.meeting.dto;

import lombok.Builder;

@Builder
public record MeetingListRequest(
        MeetingListFilter filter,
        Integer page,
        Integer size
) {
}

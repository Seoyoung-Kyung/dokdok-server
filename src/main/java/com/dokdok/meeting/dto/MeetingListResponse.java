package com.dokdok.meeting.dto;

import com.dokdok.meeting.entity.MeetingStatus;
import com.dokdok.topic.entity.TopicType;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record MeetingListResponse(
        List<Item> items,
        Integer totalCount,
        Integer currentPage,
        Integer pageSize,
        Integer totalPages
) {
    @Builder
    public record Item(
            Long meetingId,
            String meetingName,
            String bookName,
            LocalDateTime startDateTime,
            LocalDateTime endDateTime,
            List<TopicType> topicTypes,
            boolean joined,
            MeetingStatus meetingStatus
    ) {
    }
}

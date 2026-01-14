package com.dokdok.topic.dto.response;

import com.dokdok.topic.entity.TopicStatus;

public record ConfirmTopicsResponse(
        Long meetingId,
        TopicStatus topicStatus
) {
    public static ConfirmTopicsResponse from(Long meetingId) {
        return new ConfirmTopicsResponse(meetingId, TopicStatus.CONFIRMED);
    }
}

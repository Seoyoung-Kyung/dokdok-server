package com.dokdok.topic.dto.response;

import java.time.LocalDateTime;

public record TopicAnswerResponse(
        Long topicId,
        boolean isSubmitted,
        LocalDateTime updatedAt
) {
}

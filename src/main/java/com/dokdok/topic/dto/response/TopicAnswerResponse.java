package com.dokdok.topic.dto.response;

import com.dokdok.topic.entity.TopicAnswer;
import java.time.LocalDateTime;

public record TopicAnswerResponse(
        Long topicId,
        boolean isSubmitted,
        LocalDateTime updatedAt
) {
    public static TopicAnswerResponse from(TopicAnswer answer) {
        return new TopicAnswerResponse(
                answer.getTopic().getId(),
                Boolean.TRUE.equals(answer.getIsSubmitted()),
                answer.getUpdatedAt()
        );
    }
}

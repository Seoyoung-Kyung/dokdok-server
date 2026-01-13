package com.dokdok.topic.dto.response;

import com.dokdok.topic.entity.TopicAnswer;
import java.time.LocalDateTime;

public record TopicAnswerSubmitResponse(
        Long topicId,
        boolean isSubmitted,
        LocalDateTime submittedAt
) {
    public static TopicAnswerSubmitResponse from(TopicAnswer answer) {
        return new TopicAnswerSubmitResponse(
                answer.getTopic().getId(),
                Boolean.TRUE.equals(answer.getIsSubmitted()),
                answer.getUpdatedAt()
        );
    }
}

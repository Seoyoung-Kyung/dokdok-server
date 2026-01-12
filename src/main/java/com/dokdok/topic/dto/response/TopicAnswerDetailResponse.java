package com.dokdok.topic.dto.response;

import com.dokdok.topic.entity.TopicAnswer;
import java.time.LocalDateTime;

public record TopicAnswerDetailResponse(
        Long topicId,
        String content,
        boolean isSubmitted,
        LocalDateTime updatedAt
) {
    public static TopicAnswerDetailResponse from(TopicAnswer answer) {
        return new TopicAnswerDetailResponse(
                answer.getTopic().getId(),
                answer.getContent(),
                Boolean.TRUE.equals(answer.getIsSubmitted()),
                answer.getUpdatedAt()
        );
    }
}

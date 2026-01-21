package com.dokdok.retrospective.dto.response;

import com.dokdok.topic.entity.Topic;

public record TopicInfo(
        Long topicId,
        String topicName
) {
    public static TopicInfo from(Topic topic) {
        return new TopicInfo(
                topic.getId(),
                topic.getTitle()
        );
    }
}
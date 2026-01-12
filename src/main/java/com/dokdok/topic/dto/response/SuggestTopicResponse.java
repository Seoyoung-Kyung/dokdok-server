package com.dokdok.topic.dto.response;

import com.dokdok.topic.entity.Topic;
import com.dokdok.topic.enums.TopicType;
import com.dokdok.user.entity.User;
import lombok.Builder;

@Builder
public record SuggestTopicResponse(
        Long topicId,
        Long meetingId,
        String title,
        String description,
        TopicType topicType,
        CreatedByInfo createdBy
) {

    public static SuggestTopicResponse from(Topic topic, User user) {
        return SuggestTopicResponse.builder()
                .topicId(topic.getId())
                .meetingId(topic.getMeeting().getId())
                .title(topic.getTitle())
                .description(topic.getDescription())
                .topicType(topic.getTopicType())
                .createdBy(CreatedByInfo.of(
                        user.getId(),
                        user.getNickname()
                ))
                .build();
    }
}
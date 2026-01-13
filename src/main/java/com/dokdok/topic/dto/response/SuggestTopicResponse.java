package com.dokdok.topic.dto.response;

import com.dokdok.topic.entity.Topic;
import com.dokdok.topic.entity.TopicType;
import com.dokdok.user.entity.User;
import lombok.Builder;

@Builder
public record SuggestTopicResponse(
        Long topicId,
        Long meetingId,
        String title,
        String description,
        TopicType topicType,
        String topicTypeLabel,
        String topicTypeDescription,
        CreatedByInfo createdBy
) {

    public static SuggestTopicResponse from(Topic topic, User user) {
        return SuggestTopicResponse.builder()
                .topicId(topic.getId())
                .meetingId(topic.getMeeting().getId())
                .title(topic.getTitle())
                .description(topic.getDescription())
                .topicType(topic.getTopicType())
                .topicTypeLabel(topic.getTopicType().getDisplayName())
                .topicTypeDescription(topic.getTopicType().getDescription())
                .createdBy(CreatedByInfo.of(
                        user.getId(),
                        user.getNickname()
                ))
                .build();
    }
}
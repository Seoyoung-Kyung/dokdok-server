package com.dokdok.topic.dto;

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

    public record CreatedByInfo(
        Long userId,
        String nickName
    ) {
        public static CreatedByInfo of(Long userId, String nickName) {
            return new CreatedByInfo(userId, nickName);
        }
    }

    public static SuggestTopicResponse from(Topic topic, User user) {
        return new SuggestTopicResponse(
                topic.getId(),
                topic.getMeeting().getId(),
                topic.getTitle(),
                topic.getDescription(),
                topic.getTopicType(),
                CreatedByInfo.of(
                        user.getId(),
                        user.getNickname()
                )
        );
    }
}
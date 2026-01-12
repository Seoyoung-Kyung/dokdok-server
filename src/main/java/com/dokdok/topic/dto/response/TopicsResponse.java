package com.dokdok.topic.dto.response;

import com.dokdok.topic.entity.Topic;
import com.dokdok.topic.entity.TopicStatus;
import com.dokdok.topic.entity.TopicType;

import java.util.List;

public record TopicsResponse(
        List<TopicDto> topics
) {

    public record TopicDto(
            Long topicId,
            Long meetingId,
            String title,
            String description,
            TopicType topicType,
            TopicStatus topicStatus,
            Integer likeCount,
            CreatedByInfo createdByInfo
    ) {
        public static TopicDto from(Topic topic) {
            return new TopicDto(
                    topic.getId(),
                    topic.getMeeting().getId(),
                    topic.getTitle(),
                    topic.getDescription(),
                    topic.getTopicType(),
                    topic.getTopicStatus(),
                    topic.getLikeCount(),
                    CreatedByInfo.of(
                            topic.getProposedBy().getId(),
                            topic.getProposedBy().getNickname()
                    )
            );
        }
    }

    public static TopicsResponse from(List<Topic> topics) {
        return new TopicsResponse(
                topics.stream()
                        .map(TopicDto::from)
                        .toList()
        );
    }
}
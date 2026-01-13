package com.dokdok.topic.dto.response;

import com.dokdok.topic.entity.Topic;
import com.dokdok.topic.entity.TopicStatus;
import com.dokdok.topic.entity.TopicType;
import lombok.Builder;

import java.util.List;

public record TopicsResponse(
        List<TopicDto> topics
) {

    @Builder
    public record TopicDto(
            Long topicId,
            Long meetingId,
            String title,
            String description,
            TopicType topicType,
            String topicTypeLabel,
            TopicStatus topicStatus,
            Integer likeCount,
            CreatedByInfo createdByInfo
    ) {
        public static TopicDto from(Topic topic) {
            return TopicDto.builder()
                    .topicId(topic.getId())
                    .meetingId(topic.getMeeting().getId())
                    .title(topic.getTitle())
                    .description(topic.getDescription())
                    .topicType(topic.getTopicType())
                    .topicTypeLabel(topic.getTopicType().getDisplayName())
                    .topicStatus(topic.getTopicStatus())
                    .likeCount(topic.getLikeCount())
                    .createdByInfo(
                            CreatedByInfo.of(
                                    topic.getProposedBy().getId(),
                                    topic.getProposedBy().getNickname()
                            )
                    ).build();
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
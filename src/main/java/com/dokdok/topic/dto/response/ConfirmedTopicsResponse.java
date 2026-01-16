package com.dokdok.topic.dto.response;

import com.dokdok.topic.entity.Topic;
import com.dokdok.topic.entity.TopicType;
import lombok.Builder;

import java.util.List;

public record ConfirmedTopicsResponse(
        Long meetingId,
        List<ConfirmedTopicDto> topics
) {

    @Builder
    public record ConfirmedTopicDto(
            Long topicId,
            String title,
            String description,
            TopicType topicType,
            Integer confirmOrder,
            CreatedByInfo createdByInfo
    ) {
        public static ConfirmedTopicDto from(Topic topic) {
            return ConfirmedTopicDto.builder()
                    .topicId(topic.getId())
                    .title(topic.getTitle())
                    .description(topic.getDescription())
                    .topicType(topic.getTopicType())
                    .confirmOrder(topic.getConfirmOrder())
                    .createdByInfo(
                            CreatedByInfo.of(
                                    topic.getProposedBy().getId(),
                                    topic.getProposedBy().getNickname()
                            )
                    )
                    .build();
        }
    }

    public static ConfirmedTopicsResponse from(
            Long meetingId,
            List<ConfirmedTopicDto> topics
    ) {
        return new ConfirmedTopicsResponse(meetingId, topics);
    }
}

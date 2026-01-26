package com.dokdok.topic.dto.response;

import com.dokdok.topic.entity.Topic;
import com.dokdok.topic.entity.TopicType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.List;

@Schema(description = "확정된 주제 목록 응답")
public record ConfirmedTopicsResponse(
        @Schema(description = "약속 ID", example = "1")
        Long meetingId,

        @Schema(description = "확정된 주제 목록")
        List<ConfirmedTopicDto> topics
) {

    @Builder
    @Schema(description = "확정된 주제 정보")
    public record ConfirmedTopicDto(
            @Schema(description = "주제 ID", example = "1")
            Long topicId,

            @Schema(description = "주제 제목", example = "책의 주요 메시지")
            String title,

            @Schema(description = "주제 설명", example = "이 책에서 전달하고자 하는 핵심 메시지는 무엇인가요?")
            String description,

            @Schema(description = "주제 타입", example = "DISCUSSION",
                    allowableValues = {"FREE", "DISCUSSION", "EMOTION", "EXPERIENCE", "CHARACTER_ANALYSIS", "COMPARISON", "STRUCTURE", "IN_DEPTH", "CREATIVE", "CUSTOM"})
            TopicType topicType,

            @Schema(description = "확정 순서", example = "1")
            Integer confirmOrder,

            @Schema(description = "작성자 정보")
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

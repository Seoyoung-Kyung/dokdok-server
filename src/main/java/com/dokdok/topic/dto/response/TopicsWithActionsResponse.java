package com.dokdok.topic.dto.response;

import com.dokdok.global.response.CursorResponse;
import com.dokdok.topic.entity.Topic;
import com.dokdok.topic.entity.TopicStatus;
import com.dokdok.topic.entity.TopicType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.List;
import java.util.Set;

@Builder
@Schema(description = "주제 목록 및 권한 정보 응답")
public record TopicsWithActionsResponse(
        @Schema(description = "주제 목록 페이지 정보")
        CursorResponse<TopicDto, TopicsCursor> page,

        @Schema(description = "사용자 권한 정보")
        Actions actions
) {

    @Schema(description = "사용자 권한 정보")
    public record Actions(
            @Schema(description = "주제 확정 가능 여부", example = "true")
            Boolean canConfirm,

            @Schema(description = "주제 제안 가능 여부", example = "true")
            Boolean canSuggest
    ) {
        public static Actions of(Boolean canConfirm, Boolean canSuggest) {
            return new Actions(canConfirm, canSuggest);
        }
    }

    @Builder
    @Schema(description = "주제 정보")
    public record TopicDto(
            @Schema(description = "주제 ID", example = "1")
            Long topicId,

            @Schema(description = "약속 ID", example = "1")
            Long meetingId,

            @Schema(description = "주제 제목", example = "책의 주요 메시지")
            String title,

            @Schema(description = "주제 설명", example = "이 책에서 전달하고자 하는 핵심 메시지는 무엇인가요?")
            String description,

            @Schema(description = "주제 타입", example = "DISCUSSION",
                    allowableValues = {"FREE", "DISCUSSION", "EMOTION", "EXPERIENCE", "CHARACTER_ANALYSIS", "COMPARISON", "STRUCTURE", "IN_DEPTH", "CREATIVE", "CUSTOM"})
            TopicType topicType,

            @Schema(description = "주제 타입 라벨", example = "토론형")
            String topicTypeLabel,

            @Schema(description = "주제 상태", example = "PROPOSED",
                    allowableValues = {"PROPOSED", "CONFIRMED"})
            TopicStatus topicStatus,

            @Schema(description = "좋아요 수", example = "5")
            Integer likeCount,

            @Schema(description = "삭제 가능 여부", example = "true")
            Boolean canDelete,

            @Schema(description = "작성자 정보")
            CreatedByInfo createdByInfo
    ) {
        public static TopicDto from(Topic topic, Boolean canDelete) {
            return TopicDto.builder()
                    .topicId(topic.getId())
                    .meetingId(topic.getMeeting().getId())
                    .title(topic.getTitle())
                    .description(topic.getDescription())
                    .topicType(topic.getTopicType())
                    .topicTypeLabel(topic.getTopicType().getDisplayName())
                    .topicStatus(topic.getTopicStatus())
                    .likeCount(topic.getLikeCount())
                    .canDelete(canDelete)
                    .createdByInfo(
                            CreatedByInfo.of(
                                    topic.getProposedBy().getId(),
                                    topic.getProposedBy().getNickname()
                            )
                    ).build();
        }
    }

    public static TopicsWithActionsResponse from(
            List<Topic> topics,
            int pageSize,
            boolean hasNext,
            Set<Long> deletableTopicIds,
            Actions actions,
            Long totalCount
    ) {
        List<TopicDto> topicDtos = topics.stream()
                .map(topic -> TopicDto.from(
                        topic,
                        deletableTopicIds.contains(topic.getId())
                ))
                .toList();

        TopicsCursor cursor = null;
        if (hasNext && !topics.isEmpty()) {
            Topic lastTopic = topics.get(topics.size() - 1);
            cursor = TopicsCursor.from(lastTopic);
        }

        CursorResponse<TopicDto, TopicsCursor> page =
                CursorResponse.of(topicDtos, pageSize, hasNext, cursor,
                        totalCount != null ? totalCount.intValue() : null);

        return new TopicsWithActionsResponse(page, actions);
    }
}

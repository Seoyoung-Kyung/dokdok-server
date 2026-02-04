package com.dokdok.topic.dto.response;

import com.dokdok.topic.entity.Topic;
import com.dokdok.topic.entity.TopicType;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.List;

@JsonPropertyOrder({"items", "pageSize", "hasNext", "nextCursor", "totalCount", "actions"})
@Schema(description = "확정된 주제 목록 응답")
public record ConfirmedTopicsResponse(
        @Schema(description = "확정된 주제 목록")
        List<ConfirmedTopicDto> items,
        @Schema(description = "페이지 크기", example = "10")
        int pageSize,
        @Schema(description = "다음 페이지 존재 여부", example = "false")
        boolean hasNext,
        @Schema(description = "다음 페이지 커서 (hasNext가 false면 null)")
        ConfirmedTopicsCursor nextCursor,
        @JsonInclude(JsonInclude.Include.NON_NULL)
        @Schema(description = "전체 아이템 수", example = "25")
        Integer totalCount,
        @Schema(description = "사전 의견 관련 권한 정보")
        Actions actions
) {
    public record Actions(
            @Schema(description = "사전 의견 확인 가능 여부", example = "true")
            Boolean canViewPreOpinions,
            @Schema(description = "사전 의견 작성 가능 여부", example = "false")
            Boolean canWritePreOpinions
    ) {
        public static Actions of(Boolean canViewPreOpinions, Boolean canWritePreOpinions) {
            return new Actions(canViewPreOpinions, canWritePreOpinions);
        }
    }

    @Builder
    @Schema(description = "확정된 주제 정보")
    public record ConfirmedTopicDto(
            @Schema(description = "주제 ID", example = "10")
            Long topicId,
            @Schema(description = "주제 제목", example = "데미안에서 '자기 자신'이란?")
            String title,
            @Schema(description = "주제 설명", example = "주제에 대한 간단한 설명입니다.")
            String description,
            @Schema(description = "주제 타입", example = "DISCUSSION")
            TopicType topicType,
            @Schema(description = "확정 순서", example = "1")
            Integer confirmOrder,
            @Schema(description = "주제 제안자 정보")
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
            List<ConfirmedTopicDto> items,
            int pageSize,
            boolean hasNext,
            ConfirmedTopicsCursor nextCursor,
            Integer totalCount,
            Actions actions
    ) {
        return new ConfirmedTopicsResponse(
                items,
                pageSize,
                hasNext,
                hasNext ? nextCursor : null,
                totalCount,
                actions
        );
    }
}

package com.dokdok.topic.dto.response;

import com.dokdok.topic.entity.TopicAnswer;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "내 토픽 답변 상세 응답")
public record TopicAnswerDetailResponse(
        @Schema(description = "토픽 ID", example = "12")
        Long topicId,
        @Schema(description = "답변 내용", example = "이 책을 읽고 ...")
        String content,
        @Schema(description = "제출 여부", example = "true")
        boolean isSubmitted,
        @Schema(description = "수정 일시", example = "2026-01-19T20:01:37.105545")
        LocalDateTime updatedAt
) {
    public static TopicAnswerDetailResponse from(TopicAnswer answer) {
        return new TopicAnswerDetailResponse(
                answer.getTopic().getId(),
                answer.getContent(),
                Boolean.TRUE.equals(answer.getIsSubmitted()),
                answer.getUpdatedAt()
        );
    }
}

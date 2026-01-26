package com.dokdok.topic.dto.response;

import com.dokdok.topic.entity.TopicStatus;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "주제 확정 응답")
public record ConfirmTopicsResponse(
        @Schema(description = "약속 ID", example = "1")
        Long meetingId,

        @Schema(description = "주제 상태 (PROPOSED: 제안됨, CONFIRMED: 확정됨)", example = "CONFIRMED",
                allowableValues = {"PROPOSED", "CONFIRMED"})
        TopicStatus topicStatus
) {
    public static ConfirmTopicsResponse from(Long meetingId) {
        return new ConfirmTopicsResponse(meetingId, TopicStatus.CONFIRMED);
    }
}

package com.dokdok.meeting.dto;

import com.dokdok.meeting.entity.MeetingStatus;
import com.dokdok.topic.entity.TopicType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "약속 목록 응답")
@Builder
public record MeetingListResponse(
        @Schema(description = "약속 목록")
        List<Item> items,

        @Schema(description = "전체 약속 수", example = "25")
        Integer totalCount,

        @Schema(description = "현재 페이지 (0부터 시작)", example = "0")
        Integer currentPage,

        @Schema(description = "페이지 크기", example = "4")
        Integer pageSize,

        @Schema(description = "전체 페이지 수", example = "7")
        Integer totalPages
) {
    @Schema(description = "약속 목록 아이템")
    @Builder
    public record Item(
            @Schema(description = "약속 ID", example = "1")
            Long meetingId,

            @Schema(description = "약속 이름", example = "1월 독서 모임")
            String meetingName,

            @Schema(description = "책 이름", example = "클린 코드")
            String bookName,

            @Schema(description = "시작 일시", example = "2025-02-01T14:00:00")
            LocalDateTime startDateTime,

            @Schema(description = "종료 일시", example = "2025-02-01T16:00:00")
            LocalDateTime endDateTime,

            @Schema(description = "주제 타입 목록", example = "[\"FREE\", \"CHAPTER\"]")
            List<TopicType> topicTypes,

            @Schema(description = "참가 여부", example = "true")
            boolean joined,

            @Schema(description = "약속 상태", example = "CONFIRMED")
            MeetingStatus meetingStatus
    ) {
    }
}

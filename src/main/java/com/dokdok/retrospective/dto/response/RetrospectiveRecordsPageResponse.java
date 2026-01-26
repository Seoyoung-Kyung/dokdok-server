package com.dokdok.retrospective.dto.response;

import com.dokdok.retrospective.entity.PersonalMeetingRetrospective;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
@Schema(description = "개인 회고 목록 페이지 응답")
public record RetrospectiveRecordsPageResponse(
        @Schema(description = "회고 목록")
        List<RetrospectiveRecordResponse> items,

        @Schema(description = "페이지 크기")
        Integer pageSize,

        @Schema(description = "다음 페이지 존재 여부")
        Boolean hasNext,

        @Schema(description = "다음 페이지 커서 (다음 페이지가 없으면 null)")
        NextCursor nextCursor
) {
    @Builder
    @Schema(description = "다음 페이지 조회를 위한 커서")
    public record NextCursor(
            @Schema(description = "마지막 항목의 생성 시간 (정렬 기준)")
            LocalDateTime createdAt,

            @Schema(description = "마지막 항목의 회고 ID (동점 대비)")
            Long retrospectiveId
    ) {
        public static NextCursor from(PersonalMeetingRetrospective retrospective) {
            return NextCursor.builder()
                    .createdAt(retrospective.getCreatedAt())
                    .retrospectiveId(retrospective.getId())
                    .build();
        }
    }

    public static RetrospectiveRecordsPageResponse from(
            List<RetrospectiveRecordResponse> items,
            int pageSize,
            boolean hasNext,
            PersonalMeetingRetrospective lastRetrospective
    ) {
        NextCursor cursor = null;
        if (hasNext && lastRetrospective != null) {
            cursor = NextCursor.from(lastRetrospective);
        }

        return RetrospectiveRecordsPageResponse.builder()
                .items(items)
                .pageSize(pageSize)
                .hasNext(hasNext)
                .nextCursor(cursor)
                .build();
    }
}

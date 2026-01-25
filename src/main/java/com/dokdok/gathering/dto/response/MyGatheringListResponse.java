package com.dokdok.gathering.dto.response;

import com.dokdok.gathering.entity.GatheringMember;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "내 모임 목록 응답")
@Builder
public record MyGatheringListResponse(
        @Schema(description = "모임 목록")
        List<GatheringListItemResponse> items,
        @Schema(description = "페이지 크기", example = "10")
        Integer pageSize,
        @Schema(description = "다음 페이지 존재 여부", example = "true")
        Boolean hasNext,
        @Schema(description = "다음 커서")
        NextCursor nextCursor
) {
    @Schema(description = "커서 정보")
    @Builder
    public record NextCursor(
            @Schema(description = "가입 일시", example = "2025-02-01T10:00:00")
            LocalDateTime joinedAt,
            @Schema(description = "모임 멤버 ID", example = "10")
            Long gatheringMemberId
    ){}

    public static MyGatheringListResponse from(
            List<GatheringListItemResponse> items,
            int pageSize,
            boolean hasNext,
            GatheringMember lastMember
    ) {
        NextCursor cursor = hasNext && lastMember != null
                ? NextCursor.builder()
                .joinedAt(lastMember.getJoinedAt())
                .gatheringMemberId(lastMember.getId())
                .build()
                : null;

        return MyGatheringListResponse.builder()
                .items(items)
                .pageSize(pageSize)
                .hasNext(hasNext)
                .nextCursor(cursor)
                .build();
    }
}

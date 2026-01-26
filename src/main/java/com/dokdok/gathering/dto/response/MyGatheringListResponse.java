package com.dokdok.gathering.dto.response;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "내 모임 전체 목록 커서 응답")
public record MyGatheringListResponse(
        @ArraySchema(
                arraySchema = @Schema(description = "아이템 목록"),
                schema = @Schema(implementation = GatheringListItemResponse.class)
        )
        List<GatheringListItemResponse> items,

        @Schema(description = "페이지 크기", example = "10")
        int pageSize,

        @Schema(description = "다음 페이지 존재 여부", example = "false")
        boolean hasNext,

        @Schema(description = "다음 페이지 커서 (hasNext가 false면 null)", implementation = MyGatheringCursor.class)
        MyGatheringCursor nextCursor
) {
}

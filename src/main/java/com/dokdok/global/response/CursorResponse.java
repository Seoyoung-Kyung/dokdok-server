package com.dokdok.global.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.List;

@Schema(description = "커서 기반 무한스크롤 응답")
@Builder
public record CursorResponse<T, C>(
        @Schema(description = "아이템 목록")
        List<T> items,

        @Schema(description = "페이지 크기", example = "10")
        int pageSize,

        @Schema(description = "다음 페이지 존재 여부", example = "true")
        boolean hasNext,

        @Schema(description = "다음 페이지 커서 (hasNext가 false면 null)")
        C nextCursor
) {
    public static <T, C> CursorResponse<T, C> of(List<T> items, int pageSize, boolean hasNext, C nextCursor) {
        return new CursorResponse<>(items, pageSize, hasNext, hasNext ? nextCursor : null);
    }

    public static <T, C> CursorResponse<T, C> of(List<T> items, int pageSize, C nextCursor) {
        boolean hasNext = nextCursor != null;
        return new CursorResponse<>(items, pageSize, hasNext, nextCursor);
    }
}
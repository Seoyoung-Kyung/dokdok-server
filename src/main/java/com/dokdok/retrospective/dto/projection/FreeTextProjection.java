package com.dokdok.retrospective.dto.projection;

public record FreeTextProjection(
        Long retrospectiveId,
        String title,
        String content
) {
}
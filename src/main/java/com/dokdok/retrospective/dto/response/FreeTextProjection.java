package com.dokdok.retrospective.dto.response;

public record FreeTextProjection(
        Long retrospectiveId,
        String title,
        String content
) {
}
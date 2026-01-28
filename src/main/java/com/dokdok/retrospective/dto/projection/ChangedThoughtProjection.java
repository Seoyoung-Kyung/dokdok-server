package com.dokdok.retrospective.dto.projection;

public record ChangedThoughtProjection(
        Long retrospectiveId,
        Long topicId,
        Integer confirmOrder,
        String keyIssue,
        String postOpinion
) {
}

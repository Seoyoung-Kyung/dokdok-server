package com.dokdok.retrospective.dto.response;

public record ChangedThoughtProjection(
        Long retrospectiveId,
        Long topicId,
        String keyIssue,
        String postOpinion
) {
}

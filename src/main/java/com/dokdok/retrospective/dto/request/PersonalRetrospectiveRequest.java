package com.dokdok.retrospective.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record PersonalRetrospectiveRequest(
        @NotNull(message = "isShared는 필수입니다")
        Boolean isShared,

        @Valid
        List<ChangedThoughtRequest> changedThoughts,

        @Valid
        List<OthersPerspectiveRequest> othersPerspectives,

        @Valid
        List<FreeTextRequest> freeTexts
) {
    public record ChangedThoughtRequest(
            @NotNull(message = "topicId는 필수입니다")
            Long topicId,
            String keyIssue,
            String postOpinion,
            Integer sortOrder
    ) {}

    public record OthersPerspectiveRequest(
            String topicName,
            String opinionContent,
            String impressiveReason,
            Integer sortOrder
    ) {}

    public record FreeTextRequest(
            String title,
            String content,
            Integer sortOrder
    ) {}
}
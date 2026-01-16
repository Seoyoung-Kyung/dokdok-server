package com.dokdok.retrospective.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record PersonalRetrospectiveRequest(

        @Valid
        List<ChangedThoughtRequest> changedThoughts,

        @Valid
        List<OthersPerspectiveRequest> othersPerspectives,

        @Valid
        @Size(max = 10, message = "자유 서술은 최대 10개까지 가능합니다")
        List<FreeTextRequest> freeTexts
) {
    public record ChangedThoughtRequest(
            @NotNull(message = "topicId는 필수입니다")
            Long topicId,

            @Size(max = 200, message = "핵심 쟁점은 200자 이내여야 합니다")
            String keyIssue,

            @Size(max = 10_000, message = "의견은 10,000자 이내여야 합니다")
            String postOpinion
    ) {}

    public record OthersPerspectiveRequest(
            Long topicId,

            @NotNull(message = "meetingMemberId는 필수입니다")
            Long meetingMemberId,

            @Size(max = 10_000, message = "의견 내용은 10,000자 이내여야 합니다")
            String opinionContent,

            @Size(max = 5_000, message = "인상 깊었던 이유는 5,000자 이내여야 합니다")
            String impressiveReason
    ) {}

    public record FreeTextRequest(
            @Size(max = 40, message = "자유 서술 제목은 공백 포함 40자 이내여야 합니다.")
            String title,
            @Size(max = 100_000, message = "자유 서술 내용은 공백 포함 100,000자 이내여야 합니다.")
            String content
    ) {}
}
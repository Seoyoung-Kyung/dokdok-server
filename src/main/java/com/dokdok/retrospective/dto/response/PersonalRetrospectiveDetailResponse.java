package com.dokdok.retrospective.dto.response;

import com.dokdok.retrospective.entity.RetrospectiveChangedThought;
import com.dokdok.retrospective.entity.RetrospectiveFreeText;
import com.dokdok.retrospective.entity.RetrospectiveOthersPerspective;

import java.util.List;

public record PersonalRetrospectiveDetailResponse(
        Long retrospectiveId,
        List<ChangedThought> changedThoughts,
        List<OthersPerspective> othersPerspectives,
        List<FreeText> freeTexts
) {
    public record ChangedThought(
            Long topicId,
            String keyIssue,
            String postOpinion
    ) {

        public static ChangedThought from(RetrospectiveChangedThought changedThought) {
            return new ChangedThought(
                    changedThought.getTopic().getId(),
                    changedThought.getKeyIssue(),
                    changedThought.getPostOpinion()
            );
        }
    }

    public record OthersPerspective(
            Long topicId,
            Long meetingMemberId,
            String opinionContent,
            String impressiveReason
    ) {
        public static OthersPerspective from(RetrospectiveOthersPerspective othersPerspective) {
            return new OthersPerspective(
                    othersPerspective.getTopic().getId(),
                    othersPerspective.getMeetingMember().getId(),
                    othersPerspective.getOpinionContent(),
                    othersPerspective.getImpressiveReason()
            );
        }
    }

    public record FreeText(
            String title,
            String content
    ) {
        public static FreeText from(RetrospectiveFreeText freeText) {
            return new FreeText(
                    freeText.getTitle(),
                    freeText.getContent()
            );
        }
    }

    public static PersonalRetrospectiveDetailResponse from(
            Long retrospectiveId,
            List<ChangedThought> changedThoughts,
            List<OthersPerspective> othersPerspectives,
            List<FreeText> freeTexts
    ) {
        return new PersonalRetrospectiveDetailResponse(
                retrospectiveId,
                changedThoughts,
                othersPerspectives,
                freeTexts
        );
    }

    public static PersonalRetrospectiveDetailResponse fromEntities(
            Long retrospectiveId,
            List<RetrospectiveChangedThought> changedThoughts,
            List<RetrospectiveOthersPerspective> othersPerspectives,
            List<RetrospectiveFreeText> freeTexts
    ) {
        return new PersonalRetrospectiveDetailResponse(
                retrospectiveId,
                changedThoughts.stream().map(ChangedThought::from).toList(),
                othersPerspectives.stream().map(OthersPerspective::from).toList(),
                freeTexts.stream().map(FreeText::from).toList()
        );
    }
}

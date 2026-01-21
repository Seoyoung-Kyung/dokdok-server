package com.dokdok.retrospective.dto.response;

import com.dokdok.book.entity.RecordType;
import com.dokdok.retrospective.entity.RetrospectiveChangedThought;
import com.dokdok.retrospective.entity.RetrospectiveFreeText;
import com.dokdok.retrospective.entity.RetrospectiveOthersPerspective;

import java.time.LocalDateTime;
import java.util.List;

public record RetrospectiveRecordResponse(
        Long retrospectiveId,
        String gatheringName,
        RecordType recordType,
        LocalDateTime createdAt,
        List<ChangedThought> changedThoughts,
        List<OthersPerspective> othersPerspectives,
        List<FreeText> freeTexts
) {
    public record ChangedThought(
            Long topicId,
            String keyIssue,
            String postOpinion
    ) {

        public static ChangedThought from(ChangedThoughtProjection changedThought) {
            return new ChangedThought(
                    changedThought.topicId(),
                    changedThought.keyIssue(),
                    changedThought.postOpinion()
            );
        }
    }

    public record OthersPerspective(
            Long topicId,
            Long meetingMemberId,
            String memberNickname,
            String opinionContent,
            String impressiveReason
    ) {
        public static OthersPerspective from(OtherPerspectiveProjection othersPerspective) {
            return new OthersPerspective(
                    othersPerspective.topicId(),
                    othersPerspective.meetingMemberId(),
                    othersPerspective.memberNickname(),
                    othersPerspective.opinionContent(),
                    othersPerspective.impressiveReason()
            );
        }
    }

    public record FreeText(
            String title,
            String content
    ) {
        public static FreeText from(FreeTextProjection freeText) {
            return new FreeText(
                    freeText.title(),
                    freeText.content()
            );
        }
    }

    public static RetrospectiveRecordResponse of(
            Long retrospectiveId,
            String gatheringName,
            RecordType recordType,
            LocalDateTime createdAt,
            List<RetrospectiveRecordResponse.ChangedThought> changedThoughts,
            List<RetrospectiveRecordResponse.OthersPerspective> othersPerspectives,
            List<RetrospectiveRecordResponse.FreeText> freeTexts
    ) {
        return new RetrospectiveRecordResponse(
                retrospectiveId,
                gatheringName,
                recordType,
                createdAt,
                changedThoughts,
                othersPerspectives,
                freeTexts
        );
    }
}

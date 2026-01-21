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

    public static RetrospectiveRecordResponse ofEntities(
            Long retrospectiveId,
            String gatheringName,
            RecordType recordType,
            LocalDateTime createdAt,
            List<RetrospectiveChangedThought> changedThoughts,
            List<RetrospectiveOthersPerspective> othersPerspectives,
            List<RetrospectiveFreeText> freeTexts
    ) {
        return new RetrospectiveRecordResponse(
                retrospectiveId,
                gatheringName,
                recordType,
                createdAt,
                changedThoughts.stream().map(ChangedThought::from).toList(),
                othersPerspectives.stream().map(OthersPerspective::from).toList(),
                freeTexts.stream().map(FreeText::from).toList()
        );
    }
}

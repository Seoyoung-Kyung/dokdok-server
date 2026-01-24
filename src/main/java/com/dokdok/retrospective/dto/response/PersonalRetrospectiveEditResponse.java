package com.dokdok.retrospective.dto.response;

import com.dokdok.retrospective.entity.RetrospectiveChangedThought;
import com.dokdok.retrospective.entity.RetrospectiveFreeText;
import com.dokdok.retrospective.entity.RetrospectiveOthersPerspective;

import java.util.List;

public record PersonalRetrospectiveEditResponse(
        Long retrospectiveId,
        RetrospectiveData retrospective,  // 작성 데이터 묶음
        List<TopicInfo> topics,
        List<MemberInfo> meetingMembers
) {

    public record RetrospectiveData(
            List<ChangedThought> changedThoughts,
            List<OthersPerspective> othersPerspectives,
            List<FreeText> freeTexts
    ) {}

    public record ChangedThought(
            Long topicId,
            String keyIssue,
            String preOpinion,
            String postOpinion
    ) {
        public static ChangedThought from(RetrospectiveChangedThought changedThought) {
            return new ChangedThought(
                    changedThought.getTopic().getId(),
                    changedThought.getKeyIssue(),
                    changedThought.getPreOpinion(),
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

    public static PersonalRetrospectiveEditResponse from(
            Long retrospectiveId,
            List<ChangedThought> changedThoughts,
            List<OthersPerspective> othersPerspectives,
            List<FreeText> freeTexts,
            List<TopicInfo> topics,
            List<MemberInfo> meetingMembers
    ) {
        return new PersonalRetrospectiveEditResponse(
                retrospectiveId,
                new RetrospectiveData(changedThoughts, othersPerspectives, freeTexts), // 묶어서 전달
                topics,
                meetingMembers
        );
    }
}

package com.dokdok.retrospective.dto.response;

public record OtherPerspectiveProjection(
        Long retrospectiveId,
        Long topicId,
        Long meetingMemberId,
        String memberNickname,
        String opinionContent,
        String impressiveReason
) {
}

package com.dokdok.retrospective.dto.projection;

public record OtherPerspectiveProjection(
        Long retrospectiveId,
        Long topicId,
        Integer confirmOrder,
        Long meetingMemberId,
        String memberNickname,
        String opinionContent,
        String impressiveReason
) {
}

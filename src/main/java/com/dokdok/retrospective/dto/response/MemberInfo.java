package com.dokdok.retrospective.dto.response;

public record MemberInfo(
        Long meetingMemberId,
        String nickname,
        String profileImageUrl
) {
    public static MemberInfo of(Long meetingMemberId, String nickname, String profileImageUrl) {
        return new MemberInfo(
                meetingMemberId,
                nickname,
                profileImageUrl
        );
    }
}
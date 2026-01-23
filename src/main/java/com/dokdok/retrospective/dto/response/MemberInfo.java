package com.dokdok.retrospective.dto.response;

public record MemberInfo(
        Long meetingMemberId,
        String nickname,
        String profileImage
) {
    public static MemberInfo of(Long meetingMemberId, String nickname, String profileImage) {
        return new MemberInfo(
                meetingMemberId,
                nickname,
                profileImage
        );
    }
}
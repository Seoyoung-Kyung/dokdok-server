package com.dokdok.user.dto.response;

import com.dokdok.user.entity.User;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record UserInfoResponseDto(
        Long userId,
        String nickname,
        String profileImageUrl,
        Boolean needsOnboarding
) {
    public static UserInfoResponseDto from(User user) {
        boolean needsOnboarding = user.getNickname() == null || user.getNickname().isBlank();

        return UserInfoResponseDto.builder()
                .userId(user.getId())
                .nickname(user.getNickname())
                .profileImageUrl(user.getProfileImageUrl())
                .needsOnboarding(needsOnboarding ? true : null)  // 온보딩 필요한 경우만 포함
                .build();
    }
}
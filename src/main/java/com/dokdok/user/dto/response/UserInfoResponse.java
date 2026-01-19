package com.dokdok.user.dto.response;

import com.dokdok.user.entity.User;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record UserInfoResponse(
        Long userId,
        String nickname,
        Boolean needsOnboarding
) {
    public static UserInfoResponse from(User user) {
        boolean needsOnboarding = user.getNickname() == null || user.getNickname().isBlank();

        return UserInfoResponse.builder()
                .userId(user.getId())
                .nickname(user.getNickname())
                .needsOnboarding(needsOnboarding ? true : null)  // 온보딩 필요한 경우만 포함
                .build();
    }
}
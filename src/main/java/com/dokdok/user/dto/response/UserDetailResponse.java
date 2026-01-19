package com.dokdok.user.dto.response;

import com.dokdok.user.entity.User;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record UserDetailResponse(
        Long userId,
        String nickname,
        String email,
        String profileImageUrl,
        LocalDateTime createdAt
)
{
    public static UserDetailResponse from(User user) {
        return UserDetailResponse.builder()
                .userId(user.getId())
                .nickname(user.getNickname())
                .email(user.getUserEmail())
                .profileImageUrl(user.getProfileImageUrl())
                .createdAt(user.getCreatedAt())
                .build();
    }

    public static UserDetailResponse from(User user, String presignedProfileImage) {
        return UserDetailResponse.builder()
                .userId(user.getId())
                .nickname(user.getNickname())
                .email(user.getUserEmail())
                .profileImageUrl(presignedProfileImage)
                .createdAt(user.getCreatedAt())
                .build();
    }
}

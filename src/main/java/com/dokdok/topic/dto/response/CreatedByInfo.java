package com.dokdok.topic.dto.response;

public record CreatedByInfo(
        Long userId,
        String nickname
) {

    public static CreatedByInfo of(Long userId, String nickname) {
        return new CreatedByInfo(userId, nickname);
    }

}
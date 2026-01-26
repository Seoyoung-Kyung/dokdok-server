package com.dokdok.topic.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "주제 제안자 정보")
public record CreatedByInfo(
        @Schema(description = "사용자 ID", example = "5")
        Long userId,
        @Schema(description = "닉네임", example = "독서왕")
        String nickname
) {

    public static CreatedByInfo of(Long userId, String nickname) {
        return new CreatedByInfo(userId, nickname);
    }

}

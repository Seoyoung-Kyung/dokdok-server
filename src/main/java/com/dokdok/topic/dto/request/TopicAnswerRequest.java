package com.dokdok.topic.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TopicAnswerRequest(
        @NotBlank(message = "설명은 필수입니다")
        @Size(max = 1000, message = "설명은 1000자 이내여야 합니다")
        String content
) {
}

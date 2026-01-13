package com.dokdok.topic.dto.request;

import jakarta.validation.constraints.NotBlank;

public record TopicAnswerRequest(
        @NotBlank String content
) {
}

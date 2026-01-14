package com.dokdok.topic.dto.request;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record ConfirmTopicsRequest(
        @NotEmpty(message = "topicIds는 필수입니다")
        List<Long> topicIds
) {
}

package com.dokdok.topic.dto.request;

import com.dokdok.topic.entity.TopicType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SuggestTopicRequest(

        @NotBlank(message = "제목은 필수입니다")
        @Size(max = 100, message = "제목은 100자 이내여야 합니다")
        String title,

        @NotBlank(message = "설명은 필수입니다")
        @Size(max = 1000, message = "설명은 1000자 이내여야 합니다")
        String description,

        @NotNull(message = "주제 타입은 필수입니다")
        TopicType topicType
) {
}
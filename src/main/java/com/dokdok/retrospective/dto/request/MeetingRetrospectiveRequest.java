package com.dokdok.retrospective.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record MeetingRetrospectiveRequest(

        @NotNull
        Long topicId,

        @NotBlank
        String comment
) { }

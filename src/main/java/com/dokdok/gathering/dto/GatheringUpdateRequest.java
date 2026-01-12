package com.dokdok.gathering.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import org.hibernate.validator.constraints.Length;

@Builder
public record GatheringUpdateRequest(
        @NotBlank(message = "모임명은 공백일 수 없습니다.")
        @Length(max=255, message = "모임며은 255자를 초과할 수 없습니다.")
        String gatheringName,
        String description
) {
}

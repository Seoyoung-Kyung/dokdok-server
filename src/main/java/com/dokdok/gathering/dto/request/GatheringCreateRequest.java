package com.dokdok.gathering.dto.request;

import jakarta.validation.constraints.NotBlank;

public record GatheringCreateRequest(
        @NotBlank(message = "모임의 이름은 필수입력입니다.")
        String gatheringName,
        String gatheringDescription
) {
}

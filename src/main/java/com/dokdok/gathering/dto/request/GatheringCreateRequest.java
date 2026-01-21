package com.dokdok.gathering.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record GatheringCreateRequest(
        @NotBlank(message = "모임의 이름은 필수입력입니다.")
        @Size(max = 12, message = "모임 이름은 12자 이내로 입력해주세요.")
        String gatheringName,

        @Size(max = 150, message = "모임 설명은 150자 이내로 입력해주세요.")
        String gatheringDescription
) {
}

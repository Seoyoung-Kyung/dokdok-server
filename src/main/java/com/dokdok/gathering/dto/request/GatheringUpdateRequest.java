package com.dokdok.gathering.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import org.hibernate.validator.constraints.Length;

@Builder
public record GatheringUpdateRequest(
        @NotNull(message = "모임명은 필수입니다")
        @Pattern(regexp = "^(?!\\s*$).{0,12}$",
                message = "모임명은 공백만 포함할 수 없으며 12자를 초과할 수 없습니다")
        String gatheringName,
        @Length(max=150, message = "모임 설명은 150자를 초과할 수 없습니다.")
        String description
) {
}

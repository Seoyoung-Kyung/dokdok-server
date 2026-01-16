package com.dokdok.gathering.dto.request;

import com.dokdok.gathering.entity.GatheringMemberStatus;
import jakarta.validation.constraints.NotNull;

public record JoinGatheringMemberRequest(

        @NotNull(message = "승인상태는 빈 값일 수 없습니다.")
        GatheringMemberStatus approve_type
) {
}

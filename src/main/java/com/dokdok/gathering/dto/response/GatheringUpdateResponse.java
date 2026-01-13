package com.dokdok.gathering.dto;

import com.dokdok.gathering.entity.Gathering;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record GatheringUpdateResponse(
        Long gatheringId,
        String gatheringName,
        String description,
        LocalDateTime updatedAt
) {
    public static GatheringUpdateResponse from(Gathering gathering){
        return GatheringUpdateResponse.builder()
                .gatheringId(gathering.getId())
                .gatheringName(gathering.getGatheringName())
                .description(gathering.getDescription())
                .updatedAt(gathering.getUpdatedAt())
                .build();
    }
}

package com.dokdok.gathering.dto;

import com.dokdok.gathering.entity.GatheringMember;
import lombok.Builder;

@Builder
public record GatheringSimpleResponse(
        Long gatheringId,
        String gatheringName,
        Boolean isFavorite,
        String gatheringStatus,
        Integer totalMembers,
        Integer totalMeetings,      // TODO : Meeting 도메인 구현 후 실제 값으로 대체 필요
        String currentUserRole,
        Integer daysFromJoined
) {
    public static GatheringSimpleResponse from(
            GatheringMember gatheringMember,
            Integer totalMembers
    ){
        return GatheringSimpleResponse.builder()
                .gatheringId(gatheringMember.getGathering().getId())
                .gatheringName(gatheringMember.getGathering().getGatheringName())
                .isFavorite(gatheringMember.getIsFavorite())
                .gatheringStatus(gatheringMember.getGathering().getGatheringStatus())
                .totalMembers(totalMembers)
                .totalMeetings(0) // TODO : Meeting 도메인 구현 필요
                .currentUserRole(gatheringMember.getRole())
                .daysFromJoined(gatheringMember.getDaysFromJoined())
                .build();
    }
}

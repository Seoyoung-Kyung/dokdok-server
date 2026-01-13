package com.dokdok.gathering.dto;

import com.dokdok.gathering.entity.GatheringMember;
import com.dokdok.gathering.entity.GatheringRole;
import com.dokdok.gathering.entity.GatheringStatus;
import lombok.Builder;

@Builder
public record GatheringSimpleResponse(
        Long gatheringId,
        String gatheringName,
        Boolean isFavorite,
        GatheringStatus gatheringStatus,
        Integer totalMembers,
        Integer totalMeetings,
        GatheringRole currentUserRole,
        Integer daysFromJoined
) {
    public static GatheringSimpleResponse from(
            GatheringMember gatheringMember,
            int totalMembers,
            int totalMeetings,
            GatheringRole currentUserRole
    ){
        return GatheringSimpleResponse.builder()
                .gatheringId(gatheringMember.getGathering().getId())
                .gatheringName(gatheringMember.getGathering().getGatheringName())
                .isFavorite(gatheringMember.getIsFavorite())
                .gatheringStatus(gatheringMember.getGathering().getGatheringStatus())
                .totalMembers(totalMembers)
                .totalMeetings(totalMeetings)
                .currentUserRole(currentUserRole)
                .daysFromJoined(gatheringMember.getDaysFromJoined())
                .build();
    }
}

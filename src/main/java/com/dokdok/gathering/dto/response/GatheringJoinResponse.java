package com.dokdok.gathering.dto.response;

import com.dokdok.gathering.entity.GatheringMember;
import com.dokdok.gathering.entity.GatheringMemberStatus;
import lombok.Builder;

@Builder
public record GatheringJoinResponse(
        Long gatheringId,
        String gatheringName,
        GatheringMemberStatus memberStatus
) {
    public static GatheringJoinResponse from(GatheringMember member) {
        return GatheringJoinResponse.builder()
                .gatheringId(member.getGathering().getId())
                .gatheringName(member.getGathering().getGatheringName())
                .memberStatus(member.getMemberStatus())
                .build();
    }
}
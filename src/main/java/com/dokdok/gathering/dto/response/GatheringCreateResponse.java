package com.dokdok.gathering.dto.response;

import com.dokdok.gathering.entity.Gathering;
import lombok.Builder;

@Builder
public record GatheringCreateResponse(
        String gatheringName,
        Integer totalMembers,
        Integer daysFromCreation,
        Integer totalMeetings,
        String invitationLink
) {
    public static GatheringCreateResponse from(Gathering gathering) {
        return GatheringCreateResponse.builder()
                .gatheringName(gathering.getGatheringName())
                .totalMembers(1)
                .daysFromCreation(gathering.getDaysFromCreation())
                .totalMeetings(1)
                .invitationLink(gathering.getInvitationLink())
                .build();
    }
}

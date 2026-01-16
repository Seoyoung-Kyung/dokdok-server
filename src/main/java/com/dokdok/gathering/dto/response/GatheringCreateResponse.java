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
    public static GatheringCreateResponse from(Gathering gathering, int activeMembers, int totalMeetings) {
        return GatheringCreateResponse.builder()
                .gatheringName(gathering.getGatheringName())
                .totalMembers(activeMembers)
                .daysFromCreation(gathering.getDaysFromCreation())
                .totalMeetings(totalMeetings)
                .invitationLink(gathering.getInvitationLink())
                .build();
    }
}

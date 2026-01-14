package com.dokdok.gathering.dto;

import com.dokdok.gathering.entity.Gathering;
import com.dokdok.gathering.entity.GatheringMember;
import com.dokdok.gathering.entity.GatheringRole;
import com.dokdok.gathering.entity.GatheringStatus;
import lombok.Builder;

import java.util.List;

@Builder
public record GatheringDetailResponse(
        Long gatheringId,
        String gatheringName,
        String description,
        GatheringStatus gatheringStatus,
        String invitationLink,
        Integer daysFromCreation,
        GatheringRole currentUserRole,
        List<MemberInfo> members,
        Integer totalMembers
) {
    public static GatheringDetailResponse from(
            GatheringMember currentMember,
            List<GatheringMember> allMember
    ){
        List<MemberInfo> memberInfoList = allMember.stream()
                .map(MemberInfo::from)
                .toList();

        Gathering gathering = currentMember.getGathering();

        return GatheringDetailResponse.builder()
                .gatheringId(gathering.getId())
                .gatheringName(gathering.getGatheringName())
                .description(gathering.getDescription())
                .gatheringStatus(gathering.getGatheringStatus())
                .invitationLink(gathering.getInvitationLink())
                .daysFromCreation(gathering.getDaysFromCreation())
                .currentUserRole(currentMember.getRole())
                .members(memberInfoList)
                .totalMembers(allMember.size())
                .build();
    }

    @Builder
    public record MemberInfo(
            Long gatheringMemberId,
            Long userId,
            String nickname,
            String profileImageUrl,
            GatheringRole role
    ){
        public static MemberInfo from(GatheringMember member){
            if(member == null){
                return null;
            }
            return MemberInfo.builder()
                    .gatheringMemberId(member.getId())
                    .userId(member.getUser().getId())
                    .nickname(member.getUser().getNickname())
                    .profileImageUrl(member.getUser().getProfileImageUrl())
                    .role(member.getRole())
                    .build();
        }
    }
}

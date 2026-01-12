package com.dokdok.gathering.service;

import com.dokdok.gathering.entity.GatheringMember;
import com.dokdok.gathering.entity.GatheringRole;
import com.dokdok.gathering.exception.GatheringErrorCode;
import com.dokdok.gathering.exception.GatheringException;
import com.dokdok.gathering.repository.GatheringMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GatheringValidator {

    private final GatheringMemberRepository gatheringMemberRepository;

    public void validateMembership(Long gatheringId, Long userId) {
        boolean isMember = gatheringMemberRepository
                .existsByGatheringIdAndUserIdAndRemovedAtIsNull(gatheringId, userId);

        if (!isMember) {
            throw new GatheringException(GatheringErrorCode.NOT_GATHERING_MEMBER);
        }
    }

    public void validateLeader(Long gatheringId, Long userId){
        GatheringMember member = gatheringMemberRepository
                .findByGatheringIdAndUserId(gatheringId,userId)
                .orElseThrow(()-> new GatheringException(GatheringErrorCode.NOT_GATHERING_MEMBER));

        if(member.getRole() != GatheringRole.LEADER){
            throw new GatheringException(GatheringErrorCode.NOT_GATHERING_LEADER);
        }
    }

}
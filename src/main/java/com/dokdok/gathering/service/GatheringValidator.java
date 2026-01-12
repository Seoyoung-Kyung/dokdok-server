package com.dokdok.gathering.service;

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

}
package com.dokdok.gathering.service;

import com.dokdok.gathering.repository.GatheringMemberRepository;
import com.dokdok.global.exception.GlobalErrorCode;
import com.dokdok.global.exception.GlobalException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GatheringMemberValidator {

    private final GatheringMemberRepository gatheringMemberRepository;

    public void validateMembership(Long gatheringId, Long userId) {
        boolean isMember = gatheringMemberRepository
                .existsByGatheringIdAndUserIdAndRemovedAtIsNull(gatheringId, userId);

        if (!isMember) {
            throw new GlobalException(GlobalErrorCode.NOT_GATHERING_MEMBER);
        }
    }
}

package com.dokdok.gathering.service;

import com.dokdok.gathering.repository.GatheringMemberRepository;
import com.dokdok.global.exception.GlobalErrorCode;
import com.dokdok.global.exception.GlobalException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GatheringMemberService {

    private final GatheringMemberRepository gatheringMemberRepository;

    public void validateMembership(Long gatheringId, Long userId) {
        boolean isMember = gatheringMemberRepository
                .existsByGatheringIdAndUserIdAndRemovedAtIsNull(gatheringId, userId);

        // TODO: 예의 코드 상의 후 수정 예정
        if (!isMember) {
            throw new GlobalException(GlobalErrorCode.NOT_GATHERING_MEMBER);
        }
    }
}

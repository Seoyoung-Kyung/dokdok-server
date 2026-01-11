package com.dokdok.gathering.service;

import com.dokdok.gathering.dto.GatheringSimpleResponse;
import com.dokdok.gathering.dto.MyGatheringListResponse;
import com.dokdok.gathering.entity.GatheringMember;
import com.dokdok.gathering.repository.GatheringMemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GatheringService {

    private final GatheringMemberRepository gatheringMemberRepository;

    public MyGatheringListResponse getMyGatherings(Long userId, Pageable pageable){

        Page<GatheringMember> gatheringMemberPage = gatheringMemberRepository.findActiveGatheringsByUserId(userId, pageable);

        List<GatheringSimpleResponse> gatheringResponses = gatheringMemberPage.getContent()
                .stream()
                .map(gatheringMember -> {
                    Integer totalMembers = gatheringMemberRepository.countActiveMembers(gatheringMember.getGathering().getId());

                    return GatheringSimpleResponse.from(gatheringMember, totalMembers);
                })
                .collect(Collectors.toUnmodifiableList());

        return MyGatheringListResponse.from(
                gatheringResponses,
                gatheringMemberPage
        );
    }
}

package com.dokdok.gathering.service;

import com.dokdok.gathering.dto.GatheringDetailResponse;
import com.dokdok.gathering.dto.GatheringSimpleResponse;
import com.dokdok.gathering.dto.MyGatheringListResponse;
import com.dokdok.gathering.entity.Gathering;
import com.dokdok.gathering.entity.GatheringMember;
import com.dokdok.gathering.exception.GatheringErrorCode;
import com.dokdok.gathering.exception.GatheringException;
import com.dokdok.gathering.repository.GatheringMemberRepository;
import com.dokdok.gathering.repository.GatheringRepository;
import com.dokdok.global.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
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
    private final GatheringRepository gatheringRepository;

    public MyGatheringListResponse getMyGatherings(Pageable pageable){
        Long userId = SecurityUtil.getCurrentUserId();

        Page<GatheringMember> gatheringMemberPage = gatheringMemberRepository.findActiveGatheringsByUserId(userId, pageable);

        List<GatheringSimpleResponse> gatheringResponses = gatheringMemberPage.getContent()
                .stream()
                .map(gatheringMember -> {
                    Integer totalMembers = gatheringMemberRepository.countActiveMembers(gatheringMember.getGathering().getId());

                    return GatheringSimpleResponse.from(gatheringMember, totalMembers,gatheringMember.getRole());
                })
                .collect(Collectors.toList());

        return MyGatheringListResponse.from(
                gatheringResponses,
                gatheringMemberPage
        );
    }

    /**
     * 모임 상세 정보 조회 - 모임 멤버만 조회 가능
     */
    public GatheringDetailResponse getGatheringDetail(Long gatheringId){
        Long userId = SecurityUtil.getCurrentUserId();

        // 모임 존재 여부 확인
        Gathering gathering = gatheringRepository.findById(gatheringId)
                .orElseThrow(()-> new GatheringException(GatheringErrorCode.GATHERING_NOT_FOUND));

        // 해당 유저가 모임 멤버인지 확인
        GatheringMember currentMember = gatheringMemberRepository
                .findByGatheringIdAndUserId(gatheringId,userId)
                .orElseThrow(()-> new GatheringException(GatheringErrorCode.NOT_GATHERING_MEMBER));

        // 모임의 모든 멤버 조회
        List<GatheringMember> allMembers = gatheringMemberRepository.findAllMembersByGatheringId(gatheringId);

        return GatheringDetailResponse.from(
                currentMember.getGathering(),
                currentMember.getRole(),
                allMembers
        );
    }
}

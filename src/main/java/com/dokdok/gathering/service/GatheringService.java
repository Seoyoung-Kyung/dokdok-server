package com.dokdok.gathering.service;

import com.dokdok.gathering.dto.*;
import com.dokdok.gathering.entity.Gathering;
import com.dokdok.gathering.entity.GatheringMember;
import com.dokdok.gathering.repository.GatheringMemberRepository;
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
	private final GatheringValidator gatheringValidator;

	public MyGatheringListResponse getMyGatherings(Pageable pageable) {
		Long userId = SecurityUtil.getCurrentUserId();

		Page<GatheringMember> gatheringMemberPage = gatheringMemberRepository.findActiveGatheringsByUserId(userId, pageable);

		List<GatheringSimpleResponse> gatheringResponses = gatheringMemberPage.getContent()
				.stream()
				.map(gatheringMember -> {
					Integer totalMembers = gatheringMemberRepository.countActiveMembers(gatheringMember.getGathering().getId());

					return GatheringSimpleResponse.from(gatheringMember, totalMembers, gatheringMember.getRole());
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
	public GatheringDetailResponse getGatheringDetail(Long gatheringId) {
		Long userId = SecurityUtil.getCurrentUserId();

		// 모임 존재 여부 및 멤버십 검증
		gatheringValidator.validateAndGetGathering(gatheringId);
		GatheringMember currentMember = gatheringValidator.validateAndGetMember(gatheringId, userId);

		// 모임의 모든 멤버 조회
		List<GatheringMember> allMember = gatheringMemberRepository.findAllMembersByGatheringId(gatheringId);

		return GatheringDetailResponse.from(
				currentMember,
				allMember
		);
	}

	/**
	 * 모임 정보 수정 - 리더만 가능
	 */
	@Transactional
	public GatheringUpdateResponse updateGathering(Long gatheringId, GatheringUpdateRequest request) {
		Long currentUserId = SecurityUtil.getCurrentUserId();

		// 모임 존재 여부 및 리더 권한 검증
		Gathering gathering = gatheringValidator.validateAndGetGathering(gatheringId);
		gatheringValidator.validateLeader(gatheringId, currentUserId);

		// 모임 정보 수정
		gathering.updateGatheringInfo(request.gatheringName(), request.description());

		return GatheringUpdateResponse.from(gathering);
	}
}

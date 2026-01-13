package com.dokdok.gathering.service;

import com.dokdok.gathering.entity.Gathering;
import com.dokdok.gathering.entity.GatheringMember;
import com.dokdok.gathering.entity.GatheringRole;
import com.dokdok.gathering.exception.GatheringErrorCode;
import com.dokdok.gathering.exception.GatheringException;
import com.dokdok.gathering.repository.GatheringMemberRepository;
import com.dokdok.gathering.repository.GatheringRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GatheringValidator {

	private final GatheringMemberRepository gatheringMemberRepository;
	private final GatheringRepository gatheringRepository;

	public void validateMembership(Long gatheringId, Long userId) {
		boolean isMember = gatheringMemberRepository
				.existsByGatheringIdAndUserIdAndRemovedAtIsNull(gatheringId, userId);

		if (!isMember) {
			throw new GatheringException(GatheringErrorCode.NOT_GATHERING_MEMBER);
		}
	}

	public void validateLeader(Long gatheringId, Long userId) {
		GatheringMember member = gatheringMemberRepository
				.findByGatheringIdAndUserId(gatheringId, userId)
				.orElseThrow(() -> new GatheringException(GatheringErrorCode.NOT_GATHERING_MEMBER));

		if (member.getRole() != GatheringRole.LEADER) {
			throw new GatheringException(GatheringErrorCode.NOT_GATHERING_LEADER);
		}
	}

	/**
	 * 모임 존재 여부를 검증하고 Gathering을 반환합니다.
	 */
	public Gathering validateAndGetGathering(Long gatheringId) {
		return gatheringRepository.findById(gatheringId)
				.orElseThrow(() -> new GatheringException(GatheringErrorCode.GATHERING_NOT_FOUND));
	}

	/**
	 * 멤버십을 검증하고 GatheringMember를 반환합니다.
	 */
	public GatheringMember validateAndGetMember(Long gatheringId, Long userId) {
		return gatheringMemberRepository
				.findByGatheringIdAndUserId(gatheringId, userId)
				.orElseThrow(() -> new GatheringException(GatheringErrorCode.NOT_GATHERING_MEMBER));
	}

}
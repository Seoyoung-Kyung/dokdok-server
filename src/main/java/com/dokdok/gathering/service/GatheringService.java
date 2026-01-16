package com.dokdok.gathering.service;

import com.dokdok.gathering.dto.request.GatheringCreateRequest;
import com.dokdok.gathering.dto.request.GatheringUpdateRequest;
import com.dokdok.gathering.dto.request.JoinGatheringMemberRequest;
import com.dokdok.gathering.dto.response.*;
import com.dokdok.gathering.entity.*;
import com.dokdok.gathering.exception.GatheringErrorCode;
import com.dokdok.gathering.exception.GatheringException;
import com.dokdok.gathering.repository.GatheringMemberRepository;
import com.dokdok.gathering.repository.GatheringRepository;
import com.dokdok.global.util.SecurityUtil;
import com.dokdok.gathering.util.InvitationCodeGenerator;
import com.dokdok.meeting.entity.MeetingStatus;
import com.dokdok.meeting.repository.MeetingRepository;
import com.dokdok.user.entity.User;
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

    private final GatheringRepository gatheringRepository;
	private final GatheringMemberRepository gatheringMemberRepository;
	private final GatheringValidator gatheringValidator;
	private final MeetingRepository meetingRepository;

    /**
     * 모임을 생성합니다.
     * 모임 생성을 요청하는 사용자가 해당 모임의 모임장이 됩니다.
     */
    @Transactional
    public GatheringCreateResponse createGathering(GatheringCreateRequest request) {

        User user = SecurityUtil.getCurrentUserEntity();
        String invitationLink = generateUniqueInvitationCode();

        Gathering gathering = Gathering.of(request.gatheringName(), request.gatheringDescription(), invitationLink, user);
        Gathering savedGathering = gatheringRepository.save(gathering);

        saveGatheringMember(savedGathering, user, GatheringRole.LEADER, GatheringMemberStatus.ACTIVE);

        return GatheringCreateResponse.from(savedGathering, getActiveMemberCount(savedGathering.getId()));
    }

    /**
     * 초대링크로 진입한 모임의 정보를 Summery정보를 보여줍니다.
     */
    public GatheringCreateResponse getJoinGatheringInfo(String invitationLink) {

        Gathering gathering = gatheringValidator.validateInvitationLink(invitationLink);
        return GatheringCreateResponse.from(gathering, getActiveMemberCount(gathering.getId()));
    }

    /**
     * 초대링크를 통해 들어온 사용자가 모임에 가입 요청을 합니다.
     */
    @Transactional
    public GatheringJoinResponse joinGathering(String invitationLink) {

        User user = SecurityUtil.getCurrentUserEntity();

        Gathering gathering = gatheringValidator.validateInvitationLink(invitationLink);
        gatheringValidator.validateJoinedGathering(gathering.getId(), user.getId());

        GatheringMember member = saveGatheringMember(gathering, user, GatheringRole.MEMBER, GatheringMemberStatus.PENDING);

        return GatheringJoinResponse.from(member);
    }

    /**
     * 모임장이 가입 요청을 한 멤버에 대해 승인|거절을 처리합니다.
     */
    @Transactional
    public void handleJoinRequest(Long gatheringId, Long memberId, JoinGatheringMemberRequest request) {

        User user = SecurityUtil.getCurrentUserEntity();
        gatheringValidator.validateLeader(gatheringId, user.getId());

        GatheringMemberStatus approveType = request.approve_type();
        if (approveType == GatheringMemberStatus.PENDING) {
            throw new GatheringException(GatheringErrorCode.INVALID_APPROVE_TYPE);
        }

        GatheringMember gatheringMember = gatheringMemberRepository.findByGatheringIdAndUserId(gatheringId, memberId)
                .orElseThrow(() -> new GatheringException(GatheringErrorCode.NOT_GATHERING_MEMBER));

        if (gatheringMember.getMemberStatus() != GatheringMemberStatus.PENDING) {
            throw new GatheringException(GatheringErrorCode.NOT_PENDING_STATUS);
        }

        gatheringMember.handleJoinRequest(approveType);
    }

	public MyGatheringListResponse getMyGatherings(Pageable pageable) {
		Long userId = SecurityUtil.getCurrentUserId();

		Page<GatheringMember> gatheringMemberPage = gatheringMemberRepository.findActiveGatheringsByUserId(userId, pageable);

		List<GatheringSimpleResponse> gatheringResponses = gatheringMemberPage.getContent()
				.stream()
				.map(gatheringMember -> {
					int totalMembers = gatheringMemberRepository.countActiveMembers(gatheringMember.getGathering().getId());
					int totalMeetings = meetingRepository.countByGatheringIdAndMeetingStatus(gatheringMember.getGathering().getId(), MeetingStatus.DONE);

					return GatheringSimpleResponse.from(gatheringMember, totalMembers,totalMeetings, gatheringMember.getRole());
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

	// 모임 삭제 - 리더만 가능
	@Transactional
	public void deleteGathering(Long gatheringId) {
		Long userId = SecurityUtil.getCurrentUserId();

		// 모임 존재 여부 & 리더 권한 검증
		Gathering gathering = gatheringValidator.validateAndGetGathering(gatheringId);
		gatheringValidator.validateLeader(gatheringId, userId);

		if (gathering.getGatheringStatus().equals(GatheringStatus.INACTIVE)) {
			throw new GatheringException(GatheringErrorCode.ALREADY_INACTIVE);
		}
		gathering.deleteGathering();
	}

	// 모임원 탈퇴 - 리더만 가능
	@Transactional
	public void removeMember(Long gatheringId, Long targetUserId) {
		Long requestId = SecurityUtil.getCurrentUserId();

		// 모임 존재 여부
		gatheringValidator.validateAndGetGathering(gatheringId);

		// 권한이 리더인지 검증
		gatheringValidator.validateLeader(gatheringId,requestId);

		// 강퇴 대상 멤버 조회 & 존재여부
		GatheringMember targetMember = gatheringValidator.validateAndGetMember(gatheringId, targetUserId);

		// 강퇴 대상이 리더인지 확인
		if (targetMember.getRole() == GatheringRole.LEADER) {
			throw new GatheringException(GatheringErrorCode.CANNOT_REMOVE_LEADER);
		}

		targetMember.remove();
	}

	/**
	 * 중복되지 않는 초대 코드를 생성합니다.
	 * 최대 10번 재시도하며, 실패 시 예외를 발생시킵니다.
	 */
	private String generateUniqueInvitationCode() {
		int maxRetries = 10;

		for (int i = 0; i < maxRetries; i++) {
			String code = InvitationCodeGenerator.generate();
			if (!gatheringRepository.existsByInvitationLink(code)) {
				return code;
			}
		}

		throw new GatheringException(GatheringErrorCode.INVITATION_CODE_GENERATION_FAILED);
	}

    /**
     * 공통 메서드
     * 모임 멤버를 추가합니다.
     */
    private GatheringMember saveGatheringMember(Gathering gathering,  User user, GatheringRole role, GatheringMemberStatus status) {

        GatheringMember gatheringMember = GatheringMember.of(gathering, user, role, status);
        return gatheringMemberRepository.save(gatheringMember);
    }

    /**
     * 공통 메서드
     * ACTIVE 상태인 모임 멤버 수를 조회합니다.
     */
    private int getActiveMemberCount(Long gatheringId) {
        return gatheringMemberRepository.countActiveMembersByStatus(gatheringId);
    }
}

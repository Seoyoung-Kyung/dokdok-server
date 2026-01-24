package com.dokdok.gathering.service;

import com.dokdok.gathering.dto.request.GatheringCreateRequest;
import com.dokdok.gathering.dto.request.JoinGatheringMemberRequest;
import com.dokdok.gathering.dto.response.GatheringCreateResponse;
import com.dokdok.gathering.dto.response.GatheringDetailResponse;
import com.dokdok.gathering.dto.response.GatheringJoinResponse;
import com.dokdok.gathering.dto.response.GatheringListItemResponse;
import com.dokdok.gathering.dto.request.GatheringUpdateRequest;
import com.dokdok.gathering.dto.response.GatheringUpdateResponse;
import com.dokdok.gathering.dto.response.FavoriteGatheringListResponse;
import com.dokdok.gathering.dto.response.MyGatheringListResponse;
import com.dokdok.gathering.entity.Gathering;
import com.dokdok.gathering.entity.GatheringMember;
import com.dokdok.gathering.entity.GatheringMemberStatus;
import com.dokdok.gathering.entity.GatheringStatus;
import com.dokdok.gathering.exception.GatheringErrorCode;
import com.dokdok.gathering.exception.GatheringException;
import com.dokdok.gathering.repository.GatheringMemberRepository;
import com.dokdok.gathering.repository.GatheringRepository;
import com.dokdok.gathering.util.InvitationCodeGenerator;
import com.dokdok.global.util.SecurityUtil;
import com.dokdok.meeting.entity.MeetingStatus;
import com.dokdok.meeting.repository.MeetingRepository;
import com.dokdok.user.entity.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static com.dokdok.gathering.entity.GatheringRole.LEADER;
import static com.dokdok.gathering.entity.GatheringRole.MEMBER;
import static com.dokdok.gathering.entity.GatheringStatus.ACTIVE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.mockStatic;

@ExtendWith(MockitoExtension.class)
@DisplayName("GatheringService 테스트")
class GatheringServiceTest {

	@InjectMocks
    private GatheringService gatheringService;

    @Mock
    private GatheringMemberRepository gatheringMemberRepository;

    @Mock
    private GatheringRepository gatheringRepository;

    @Mock
    private GatheringValidator gatheringValidator;

	@Mock
	private MeetingRepository meetingRepository;

	private MockedStatic<SecurityUtil> securityUtilMock;

	private User leader;
	private User member;
	private User newUser;
	private User pendingUser;
	private Gathering gathering1;
	private Gathering gathering2;
	private GatheringMember leaderMember;
	private GatheringMember normalMember;
	private GatheringMember pendingMember;

	@BeforeEach
	void setUp() {
		securityUtilMock = mockStatic(SecurityUtil.class);

		leader = User.builder()
				.id(1L)
				.nickname("리더닉네임")
				.profileImageUrl("leader.jpg")
				.build();

		member = User.builder()
				.id(2L)
				.nickname("멤버닉네임")
				.profileImageUrl("member.jpg")
				.build();

		newUser = User.builder()
				.id(3L)
				.nickname("신규유저")
				.profileImageUrl("new.jpg")
				.build();

		pendingUser = User.builder()
				.id(4L)
				.nickname("대기유저")
				.profileImageUrl("pending.jpg")
				.build();

		gathering1 = Gathering.builder()
				.id(1L)
				.gatheringName("독서 모임")
				.description("열심히 읽는 모임")
				.gatheringStatus(ACTIVE)
				.invitationLink("https://invite.link/abc123")
				.gatheringLeader(leader)
				.createdAt(LocalDateTime.now().minusDays(30))
				.updatedAt(LocalDateTime.now().minusDays(30))
				.build();

		gathering2 = Gathering.builder()
				.id(2L)
				.gatheringName("bookbook")
				.description("test test")
				.gatheringStatus(ACTIVE)
				.gatheringLeader(leader)
				.createdAt(LocalDateTime.now().minusDays(20))
				.updatedAt(LocalDateTime.now().minusDays(20))
				.build();

		leaderMember = GatheringMember.builder()
				.id(1L)
				.gathering(gathering1)
				.user(leader)
				.isFavorite(true)
				.role(LEADER)
				.joinedAt(LocalDateTime.now().minusDays(30))
				.build();

		normalMember = GatheringMember.builder()
				.id(2L)
				.gathering(gathering1)
				.user(member)
				.isFavorite(false)
				.role(MEMBER)
				.memberStatus(GatheringMemberStatus.ACTIVE)
				.joinedAt(LocalDateTime.now().minusDays(10))
				.build();

		pendingMember = GatheringMember.builder()
				.id(3L)
				.gathering(gathering1)
				.user(pendingUser)
				.isFavorite(false)
				.role(MEMBER)
				.memberStatus(GatheringMemberStatus.PENDING)
				.build();
	}

	@AfterEach
	void tearDown() {
		securityUtilMock.close();
	}

	@Test
	@DisplayName("모임 생성 성공 - 고유 초대 코드 생성")
	void createGathering_Success() {
		// given
		GatheringCreateRequest request = new GatheringCreateRequest("새 모임", "설명");
		securityUtilMock.when(SecurityUtil::getCurrentUserEntity).thenReturn(leader);

		try (MockedStatic<InvitationCodeGenerator> codeMock = mockStatic(InvitationCodeGenerator.class)) {
			codeMock.when(InvitationCodeGenerator::generate).thenReturn("INVITE_CODE");

			given(gatheringRepository.existsByInvitationLink("INVITE_CODE")).willReturn(false);
			given(gatheringRepository.save(any(Gathering.class))).willAnswer(invocation -> invocation.getArgument(0));
			given(gatheringMemberRepository.save(any(GatheringMember.class))).willAnswer(invocation -> invocation.getArgument(0));
			given(gatheringMemberRepository.countActiveMembersByStatus(any())).willReturn(1);
			given(meetingRepository.countByGatheringIdAndMeetingStatus(any(), eq(MeetingStatus.DONE))).willReturn(0);

			// when
			GatheringCreateResponse response = gatheringService.createGathering(request);

			// then
			assertThat(response).isNotNull();
			assertThat(response.gatheringName()).isEqualTo("새 모임");
			assertThat(response.invitationLink()).isEqualTo("INVITE_CODE");
			assertThat(response.totalMembers()).isEqualTo(1);
			assertThat(response.totalMeetings()).isEqualTo(0);

			securityUtilMock.verify(SecurityUtil::getCurrentUserEntity, times(1));
			verify(gatheringRepository, times(1)).existsByInvitationLink("INVITE_CODE");
			verify(gatheringRepository, times(1)).save(any(Gathering.class));
			verify(gatheringMemberRepository, times(1)).save(any(GatheringMember.class));
		}
	}

	@Test
	@DisplayName("모임 생성 시 초대 코드 중복이면 재시도 후 성공")
	void createGathering_RetryOnDuplicateCode() {
		// given
		GatheringCreateRequest request = new GatheringCreateRequest("새 모임", "설명");
		securityUtilMock.when(SecurityUtil::getCurrentUserEntity).thenReturn(leader);

		try (MockedStatic<InvitationCodeGenerator> codeMock = mockStatic(InvitationCodeGenerator.class)) {
			codeMock.when(InvitationCodeGenerator::generate)
					.thenReturn("DUPLICATE_CODE")
					.thenReturn("UNIQUE_CODE");

			given(gatheringRepository.existsByInvitationLink("DUPLICATE_CODE")).willReturn(true);
			given(gatheringRepository.existsByInvitationLink("UNIQUE_CODE")).willReturn(false);
			given(gatheringRepository.save(any(Gathering.class))).willAnswer(invocation -> invocation.getArgument(0));
			given(gatheringMemberRepository.save(any(GatheringMember.class))).willAnswer(invocation -> invocation.getArgument(0));
			given(gatheringMemberRepository.countActiveMembersByStatus(any())).willReturn(1);
			given(meetingRepository.countByGatheringIdAndMeetingStatus(any(), eq(MeetingStatus.DONE))).willReturn(0);

			// when
			GatheringCreateResponse response = gatheringService.createGathering(request);

			// then
			assertThat(response.invitationLink()).isEqualTo("UNIQUE_CODE");

			verify(gatheringRepository, times(1)).existsByInvitationLink("DUPLICATE_CODE");
			verify(gatheringRepository, times(1)).existsByInvitationLink("UNIQUE_CODE");
			verify(gatheringRepository, times(1)).save(any(Gathering.class));
			verify(gatheringMemberRepository, times(1)).save(any(GatheringMember.class));
		}
	}

	@Test
	@DisplayName("모임 생성 실패 - 초대 코드 중복이 계속되는 경우 예외")
	void createGathering_Fail_WhenInvitationCodeCollides() {
		// given
		GatheringCreateRequest request = new GatheringCreateRequest("새 모임", "설명");
		securityUtilMock.when(SecurityUtil::getCurrentUserEntity).thenReturn(leader);

		try (MockedStatic<InvitationCodeGenerator> codeMock = mockStatic(InvitationCodeGenerator.class)) {
			codeMock.when(InvitationCodeGenerator::generate).thenReturn("DUPLICATE_CODE");
			given(gatheringRepository.existsByInvitationLink("DUPLICATE_CODE")).willReturn(true);

			// when & then
			assertThatThrownBy(() -> gatheringService.createGathering(request))
					.isInstanceOf(GatheringException.class)
					.hasFieldOrPropertyWithValue("errorCode", GatheringErrorCode.INVITATION_CODE_GENERATION_FAILED);

			verify(gatheringRepository, times(10)).existsByInvitationLink("DUPLICATE_CODE");
			verify(gatheringRepository, times(0)).save(any(Gathering.class));
			verify(gatheringMemberRepository, times(0)).save(any(GatheringMember.class));
		}
	}

	@Test
	@DisplayName("즐겨찾기 모임 목록 조회 성공")
	void getFavoriteGatherings_Success() {
		// given
		Long userId = 1L;

		securityUtilMock.when(SecurityUtil::getCurrentUserId).thenReturn(userId);

		GatheringMember member1 = GatheringMember.builder()
				.id(1L)
				.gathering(gathering1)
				.user(leader)
				.isFavorite(true)
				.role(LEADER)
				.joinedAt(LocalDateTime.now().minusDays(10))
				.build();

		GatheringMember member2 = GatheringMember.builder()
				.id(2L)
				.gathering(gathering2)
				.user(leader)
				.isFavorite(false)
				.role(MEMBER)
				.joinedAt(LocalDateTime.now().minusDays(5))
				.build();

		List<GatheringMember> favoriteMembers = List.of(member1, member2);

		given(gatheringMemberRepository.findFavoriteGatheringsByUserId(userId)).willReturn(favoriteMembers);
		given(gatheringMemberRepository.countActiveMembersByStatus(1L)).willReturn(1);
		given(gatheringMemberRepository.countActiveMembersByStatus(2L)).willReturn(1);
		given(meetingRepository.countByGatheringIdAndMeetingStatus(1L, MeetingStatus.DONE)).willReturn(3);
		given(meetingRepository.countByGatheringIdAndMeetingStatus(2L, MeetingStatus.DONE)).willReturn(5);

		// when
		FavoriteGatheringListResponse response = gatheringService.getFavoriteGatherings();

		// then
		assertThat(response).isNotNull();
		assertThat(response.gatherings()).hasSize(2);

		GatheringListItemResponse firstGathering = response.gatherings().get(0);
		assertThat(firstGathering.gatheringId()).isEqualTo(1L);
		assertThat(firstGathering.gatheringName()).isEqualTo("독서 모임");
		assertThat(firstGathering.isFavorite()).isTrue();
		assertThat(firstGathering.gatheringStatus()).isEqualTo(ACTIVE);
		assertThat(firstGathering.totalMembers()).isEqualTo(1);
		assertThat(firstGathering.totalMeetings()).isEqualTo(3);
		assertThat(firstGathering.currentUserRole()).isEqualTo(LEADER);
		assertThat(firstGathering.daysFromJoined()).isEqualTo(10);

		GatheringListItemResponse secondGathering = response.gatherings().get(1);
		assertThat(secondGathering.gatheringId()).isEqualTo(2L);
		assertThat(secondGathering.gatheringName()).isEqualTo("bookbook");
		assertThat(secondGathering.isFavorite()).isFalse();
		assertThat(secondGathering.gatheringStatus()).isEqualTo(ACTIVE);
		assertThat(secondGathering.totalMembers()).isEqualTo(1);
		assertThat(secondGathering.totalMeetings()).isEqualTo(5);
		assertThat(secondGathering.currentUserRole()).isEqualTo(MEMBER);

		securityUtilMock.verify(SecurityUtil::getCurrentUserId, times(1));
		verify(gatheringMemberRepository, times(1)).findFavoriteGatheringsByUserId(eq(userId));
		verify(gatheringMemberRepository, times(1)).countActiveMembersByStatus(1L);
		verify(gatheringMemberRepository, times(1)).countActiveMembersByStatus(2L);
		verify(meetingRepository, times(1)).countByGatheringIdAndMeetingStatus(1L, MeetingStatus.DONE);
		verify(meetingRepository, times(1)).countByGatheringIdAndMeetingStatus(2L, MeetingStatus.DONE);
	}

	@Test
	@DisplayName("즐겨찾기 모임 목록이 비어있을 때")
	void getFavoriteGatherings_EmptyList() {
		// given
		Long userId = 1L;

		securityUtilMock.when(SecurityUtil::getCurrentUserId).thenReturn(userId);

		given(gatheringMemberRepository.findFavoriteGatheringsByUserId(userId)).willReturn(List.of());

		// when
		FavoriteGatheringListResponse response = gatheringService.getFavoriteGatherings();

		// then
		assertThat(response).isNotNull();
		assertThat(response.gatherings()).isEmpty();

		securityUtilMock.verify(SecurityUtil::getCurrentUserId, times(1));
		verify(gatheringMemberRepository, times(1)).findFavoriteGatheringsByUserId(eq(userId));
		verify(gatheringMemberRepository, times(0)).countByGatheringIdAndRemovedAtIsNull(any());
	}

	@Test
	@DisplayName("내 모임 목록 조회 성공")
	void getMyGatherings_Success() {
		// given
		Long userId = 1L;
		Pageable pageable = PageRequest.of(0, 10);

		securityUtilMock.when(SecurityUtil::getCurrentUserId).thenReturn(userId);

		GatheringMember member1 = GatheringMember.builder()
				.id(1L)
				.gathering(gathering1)
				.user(leader)
				.isFavorite(true)
				.role(LEADER)
				.joinedAt(LocalDateTime.now().minusDays(10))
				.build();

		GatheringMember member2 = GatheringMember.builder()
				.id(2L)
				.gathering(gathering2)
				.user(leader)
				.isFavorite(false)
				.role(MEMBER)
				.joinedAt(LocalDateTime.now().minusDays(5))
				.build();

		Page<GatheringMember> memberPage = new PageImpl<>(List.of(member1, member2), pageable, 2);

		given(gatheringMemberRepository.findActiveGatheringsByUserIdWithPage(userId, pageable)).willReturn(memberPage);
		given(gatheringMemberRepository.countActiveMembersByStatus(1L)).willReturn(1);
		given(gatheringMemberRepository.countActiveMembersByStatus(2L)).willReturn(1);
		given(meetingRepository.countByGatheringIdAndMeetingStatus(1L, MeetingStatus.DONE)).willReturn(3);
		given(meetingRepository.countByGatheringIdAndMeetingStatus(2L, MeetingStatus.DONE)).willReturn(5);

		// when
		MyGatheringListResponse response = gatheringService.getMyGatherings(pageable);

		// then
		assertThat(response).isNotNull();
		assertThat(response.items()).hasSize(2);
		assertThat(response.totalCount()).isEqualTo(2);
		assertThat(response.currentPage()).isEqualTo(0);
		assertThat(response.pageSize()).isEqualTo(10);
		assertThat(response.totalPages()).isEqualTo(1);

		GatheringListItemResponse firstGathering = response.items().get(0);
		assertThat(firstGathering.gatheringId()).isEqualTo(1L);
		assertThat(firstGathering.gatheringName()).isEqualTo("독서 모임");
		assertThat(firstGathering.isFavorite()).isTrue();

		securityUtilMock.verify(SecurityUtil::getCurrentUserId, times(1));
		verify(gatheringMemberRepository, times(1)).findActiveGatheringsByUserIdWithPage(userId, pageable);
		verify(gatheringMemberRepository, times(1)).countActiveMembersByStatus(1L);
		verify(gatheringMemberRepository, times(1)).countActiveMembersByStatus(2L);
		verify(meetingRepository, times(1)).countByGatheringIdAndMeetingStatus(1L, MeetingStatus.DONE);
		verify(meetingRepository, times(1)).countByGatheringIdAndMeetingStatus(2L, MeetingStatus.DONE);
	}

	@Test
	@DisplayName("모임 상세 조회 성공 - 일반 멤버")
	void getGatheringDetail_Success_AsMember() {
		// given
		Long gatheringId = 1L;
		Long userId = 2L;

		securityUtilMock.when(SecurityUtil::getCurrentUserId).thenReturn(userId);

		List<GatheringMember> allMembers = List.of(leaderMember, normalMember);

		given(gatheringValidator.validateAndGetGathering(gatheringId)).willReturn(gathering1);
		given(gatheringValidator.validateAndGetMember(gatheringId, userId)).willReturn(normalMember);
		given(gatheringMemberRepository.findAllMembersByGatheringId(gatheringId)).willReturn(allMembers);
		given(meetingRepository.countByGatheringIdAndMeetingStatus(gatheringId, MeetingStatus.DONE)).willReturn(3);

		// when
		GatheringDetailResponse response = gatheringService.getGatheringDetail(gatheringId);

		// then
		assertThat(response).isNotNull();
		assertThat(response.gatheringId()).isEqualTo(1L);
		assertThat(response.gatheringName()).isEqualTo("독서 모임");
		assertThat(response.description()).isEqualTo("열심히 읽는 모임");
		assertThat(response.gatheringStatus()).isEqualTo(ACTIVE);
		assertThat(response.isFavorite()).isFalse();
		assertThat(response.invitationLink()).isEqualTo("https://invite.link/abc123");
		assertThat(response.currentUserRole()).isEqualTo(MEMBER);
		assertThat(response.totalMembers()).isEqualTo(2);
		assertThat(response.totalMeetings()).isEqualTo(3);
		assertThat(response.members()).hasSize(2);

		GatheringDetailResponse.MemberInfo leaderInfo = response.members().stream()
				.filter(m -> m.role() == LEADER)
				.findFirst()
				.orElseThrow();
		assertThat(leaderInfo.userId()).isEqualTo(1L);
		assertThat(leaderInfo.nickname()).isEqualTo("리더닉네임");
		assertThat(leaderInfo.role()).isEqualTo(LEADER);

		GatheringDetailResponse.MemberInfo memberInfo = response.members().stream()
				.filter(m -> m.role() == MEMBER)
				.findFirst()
				.orElseThrow();
		assertThat(memberInfo.userId()).isEqualTo(2L);
		assertThat(memberInfo.nickname()).isEqualTo("멤버닉네임");
		assertThat(memberInfo.role()).isEqualTo(MEMBER);

		securityUtilMock.verify(SecurityUtil::getCurrentUserId, times(1));
		verify(gatheringValidator, times(1)).validateAndGetGathering(gatheringId);
		verify(gatheringValidator, times(1)).validateAndGetMember(gatheringId, userId);
		verify(gatheringMemberRepository, times(1)).findAllMembersByGatheringId(gatheringId);
	}

	@Test
	@DisplayName("모임 상세 조회 실패 - 모임이 존재하지 않음")
	void getGatheringDetail_Fail_GatheringNotFound() {
		// given
		Long gatheringId = 999L;
		Long userId = 1L;

		securityUtilMock.when(SecurityUtil::getCurrentUserId).thenReturn(userId);

		doThrow(new GatheringException(GatheringErrorCode.GATHERING_NOT_FOUND))
				.when(gatheringValidator).validateAndGetGathering(gatheringId);

		// when & then
		assertThatThrownBy(() -> gatheringService.getGatheringDetail(gatheringId))
				.isInstanceOf(GatheringException.class)
				.hasMessage(GatheringErrorCode.GATHERING_NOT_FOUND.getMessage());

		securityUtilMock.verify(SecurityUtil::getCurrentUserId, times(1));
		verify(gatheringValidator, times(1)).validateAndGetGathering(gatheringId);
		verify(gatheringValidator, times(0)).validateAndGetMember(any(), any());
	}

	@Test
	@DisplayName("모임 상세 조회 실패 - 모임 멤버가 아님")
	void getGatheringDetail_Fail_NotGatheringMember() {
		// given
		Long gatheringId = 1L;
		Long userId = 999L;

		securityUtilMock.when(SecurityUtil::getCurrentUserId).thenReturn(userId);

		given(gatheringValidator.validateAndGetGathering(gatheringId)).willReturn(gathering1);
		doThrow(new GatheringException(GatheringErrorCode.NOT_GATHERING_MEMBER))
				.when(gatheringValidator).validateAndGetMember(gatheringId, userId);

		// when & then
		assertThatThrownBy(() -> gatheringService.getGatheringDetail(gatheringId))
				.isInstanceOf(GatheringException.class)
				.hasMessage(GatheringErrorCode.NOT_GATHERING_MEMBER.getMessage());

		securityUtilMock.verify(SecurityUtil::getCurrentUserId, times(1));
		verify(gatheringValidator, times(1)).validateAndGetGathering(gatheringId);
		verify(gatheringValidator, times(1)).validateAndGetMember(gatheringId, userId);
		verify(gatheringMemberRepository, times(0)).findAllMembersByGatheringId(any());
	}

	@Test
	@DisplayName("모임 정보 수정 성공 - 리더가 모임명과 설명 모두 수정")
	void updateGathering_Success_UpdateBoth() {
		// given
		Long gatheringId = 1L;
		Long leaderId = 1L;
		GatheringUpdateRequest request = GatheringUpdateRequest.builder()
				.gatheringName("새로운 모임명")
				.description("새로운 설명")
				.build();

		securityUtilMock.when(SecurityUtil::getCurrentUserId).thenReturn(leaderId);

		given(gatheringValidator.validateAndGetGathering(gatheringId)).willReturn(gathering1);

		// when
		GatheringUpdateResponse response = gatheringService.updateGathering(gatheringId, request);

		// then
		assertThat(response).isNotNull();
		assertThat(response.gatheringId()).isEqualTo(1L);
		assertThat(response.gatheringName()).isEqualTo("새로운 모임명");
		assertThat(response.description()).isEqualTo("새로운 설명");
		assertThat(gathering1.getGatheringName()).isEqualTo("새로운 모임명");
		assertThat(gathering1.getDescription()).isEqualTo("새로운 설명");

		securityUtilMock.verify(SecurityUtil::getCurrentUserId, times(1));
		verify(gatheringValidator, times(1)).validateAndGetGathering(gatheringId);
		verify(gatheringValidator, times(1)).validateLeader(gatheringId, leaderId);
	}

	@Test
	@DisplayName("모임 즐겨찾기 상태 변경 성공")
	void updateFavorite_Success() {
		// given
		Long gatheringId = 1L;
		Long userId = 2L;

		securityUtilMock.when(SecurityUtil::getCurrentUserId).thenReturn(userId);
		given(gatheringValidator.validateAndGetMember(gatheringId, userId)).willReturn(normalMember);

		// when
		gatheringService.updateFavorite(gatheringId);

		// then
		assertThat(normalMember.getIsFavorite()).isTrue();
		securityUtilMock.verify(SecurityUtil::getCurrentUserId, times(1));
		verify(gatheringValidator, times(1)).validateFavoriteLimit(userId);
	}

	@Test
	@DisplayName("즐겨찾기 추가 실패 - 최대 4개 초과")
	void updateFavorite_Fail_FavoriteLimitExceeded() {
		// given
		Long gatheringId = 1L;
		Long userId = 2L;

		securityUtilMock.when(SecurityUtil::getCurrentUserId).thenReturn(userId);
		given(gatheringValidator.validateAndGetMember(gatheringId, userId)).willReturn(normalMember);
		doThrow(new GatheringException(GatheringErrorCode.FAVORITE_LIMIT_EXCEEDED))
				.when(gatheringValidator).validateFavoriteLimit(userId);

		// when & then
		assertThatThrownBy(() -> gatheringService.updateFavorite(gatheringId))
				.isInstanceOf(GatheringException.class)
				.hasMessage(GatheringErrorCode.FAVORITE_LIMIT_EXCEEDED.getMessage());

		assertThat(normalMember.getIsFavorite()).isFalse();
	}

	@Test
	@DisplayName("모임 정보 수정 성공 - 리더가 모임명만 수정")
	void updateGathering_Success_UpdateNameOnly() {
		// given
		Long gatheringId = 1L;
		Long leaderId = 1L;
		String originalDescription = gathering1.getDescription();
		GatheringUpdateRequest request = GatheringUpdateRequest.builder()
				.gatheringName("변경된 모임명")
				.description(null)
				.build();

		securityUtilMock.when(SecurityUtil::getCurrentUserId).thenReturn(leaderId);

		given(gatheringValidator.validateAndGetGathering(gatheringId)).willReturn(gathering1);

		// when
		GatheringUpdateResponse response = gatheringService.updateGathering(gatheringId, request);

		// then
		assertThat(response).isNotNull();
		assertThat(response.gatheringId()).isEqualTo(1L);
		assertThat(response.gatheringName()).isEqualTo("변경된 모임명");
		assertThat(response.description()).isEqualTo(originalDescription);
		assertThat(gathering1.getGatheringName()).isEqualTo("변경된 모임명");
		assertThat(gathering1.getDescription()).isEqualTo(originalDescription);

		securityUtilMock.verify(SecurityUtil::getCurrentUserId, times(1));
		verify(gatheringValidator, times(1)).validateAndGetGathering(gatheringId);
		verify(gatheringValidator, times(1)).validateLeader(gatheringId, leaderId);
	}

	@Test
	@DisplayName("모임 정보 수정 성공 - 리더가 설명만 수정")
	void updateGathering_Success_UpdateDescriptionOnly() {
		// given
		Long gatheringId = 1L;
		Long leaderId = 1L;
		String originalName = gathering1.getGatheringName();
		GatheringUpdateRequest request = GatheringUpdateRequest.builder()
				.gatheringName(originalName)
				.description("변경된 설명")
				.build();

		securityUtilMock.when(SecurityUtil::getCurrentUserId).thenReturn(leaderId);

		given(gatheringValidator.validateAndGetGathering(gatheringId)).willReturn(gathering1);

		// when
		GatheringUpdateResponse response = gatheringService.updateGathering(gatheringId, request);

		// then
		assertThat(response).isNotNull();
		assertThat(response.gatheringId()).isEqualTo(1L);
		assertThat(response.gatheringName()).isEqualTo(originalName);
		assertThat(response.description()).isEqualTo("변경된 설명");
		assertThat(gathering1.getGatheringName()).isEqualTo(originalName);
		assertThat(gathering1.getDescription()).isEqualTo("변경된 설명");

		securityUtilMock.verify(SecurityUtil::getCurrentUserId, times(1));
		verify(gatheringValidator, times(1)).validateAndGetGathering(gatheringId);
		verify(gatheringValidator, times(1)).validateLeader(gatheringId, leaderId);
	}

	@Test
	@DisplayName("모임 정보 수정 실패 - 모임이 존재하지 않음")
	void updateGathering_Fail_GatheringNotFound() {
		// given
		Long gatheringId = 999L;
		Long leaderId = 1L;
		GatheringUpdateRequest request = GatheringUpdateRequest.builder()
				.gatheringName("새로운 모임명")
				.description("새로운 설명")
				.build();

		securityUtilMock.when(SecurityUtil::getCurrentUserId).thenReturn(leaderId);

		doThrow(new GatheringException(GatheringErrorCode.GATHERING_NOT_FOUND))
				.when(gatheringValidator).validateAndGetGathering(gatheringId);

		// when & then
		assertThatThrownBy(() -> gatheringService.updateGathering(gatheringId, request))
				.isInstanceOf(GatheringException.class)
				.hasMessage(GatheringErrorCode.GATHERING_NOT_FOUND.getMessage());

		securityUtilMock.verify(SecurityUtil::getCurrentUserId, times(1));
		verify(gatheringValidator, times(1)).validateAndGetGathering(gatheringId);
		verify(gatheringValidator, times(0)).validateLeader(any(), any());
	}

	@Test
	@DisplayName("모임 정보 수정 실패 - 리더가 아닌 일반 멤버")
	void updateGathering_Fail_NotLeader() {
		// given
		Long gatheringId = 1L;
		Long memberId = 2L;
		GatheringUpdateRequest request = GatheringUpdateRequest.builder()
				.gatheringName("새로운 모임명")
				.description("새로운 설명")
				.build();

		securityUtilMock.when(SecurityUtil::getCurrentUserId).thenReturn(memberId);

		given(gatheringValidator.validateAndGetGathering(gatheringId)).willReturn(gathering1);
		doThrow(new GatheringException(GatheringErrorCode.NOT_GATHERING_LEADER))
				.when(gatheringValidator).validateLeader(gatheringId, memberId);

		// when & then
		assertThatThrownBy(() -> gatheringService.updateGathering(gatheringId, request))
				.isInstanceOf(GatheringException.class)
				.hasMessage(GatheringErrorCode.NOT_GATHERING_LEADER.getMessage());

		securityUtilMock.verify(SecurityUtil::getCurrentUserId, times(1));
		verify(gatheringValidator, times(1)).validateAndGetGathering(gatheringId);
		verify(gatheringValidator, times(1)).validateLeader(gatheringId, memberId);
	}

	@Test
	@DisplayName("모임 삭제 성공 - 리더가 삭제")
	void deleteGathering_Success() {
		// given
		Long gatheringId = 1L;
		Long leaderId = 1L;

		securityUtilMock.when(SecurityUtil::getCurrentUserId).thenReturn(leaderId);

		given(gatheringValidator.validateAndGetGathering(gatheringId)).willReturn(gathering1);

		// when
		gatheringService.deleteGathering(gatheringId);

		// then
		assertThat(gathering1.getDeletedAt()).isNotNull();
		assertThat(gathering1.getGatheringStatus()).isEqualTo(GatheringStatus.INACTIVE);

		securityUtilMock.verify(SecurityUtil::getCurrentUserId, times(1));
		verify(gatheringValidator, times(1)).validateAndGetGathering(gatheringId);
		verify(gatheringValidator,times(1)).validateLeader(gatheringId,leaderId);
	}

	@Test
	@DisplayName("모임 삭제 실패 - 모임이 존재하지 않음")
	void deleteGathering_Fail_GatheringNotFound() {
		// given
		Long gatheringId = 999L;
		Long leaderId = 1L;

		securityUtilMock.when(SecurityUtil::getCurrentUserId).thenReturn(leaderId);

		doThrow(new GatheringException(GatheringErrorCode.GATHERING_NOT_FOUND))
				.when(gatheringValidator).validateAndGetGathering(gatheringId);

		// when & then
		assertThatThrownBy(() -> gatheringService.deleteGathering(gatheringId))
				.isInstanceOf(GatheringException.class)
				.hasMessage(GatheringErrorCode.GATHERING_NOT_FOUND.getMessage());

		securityUtilMock.verify(SecurityUtil::getCurrentUserId, times(1));
		verify(gatheringValidator, times(1)).validateAndGetGathering(gatheringId);
		verify(gatheringValidator, times(0)).validateLeader(any(), any());
	}

	@Test
	@DisplayName("모임 삭제 실패 - 리더가 아닌 일반 멤버")
	void deleteGathering_Fail_NotLeader() {
		// given
		Long gatheringId = 1L;
		Long memberId = 2L;

		securityUtilMock.when(SecurityUtil::getCurrentUserId).thenReturn(memberId);

		given(gatheringValidator.validateAndGetGathering(gatheringId)).willReturn(gathering1);
		doThrow(new GatheringException(GatheringErrorCode.NOT_GATHERING_LEADER))
				.when(gatheringValidator).validateLeader(gatheringId, memberId);

		// when & then
		assertThatThrownBy(() -> gatheringService.deleteGathering(gatheringId))
				.isInstanceOf(GatheringException.class)
				.hasMessage(GatheringErrorCode.NOT_GATHERING_LEADER.getMessage());

		securityUtilMock.verify(SecurityUtil::getCurrentUserId, times(1));
		verify(gatheringValidator, times(1)).validateAndGetGathering(gatheringId);
		verify(gatheringValidator, times(1)).validateLeader(gatheringId, memberId);
	}

	@Test
	@DisplayName("모임원 강퇴 성공 - 리더가 일반 멤버 강퇴")
	void removeMember_Success() {
		// given
		Long gatheringId = 1L;
		Long leaderId = 1L;
		Long targetUserId = 2L;

		securityUtilMock.when(SecurityUtil::getCurrentUserId).thenReturn(leaderId);

		given(gatheringValidator.validateAndGetGathering(gatheringId)).willReturn(gathering1);
		given(gatheringValidator.validateAndGetMember(gatheringId, targetUserId)).willReturn(normalMember);

		// when
		gatheringService.removeMember(gatheringId, targetUserId);

		// then
		assertThat(normalMember.getRemovedAt()).isNotNull();

		securityUtilMock.verify(SecurityUtil::getCurrentUserId, times(1));
		verify(gatheringValidator, times(1)).validateAndGetGathering(gatheringId);
		verify(gatheringValidator, times(1)).validateLeader(gatheringId, leaderId);
		verify(gatheringValidator, times(1)).validateAndGetMember(gatheringId, targetUserId);
	}

	@Test
	@DisplayName("모임원 강퇴 실패 - 강퇴 대상이 리더")
	void removeMember_Fail_TargetIsLeader() {
		// given
		Long gatheringId = 1L;
		Long leaderId = 1L;
		Long targetUserId = 1L;

		securityUtilMock.when(SecurityUtil::getCurrentUserId).thenReturn(leaderId);

		given(gatheringValidator.validateAndGetGathering(gatheringId)).willReturn(gathering1);
		given(gatheringValidator.validateAndGetMember(gatheringId, targetUserId)).willReturn(leaderMember);

		// when & then
		assertThatThrownBy(() -> gatheringService.removeMember(gatheringId, targetUserId))
				.isInstanceOf(GatheringException.class)
				.hasMessage(GatheringErrorCode.CANNOT_REMOVE_LEADER.getMessage());

		assertThat(leaderMember.getRemovedAt()).isNull();

		securityUtilMock.verify(SecurityUtil::getCurrentUserId, times(1));
		verify(gatheringValidator, times(1)).validateAndGetGathering(gatheringId);
		verify(gatheringValidator, times(1)).validateLeader(gatheringId, leaderId);
		verify(gatheringValidator, times(1)).validateAndGetMember(gatheringId, targetUserId);
	}

	@Test
	@DisplayName("모임원 강퇴 실패 - 리더가 아닌 멤버")
	void removeMember_Fail_NotLeader() {
		// given
		Long gatheringId = 1L;
		Long memberId = 2L;
		Long targetUserId = 3L;

		securityUtilMock.when(SecurityUtil::getCurrentUserId).thenReturn(memberId);

		given(gatheringValidator.validateAndGetGathering(gatheringId)).willReturn(gathering1);
		doThrow(new GatheringException(GatheringErrorCode.NOT_GATHERING_LEADER))
				.when(gatheringValidator).validateLeader(gatheringId, memberId);

		// when & then
		assertThatThrownBy(() -> gatheringService.removeMember(gatheringId, targetUserId))
				.isInstanceOf(GatheringException.class)
				.hasMessage(GatheringErrorCode.NOT_GATHERING_LEADER.getMessage());

		securityUtilMock.verify(SecurityUtil::getCurrentUserId, times(1));
		verify(gatheringValidator, times(1)).validateAndGetGathering(gatheringId);
		verify(gatheringValidator, times(1)).validateLeader(gatheringId, memberId);
		verify(gatheringValidator, times(0)).validateAndGetMember(any(), any());
	}

	@Test
	@DisplayName("모임 정보 조회 성공 - 유효한 초대링크로 모임 정보 조회")
	void getJoinGatheringInfo_Success() {
		// given
		String invitationLink = "https://invite.link/abc123";

		given(gatheringValidator.validateInvitationLink(invitationLink)).willReturn(gathering1);
		given(gatheringMemberRepository.countActiveMembersByStatus(gathering1.getId())).willReturn(2);
		given(meetingRepository.countByGatheringIdAndMeetingStatus(gathering1.getId(), MeetingStatus.DONE)).willReturn(5);

		// when
		GatheringCreateResponse response = gatheringService.getJoinGatheringInfo(invitationLink);

		// then
		assertThat(response).isNotNull();
		assertThat(response.gatheringName()).isEqualTo("독서 모임");
		assertThat(response.invitationLink()).isEqualTo("https://invite.link/abc123");
		assertThat(response.totalMembers()).isEqualTo(2);
		assertThat(response.totalMeetings()).isEqualTo(5);
		assertThat(response.daysFromCreation()).isEqualTo(gathering1.getDaysFromCreation());

		verify(gatheringValidator, times(1)).validateInvitationLink(invitationLink);
		verify(gatheringMemberRepository, times(1)).countActiveMembersByStatus(gathering1.getId());
		verify(meetingRepository, times(1)).countByGatheringIdAndMeetingStatus(gathering1.getId(), MeetingStatus.DONE);
	}

	@Test
	@DisplayName("모임 정보 조회 실패 - 유효하지 않은 초대링크")
	void getJoinGatheringInfo_Fail_InvalidInvitationLink() {
		// given
		String invalidLink = "https://invite.link/invalid";

		doThrow(new GatheringException(GatheringErrorCode.GATHERING_NOT_FOUND))
				.when(gatheringValidator).validateInvitationLink(invalidLink);

		// when & then
		assertThatThrownBy(() -> gatheringService.getJoinGatheringInfo(invalidLink))
				.isInstanceOf(GatheringException.class)
				.hasMessage(GatheringErrorCode.GATHERING_NOT_FOUND.getMessage());

		verify(gatheringValidator, times(1)).validateInvitationLink(invalidLink);
	}

	@Test
	@DisplayName("모임 정보 조회 실패 - 빈 초대링크")
	void getJoinGatheringInfo_Fail_EmptyInvitationLink() {
		// given
		String emptyLink = "";

		doThrow(new GatheringException(GatheringErrorCode.GATHERING_NOT_FOUND))
				.when(gatheringValidator).validateInvitationLink(emptyLink);

		// when & then
		assertThatThrownBy(() -> gatheringService.getJoinGatheringInfo(emptyLink))
				.isInstanceOf(GatheringException.class)
				.hasMessage(GatheringErrorCode.GATHERING_NOT_FOUND.getMessage());

		verify(gatheringValidator, times(1)).validateInvitationLink(emptyLink);
	}

	@Test
	@DisplayName("모임 정보 조회 실패 - null 초대링크")
	void getJoinGatheringInfo_Fail_NullInvitationLink() {
		// given
		String nullLink = null;

		doThrow(new GatheringException(GatheringErrorCode.GATHERING_NOT_FOUND))
				.when(gatheringValidator).validateInvitationLink(nullLink);

		// when & then
		assertThatThrownBy(() -> gatheringService.getJoinGatheringInfo(nullLink))
				.isInstanceOf(GatheringException.class)
				.hasMessage(GatheringErrorCode.GATHERING_NOT_FOUND.getMessage());

		verify(gatheringValidator, times(1)).validateInvitationLink(nullLink);
	}

	@Test
	@DisplayName("모임 가입 성공 - 신규 사용자가 초대링크로 가입 요청")
	void joinGathering_Success() {
		// given
		String invitationLink = "https://invite.link/abc123";

		securityUtilMock.when(SecurityUtil::getCurrentUserEntity).thenReturn(newUser);

		given(gatheringValidator.validateInvitationLink(invitationLink)).willReturn(gathering1);
		given(gatheringMemberRepository.save(any(GatheringMember.class))).willAnswer(invocation -> {
			GatheringMember savedMember = invocation.getArgument(0);
			return GatheringMember.builder()
					.id(5L)
					.gathering(savedMember.getGathering())
					.user(savedMember.getUser())
					.role(savedMember.getRole())
					.memberStatus(savedMember.getMemberStatus())
					.isFavorite(false)
					.joinedAt(LocalDateTime.now())
					.build();
		});

		// when
		GatheringJoinResponse response = gatheringService.joinGathering(invitationLink);

		// then
		assertThat(response).isNotNull();
		assertThat(response.gatheringId()).isEqualTo(1L);
		assertThat(response.gatheringName()).isEqualTo("독서 모임");
		assertThat(response.memberStatus()).isEqualTo(GatheringMemberStatus.PENDING);

		securityUtilMock.verify(SecurityUtil::getCurrentUserEntity, times(1));
		verify(gatheringValidator, times(1)).validateInvitationLink(invitationLink);
		verify(gatheringValidator, times(1)).validateJoinedGathering(1L, 3L);
		verify(gatheringMemberRepository, times(1)).save(any(GatheringMember.class));
	}

	@Test
	@DisplayName("모임 가입 실패 - 유효하지 않은 초대링크")
	void joinGathering_Fail_InvalidInvitationLink() {
		// given
		String invalidLink = "https://invite.link/invalid";

		securityUtilMock.when(SecurityUtil::getCurrentUserEntity).thenReturn(newUser);

		doThrow(new GatheringException(GatheringErrorCode.GATHERING_NOT_FOUND))
				.when(gatheringValidator).validateInvitationLink(invalidLink);

		// when & then
		assertThatThrownBy(() -> gatheringService.joinGathering(invalidLink))
				.isInstanceOf(GatheringException.class)
				.hasMessage(GatheringErrorCode.GATHERING_NOT_FOUND.getMessage());

		securityUtilMock.verify(SecurityUtil::getCurrentUserEntity, times(1));
		verify(gatheringValidator, times(1)).validateInvitationLink(invalidLink);
		verify(gatheringValidator, times(0)).validateJoinedGathering(any(), any());
		verify(gatheringMemberRepository, times(0)).save(any());
	}

	@Test
	@DisplayName("모임 가입 실패 - 이미 활성 멤버인 경우")
	void joinGathering_Fail_AlreadyActiveMember() {
		// given
		String invitationLink = "https://invite.link/abc123";

		securityUtilMock.when(SecurityUtil::getCurrentUserEntity).thenReturn(member);

		given(gatheringValidator.validateInvitationLink(invitationLink)).willReturn(gathering1);
		doThrow(new GatheringException(GatheringErrorCode.ALREADY_GATHERING_MEMBER))
				.when(gatheringValidator).validateJoinedGathering(1L, 2L);

		// when & then
		assertThatThrownBy(() -> gatheringService.joinGathering(invitationLink))
				.isInstanceOf(GatheringException.class)
				.hasMessage(GatheringErrorCode.ALREADY_GATHERING_MEMBER.getMessage());

		securityUtilMock.verify(SecurityUtil::getCurrentUserEntity, times(1));
		verify(gatheringValidator, times(1)).validateInvitationLink(invitationLink);
		verify(gatheringValidator, times(1)).validateJoinedGathering(1L, 2L);
		verify(gatheringMemberRepository, times(0)).save(any());
	}

	@Test
	@DisplayName("모임 가입 실패 - 이미 가입 요청이 진행 중인 경우")
	void joinGathering_Fail_AlreadyPendingRequest() {
		// given
		String invitationLink = "https://invite.link/abc123";

		securityUtilMock.when(SecurityUtil::getCurrentUserEntity).thenReturn(pendingUser);

		given(gatheringValidator.validateInvitationLink(invitationLink)).willReturn(gathering1);
		doThrow(new GatheringException(GatheringErrorCode.JOIN_REQUEST_ALREADY_PENDING))
				.when(gatheringValidator).validateJoinedGathering(1L, 4L);

		// when & then
		assertThatThrownBy(() -> gatheringService.joinGathering(invitationLink))
				.isInstanceOf(GatheringException.class)
				.hasMessage(GatheringErrorCode.JOIN_REQUEST_ALREADY_PENDING.getMessage());

		securityUtilMock.verify(SecurityUtil::getCurrentUserEntity, times(1));
		verify(gatheringValidator, times(1)).validateInvitationLink(invitationLink);
		verify(gatheringValidator, times(1)).validateJoinedGathering(1L, 4L);
		verify(gatheringMemberRepository, times(0)).save(any());
	}

	@Test
	@DisplayName("가입 요청 승인 성공 - 리더가 PENDING 멤버를 ACTIVE로 승인")
	void handleJoinRequest_Success_Approve() {
		// given
		Long gatheringId = 1L;
		Long targetUserId = 4L;
		JoinGatheringMemberRequest request = new JoinGatheringMemberRequest(GatheringMemberStatus.ACTIVE);

		securityUtilMock.when(SecurityUtil::getCurrentUserEntity).thenReturn(leader);

		given(gatheringMemberRepository.findByGatheringIdAndUserId(gatheringId, targetUserId))
				.willReturn(Optional.of(pendingMember));

		// when
		gatheringService.handleJoinRequest(gatheringId, targetUserId, request);

		// then
		assertThat(pendingMember.getMemberStatus()).isEqualTo(GatheringMemberStatus.ACTIVE);
		assertThat(pendingMember.getJoinedAt()).isNotNull();

		securityUtilMock.verify(SecurityUtil::getCurrentUserEntity, times(1));
		verify(gatheringValidator, times(1)).validateLeader(gatheringId, leader.getId());
		verify(gatheringMemberRepository, times(1)).findByGatheringIdAndUserId(gatheringId, targetUserId);
	}

	@Test
	@DisplayName("가입 요청 거절 성공 - 리더가 PENDING 멤버를 REJECTED로 거절")
	void handleJoinRequest_Success_Reject() {
		// given
		Long gatheringId = 1L;
		Long targetUserId = 4L;
		JoinGatheringMemberRequest request = new JoinGatheringMemberRequest(GatheringMemberStatus.REJECTED);

		securityUtilMock.when(SecurityUtil::getCurrentUserEntity).thenReturn(leader);

		given(gatheringMemberRepository.findByGatheringIdAndUserId(gatheringId, targetUserId))
				.willReturn(Optional.of(pendingMember));

		// when
		gatheringService.handleJoinRequest(gatheringId, targetUserId, request);

		// then
		assertThat(pendingMember.getMemberStatus()).isEqualTo(GatheringMemberStatus.REJECTED);
		assertThat(pendingMember.getJoinedAt()).isNull();

		securityUtilMock.verify(SecurityUtil::getCurrentUserEntity, times(1));
		verify(gatheringValidator, times(1)).validateLeader(gatheringId, leader.getId());
		verify(gatheringMemberRepository, times(1)).findByGatheringIdAndUserId(gatheringId, targetUserId);
	}

	@Test
	@DisplayName("가입 요청 처리 실패 - 리더가 아닌 멤버가 처리 시도")
	void handleJoinRequest_Fail_NotLeader() {
		// given
		Long gatheringId = 1L;
		Long targetUserId = 4L;
		JoinGatheringMemberRequest request = new JoinGatheringMemberRequest(GatheringMemberStatus.ACTIVE);

		securityUtilMock.when(SecurityUtil::getCurrentUserEntity).thenReturn(member);

		doThrow(new GatheringException(GatheringErrorCode.NOT_GATHERING_LEADER))
				.when(gatheringValidator).validateLeader(gatheringId, member.getId());

		// when & then
		assertThatThrownBy(() -> gatheringService.handleJoinRequest(gatheringId, targetUserId, request))
				.isInstanceOf(GatheringException.class)
				.hasMessage(GatheringErrorCode.NOT_GATHERING_LEADER.getMessage());

		securityUtilMock.verify(SecurityUtil::getCurrentUserEntity, times(1));
		verify(gatheringValidator, times(1)).validateLeader(gatheringId, member.getId());
		verify(gatheringMemberRepository, times(0)).findByGatheringIdAndUserId(any(), any());
	}

	@Test
	@DisplayName("가입 요청 처리 실패 - 대상 멤버가 존재하지 않음")
	void handleJoinRequest_Fail_MemberNotFound() {
		// given
		Long gatheringId = 1L;
		Long targetUserId = 999L;
		JoinGatheringMemberRequest request = new JoinGatheringMemberRequest(GatheringMemberStatus.ACTIVE);

		securityUtilMock.when(SecurityUtil::getCurrentUserEntity).thenReturn(leader);

		given(gatheringMemberRepository.findByGatheringIdAndUserId(gatheringId, targetUserId))
				.willReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> gatheringService.handleJoinRequest(gatheringId, targetUserId, request))
				.isInstanceOf(GatheringException.class)
				.hasMessage(GatheringErrorCode.NOT_GATHERING_MEMBER.getMessage());

		securityUtilMock.verify(SecurityUtil::getCurrentUserEntity, times(1));
		verify(gatheringValidator, times(1)).validateLeader(gatheringId, leader.getId());
		verify(gatheringMemberRepository, times(1)).findByGatheringIdAndUserId(gatheringId, targetUserId);
	}

	@Test
	@DisplayName("가입 요청 처리 실패 - 대상 멤버가 PENDING 상태가 아님 (이미 ACTIVE)")
	void handleJoinRequest_Fail_NotPendingStatus_AlreadyActive() {
		// given
		Long gatheringId = 1L;
		Long targetUserId = 2L;
		JoinGatheringMemberRequest request = new JoinGatheringMemberRequest(GatheringMemberStatus.ACTIVE);

		securityUtilMock.when(SecurityUtil::getCurrentUserEntity).thenReturn(leader);

		given(gatheringMemberRepository.findByGatheringIdAndUserId(gatheringId, targetUserId))
				.willReturn(Optional.of(normalMember));

		// when & then
		assertThatThrownBy(() -> gatheringService.handleJoinRequest(gatheringId, targetUserId, request))
				.isInstanceOf(GatheringException.class)
				.hasMessage(GatheringErrorCode.NOT_PENDING_STATUS.getMessage());

		securityUtilMock.verify(SecurityUtil::getCurrentUserEntity, times(1));
		verify(gatheringValidator, times(1)).validateLeader(gatheringId, leader.getId());
		verify(gatheringMemberRepository, times(1)).findByGatheringIdAndUserId(gatheringId, targetUserId);
	}

	@Test
	@DisplayName("가입 요청 처리 실패 - 대상 멤버가 PENDING 상태가 아님 (이미 REJECTED)")
	void handleJoinRequest_Fail_NotPendingStatus_AlreadyRejected() {
		// given
		Long gatheringId = 1L;
		Long targetUserId = 5L;
		JoinGatheringMemberRequest request = new JoinGatheringMemberRequest(GatheringMemberStatus.ACTIVE);

		GatheringMember rejectedMember = GatheringMember.builder()
				.id(5L)
				.gathering(gathering1)
				.user(newUser)
				.isFavorite(false)
				.role(MEMBER)
				.memberStatus(GatheringMemberStatus.REJECTED)
				.build();

		securityUtilMock.when(SecurityUtil::getCurrentUserEntity).thenReturn(leader);

		given(gatheringMemberRepository.findByGatheringIdAndUserId(gatheringId, targetUserId))
				.willReturn(Optional.of(rejectedMember));

		// when & then
		assertThatThrownBy(() -> gatheringService.handleJoinRequest(gatheringId, targetUserId, request))
				.isInstanceOf(GatheringException.class)
				.hasMessage(GatheringErrorCode.NOT_PENDING_STATUS.getMessage());

		securityUtilMock.verify(SecurityUtil::getCurrentUserEntity, times(1));
		verify(gatheringValidator, times(1)).validateLeader(gatheringId, leader.getId());
		verify(gatheringMemberRepository, times(1)).findByGatheringIdAndUserId(gatheringId, targetUserId);
	}

	@Test
	@DisplayName("가입 요청 처리 실패 - approve_type이 PENDING인 경우")
	void handleJoinRequest_Fail_InvalidApproveType_Pending() {
		// given
		Long gatheringId = 1L;
		Long targetUserId = 4L;
		JoinGatheringMemberRequest request = new JoinGatheringMemberRequest(GatheringMemberStatus.PENDING);

		securityUtilMock.when(SecurityUtil::getCurrentUserEntity).thenReturn(leader);

		// when & then
		assertThatThrownBy(() -> gatheringService.handleJoinRequest(gatheringId, targetUserId, request))
				.isInstanceOf(GatheringException.class)
				.hasMessage(GatheringErrorCode.INVALID_APPROVE_TYPE.getMessage());

		securityUtilMock.verify(SecurityUtil::getCurrentUserEntity, times(1));
		verify(gatheringValidator, times(1)).validateLeader(gatheringId, leader.getId());
		verify(gatheringMemberRepository, times(0)).findByGatheringIdAndUserId(any(), any());
	}
}

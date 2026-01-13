package com.dokdok.gathering.service;

import com.dokdok.gathering.dto.GatheringDetailResponse;
import com.dokdok.gathering.dto.GatheringSimpleResponse;
import com.dokdok.gathering.dto.GatheringUpdateRequest;
import com.dokdok.gathering.dto.GatheringUpdateResponse;
import com.dokdok.gathering.dto.MyGatheringListResponse;
import com.dokdok.gathering.entity.Gathering;
import com.dokdok.gathering.entity.GatheringMember;
import com.dokdok.gathering.exception.GatheringErrorCode;
import com.dokdok.gathering.exception.GatheringException;
import com.dokdok.gathering.repository.GatheringMemberRepository;
import com.dokdok.global.util.SecurityUtil;
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
	private GatheringValidator gatheringValidator;

	private MockedStatic<SecurityUtil> securityUtilMock;

	private User leader;
	private User member;
	private Gathering gathering1;
	private Gathering gathering2;
	private GatheringMember leaderMember;
	private GatheringMember normalMember;

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

		gathering1 = Gathering.builder()
				.id(1L)
				.gatheringName("독서 모임")
				.description("열심히 읽는 모임")
				.gatheringStatus("ACTIVE")
				.invitationLink("https://invite.link/abc123")
				.gatheringLeader(leader)
				.createdAt(LocalDateTime.now().minusDays(30))
				.updatedAt(LocalDateTime.now().minusDays(30))
				.build();

		gathering2 = Gathering.builder()
				.id(2L)
				.gatheringName("bookbook")
				.description("test test")
				.gatheringStatus("ACTIVE")
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
				.joinedAt(LocalDateTime.now().minusDays(10))
				.build();
	}

	@AfterEach
	void tearDown() {
		securityUtilMock.close();
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

		List<GatheringMember> members = List.of(member1, member2);
		Page<GatheringMember> memberPage = new PageImpl<>(members, pageable, members.size());

		given(gatheringMemberRepository.findActiveGatheringsByUserId(userId, pageable)).willReturn(memberPage);
		given(gatheringMemberRepository.countByGatheringIdAndRemovedAtIsNull(1L)).willReturn(1);
		given(gatheringMemberRepository.countByGatheringIdAndRemovedAtIsNull(2L)).willReturn(1);

		// when
		MyGatheringListResponse response = gatheringService.getMyGatherings(pageable);

		// then
		assertThat(response).isNotNull();
		assertThat(response.gatherings()).hasSize(2);
		assertThat(response.totalCount()).isEqualTo(2);
		assertThat(response.currentPage()).isEqualTo(0);
		assertThat(response.pageSize()).isEqualTo(10);
		assertThat(response.totalPages()).isEqualTo(1);

		GatheringSimpleResponse firstGathering = response.gatherings().get(0);
		assertThat(firstGathering.gatheringId()).isEqualTo(1L);
		assertThat(firstGathering.gatheringName()).isEqualTo("독서 모임");
		assertThat(firstGathering.isFavorite()).isTrue();
		assertThat(firstGathering.gatheringStatus()).isEqualTo("ACTIVE");
		assertThat(firstGathering.totalMembers()).isEqualTo(1);
		assertThat(firstGathering.currentUserRole()).isEqualTo(LEADER);
		assertThat(firstGathering.daysFromJoined()).isEqualTo(10);

		GatheringSimpleResponse secondGathering = response.gatherings().get(1);
		assertThat(secondGathering.gatheringId()).isEqualTo(2L);
		assertThat(secondGathering.gatheringName()).isEqualTo("bookbook");
		assertThat(secondGathering.isFavorite()).isFalse();
		assertThat(secondGathering.gatheringStatus()).isEqualTo("ACTIVE");
		assertThat(secondGathering.totalMembers()).isEqualTo(1);
		assertThat(secondGathering.currentUserRole()).isEqualTo(MEMBER);

		securityUtilMock.verify(SecurityUtil::getCurrentUserId, times(1));
		verify(gatheringMemberRepository, times(1)).findActiveGatheringsByUserId(eq(userId), any(Pageable.class));
		verify(gatheringMemberRepository, times(1)).countByGatheringIdAndRemovedAtIsNull(1L);
		verify(gatheringMemberRepository, times(1)).countByGatheringIdAndRemovedAtIsNull(2L);
	}

	@Test
	@DisplayName("내 모임 목록이 비어있을 때")
	void getMyGatherings_EmptyList() {
		// given
		Long userId = 1L;
		Pageable pageable = PageRequest.of(0, 10);

		securityUtilMock.when(SecurityUtil::getCurrentUserId).thenReturn(userId);

		Page<GatheringMember> emptyPage = new PageImpl<>(List.of(), pageable, 0);

		given(gatheringMemberRepository.findActiveGatheringsByUserId(userId, pageable)).willReturn(emptyPage);

		// when
		MyGatheringListResponse response = gatheringService.getMyGatherings(pageable);

		// then
		assertThat(response).isNotNull();
		assertThat(response.gatherings()).isEmpty();
		assertThat(response.totalCount()).isEqualTo(0);
		assertThat(response.totalPages()).isEqualTo(0);

		securityUtilMock.verify(SecurityUtil::getCurrentUserId, times(1));
		verify(gatheringMemberRepository, times(1)).findActiveGatheringsByUserId(eq(userId), any(Pageable.class));
		verify(gatheringMemberRepository, times(0)).countByGatheringIdAndRemovedAtIsNull(any());
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

		// when
		GatheringDetailResponse response = gatheringService.getGatheringDetail(gatheringId);

		// then
		assertThat(response).isNotNull();
		assertThat(response.gatheringId()).isEqualTo(1L);
		assertThat(response.gatheringName()).isEqualTo("독서 모임");
		assertThat(response.description()).isEqualTo("열심히 읽는 모임");
		assertThat(response.gatheringStatus()).isEqualTo("ACTIVE");
		assertThat(response.invitationLink()).isEqualTo("https://invite.link/abc123");
		assertThat(response.currentUserRole()).isEqualTo(MEMBER);
		assertThat(response.totalMembers()).isEqualTo(2);
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
}

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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GatheringService 테스트")
class GatheringServiceTest {

    @InjectMocks
    private GatheringService gatheringService;

    @Mock
    private GatheringMemberRepository gatheringMemberRepository;

    @Mock
    private GatheringRepository gatheringRepository;

    private MockedStatic<SecurityUtil> securityUtilMock;

    @BeforeEach
    void setUp() {
        securityUtilMock = mockStatic(SecurityUtil.class);
    }

    @AfterEach
    void tearDown() {
        securityUtilMock.close();
    }

    @Test
    @DisplayName("내 모임 목록 조회 성공")
    void getMyGatherings_Success(){
        //given
        Long userId = 1L;
        Pageable pageable = PageRequest.of(0,10);

        securityUtilMock.when(SecurityUtil::getCurrentUserId).thenReturn(userId);

        User user = User.builder()
                .id(userId)
                .build();

        Gathering gathering1 = Gathering.builder()
                .id(1L)
                .gatheringName("Crew1")
                .description("test1")
                .gatheringStatus("ACTIVE")
                .gatheringLeader(user)
                .build();

        Gathering gathering2 = Gathering.builder()
                .id(2L)
                .gatheringName("bookbook")
                .description("test test")
                .gatheringStatus("ACTIVE")
                .gatheringLeader(user)
                .build();

        GatheringMember member1 = GatheringMember.builder()
                .id(1L)
                .gathering(gathering1)
                .user(user)
                .isFavorite(true)
                .role(LEADER)
                .joinedAt(LocalDateTime.now().minusDays(10))
                .build();

        GatheringMember member2 = GatheringMember.builder()
                .id(2L)
                .gathering(gathering2)
                .user(user)
                .isFavorite(false)
                .role(MEMBER)
                .joinedAt(LocalDateTime.now().minusDays(5))
                .build();

        List<GatheringMember> members = List.of(member1, member2);
        Page<GatheringMember> memberPage = new PageImpl<>(members, pageable, members.size());

        given(gatheringMemberRepository.findActiveGatheringsByUserId(userId, pageable)).willReturn(memberPage);
        given(gatheringMemberRepository.countActiveMembers(1L)).willReturn(1);
        given(gatheringMemberRepository.countActiveMembers(2L)).willReturn(1);

        // when
        MyGatheringListResponse response = gatheringService.getMyGatherings(pageable);

        // then
        assertThat(response).isNotNull();
        assertThat(response.gatherings()).hasSize(2);
        assertThat(response.totalCount()).isEqualTo(2);
        assertThat(response.currentPage()).isEqualTo(0);
        assertThat(response.pageSize()).isEqualTo(10);
        assertThat(response.totalPages()).isEqualTo(1);

        // 첫 번째 모임 검증
         GatheringSimpleResponse firstGathering = response.gatherings().get(0);
         assertThat(firstGathering.gatheringId()).isEqualTo(1L);
         assertThat(firstGathering.gatheringName()).isEqualTo("Crew1");
         assertThat(firstGathering.isFavorite()).isTrue();
         assertThat(firstGathering.gatheringStatus()).isEqualTo("ACTIVE");
         assertThat(firstGathering.totalMembers()).isEqualTo(1);
         assertThat(firstGathering.currentUserRole()).isEqualTo(LEADER);
         assertThat(firstGathering.daysFromJoined()).isEqualTo(10);

         // 두 번째 모임 검증
         GatheringSimpleResponse secondGathering = response.gatherings().get(1);
         assertThat(secondGathering.gatheringId()).isEqualTo(2L);
         assertThat(secondGathering.gatheringName()).isEqualTo("bookbook");
         assertThat(secondGathering.isFavorite()).isFalse();
        assertThat(firstGathering.gatheringStatus()).isEqualTo("ACTIVE");
         assertThat(secondGathering.totalMembers()).isEqualTo(1);
         assertThat(secondGathering.currentUserRole()).isEqualTo(MEMBER);

         securityUtilMock.verify(SecurityUtil::getCurrentUserId, times(1));
         verify(gatheringMemberRepository, times(1)).findActiveGatheringsByUserId(eq(userId), any(Pageable.class));
         verify(gatheringMemberRepository, times(1)).countActiveMembers(1L);
         verify(gatheringMemberRepository, times(1)).countActiveMembers(2L);
    }

    @Test
    @DisplayName("내 모임 목록이 비어있을 때")
    void getMyGatherings_EmptyList(){
        //given
        Long userId = 1L;
        Pageable pageable = PageRequest.of(0,10);

        securityUtilMock.when(SecurityUtil::getCurrentUserId).thenReturn(userId);

        Page<GatheringMember> emptyPage = new PageImpl<>(List.of(), pageable, 0);

        given(gatheringMemberRepository.findActiveGatheringsByUserId(userId, pageable)).willReturn(emptyPage);

        //when
        MyGatheringListResponse response = gatheringService.getMyGatherings(pageable);

        //then
        assertThat(response).isNotNull();
        assertThat(response.gatherings()).isEmpty();
        assertThat(response.totalCount()).isEqualTo(0);
        assertThat(response.totalPages()).isEqualTo(0);

        securityUtilMock.verify(SecurityUtil::getCurrentUserId, times(1));
        verify(gatheringMemberRepository, times(1)).findActiveGatheringsByUserId(eq(userId), any(Pageable.class));
        verify(gatheringMemberRepository, times(0)).countActiveMembers(any());
    }

    @Test
    @DisplayName("모임 상세 조회 성공 - 일반 멤버")
    void getGatheringDetail_Success_AsMember() {
        // given
        Long gatheringId = 1L;
        Long userId = 2L;

        securityUtilMock.when(SecurityUtil::getCurrentUserId).thenReturn(userId);

        User leader = User.builder()
                .id(1L)
                .nickname("리더닉네임")
                .profileImageUrl("leader.jpg")
                .build();

        User member = User.builder()
                .id(userId)
                .nickname("멤버닉네임")
                .profileImageUrl("member.jpg")
                .build();

        Gathering gathering = Gathering.builder()
                .id(gatheringId)
                .gatheringName("독서 모임")
                .description("열심히 읽는 모임")
                .gatheringStatus("ACTIVE")
                .invitationLink("https://invite.link/abc123")
                .gatheringLeader(leader)
                .build();

        GatheringMember leaderMember = GatheringMember.builder()
                .id(1L)
                .gathering(gathering)
                .user(leader)
                .role(LEADER)
                .joinedAt(LocalDateTime.now().minusDays(30))
                .build();

        GatheringMember normalMember = GatheringMember.builder()
                .id(2L)
                .gathering(gathering)
                .user(member)
                .role(MEMBER)
                .joinedAt(LocalDateTime.now().minusDays(10))
                .build();

        List<GatheringMember> allMembers = List.of(leaderMember, normalMember);

        given(gatheringRepository.findById(gatheringId)).willReturn(Optional.of(gathering));
        given(gatheringMemberRepository.findByGatheringIdAndUserId(gatheringId, userId))
                .willReturn(Optional.of(normalMember));
        given(gatheringMemberRepository.findAllMembersByGatheringId(gatheringId))
                .willReturn(allMembers);

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

        // 리더 정보 검증
        GatheringDetailResponse.MemberInfo leaderInfo = response.members().stream()
                .filter(m -> m.role() == LEADER)
                .findFirst()
                .orElseThrow();
        assertThat(leaderInfo.userId()).isEqualTo(1L);
        assertThat(leaderInfo.nickname()).isEqualTo("리더닉네임");
        assertThat(leaderInfo.role()).isEqualTo(LEADER);

        // 일반 멤버 정보 검증
        GatheringDetailResponse.MemberInfo memberInfo = response.members().stream()
                .filter(m -> m.role() == MEMBER)
                .findFirst()
                .orElseThrow();
        assertThat(memberInfo.userId()).isEqualTo(2L);
        assertThat(memberInfo.nickname()).isEqualTo("멤버닉네임");
        assertThat(memberInfo.role()).isEqualTo(MEMBER);

        securityUtilMock.verify(SecurityUtil::getCurrentUserId, times(1));
        verify(gatheringRepository, times(1)).findById(gatheringId);
        verify(gatheringMemberRepository, times(1)).findByGatheringIdAndUserId(gatheringId, userId);
        verify(gatheringMemberRepository, times(1)).findAllMembersByGatheringId(gatheringId);
    }

    @Test
    @DisplayName("모임 상세 조회 실패 - 모임이 존재하지 않음")
    void getGatheringDetail_Fail_GatheringNotFound() {
        // given
        Long gatheringId = 999L;
        Long userId = 1L;

        securityUtilMock.when(SecurityUtil::getCurrentUserId).thenReturn(userId);

        given(gatheringRepository.findById(gatheringId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> gatheringService.getGatheringDetail(gatheringId))
                .isInstanceOf(GatheringException.class)
                .hasMessage(GatheringErrorCode.GATHERING_NOT_FOUND.getMessage());

        securityUtilMock.verify(SecurityUtil::getCurrentUserId, times(1));
        verify(gatheringRepository, times(1)).findById(gatheringId);
        verify(gatheringMemberRepository, times(0)).findByGatheringIdAndUserId(any(), any());
    }

    @Test
    @DisplayName("모임 상세 조회 실패 - 모임 멤버가 아님")
    void getGatheringDetail_Fail_NotGatheringMember() {
        // given
        Long gatheringId = 1L;
        Long userId = 999L;

        securityUtilMock.when(SecurityUtil::getCurrentUserId).thenReturn(userId);

        User leader = User.builder()
                .id(1L)
                .nickname("리더")
                .build();

        Gathering gathering = Gathering.builder()
                .id(gatheringId)
                .gatheringName("독서 모임")
                .gatheringLeader(leader)
                .build();

        given(gatheringRepository.findById(gatheringId)).willReturn(Optional.of(gathering));
        given(gatheringMemberRepository.findByGatheringIdAndUserId(gatheringId, userId))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> gatheringService.getGatheringDetail(gatheringId))
                .isInstanceOf(GatheringException.class)
                .hasMessage(GatheringErrorCode.NOT_GATHERING_MEMBER.getMessage());

        securityUtilMock.verify(SecurityUtil::getCurrentUserId, times(1));
        verify(gatheringRepository, times(1)).findById(gatheringId);
        verify(gatheringMemberRepository, times(1)).findByGatheringIdAndUserId(gatheringId, userId);
        verify(gatheringMemberRepository, times(0)).findAllMembersByGatheringId(any());
    }
}

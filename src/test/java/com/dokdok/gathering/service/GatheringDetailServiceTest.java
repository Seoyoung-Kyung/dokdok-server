package com.dokdok.gathering.service;

import com.dokdok.gathering.dto.GatheringDetailResponse;
import com.dokdok.gathering.entity.Gathering;
import com.dokdok.gathering.entity.GatheringMember;
import com.dokdok.gathering.entity.GatheringRole;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GatheringDetailService 테스트")
public class GatheringDetailServiceTest {
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
                .role(GatheringRole.LEADER)
                .joinedAt(LocalDateTime.now().minusDays(30))
                .build();

        GatheringMember normalMember = GatheringMember.builder()
                .id(2L)
                .gathering(gathering)
                .user(member)
                .role(GatheringRole.MEMBER)
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
        assertThat(response.currentUserRole()).isEqualTo(GatheringRole.MEMBER);
        assertThat(response.totalMembers()).isEqualTo(2);
        assertThat(response.members()).hasSize(2);

        // 리더 정보 검증
        GatheringDetailResponse.MemberInfo leaderInfo = response.members().stream()
                .filter(m -> m.role() == GatheringRole.LEADER)
                .findFirst()
                .orElseThrow();
        assertThat(leaderInfo.userId()).isEqualTo(1L);
        assertThat(leaderInfo.nickname()).isEqualTo("리더닉네임");
        assertThat(leaderInfo.role()).isEqualTo(GatheringRole.LEADER);

        // 일반 멤버 정보 검증
        GatheringDetailResponse.MemberInfo memberInfo = response.members().stream()
                .filter(m -> m.role() == GatheringRole.MEMBER)
                .findFirst()
                .orElseThrow();
        assertThat(memberInfo.userId()).isEqualTo(2L);
        assertThat(memberInfo.nickname()).isEqualTo("멤버닉네임");
        assertThat(memberInfo.role()).isEqualTo(GatheringRole.MEMBER);

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

package com.dokdok.gathering.service;

import com.dokdok.gathering.dto.GatheringSimpleResponse;
import com.dokdok.gathering.dto.MyGatheringListResponse;
import com.dokdok.gathering.entity.Gathering;
import com.dokdok.gathering.entity.GatheringMember;
import com.dokdok.gathering.repository.GatheringMemberRepository;
import com.dokdok.user.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("GatheringService 테스트")
class GatheringServiceTest {

    @InjectMocks
    private GatheringService gatheringService;

    @Mock
    private GatheringMemberRepository gatheringMemberRepository;

    @Test
    @DisplayName("내 모임 목록 조회 성공")
    void getMyGatherings_Success(){
        //given
        Long userId = 1L;
        Pageable pageable = PageRequest.of(0,10);

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
                .role("LEADER")
                .joinedAt(LocalDateTime.now().minusDays(10))
                .build();

        GatheringMember member2 = GatheringMember.builder()
                .id(2L)
                .gathering(gathering2)
                .user(user)
                .isFavorite(false)
                .role("MEMBER")
                .joinedAt(LocalDateTime.now().minusDays(5))
                .build();

        List<GatheringMember> members = List.of(member1, member2);
        Page<GatheringMember> memberPage = new PageImpl<>(members, pageable, members.size());

        given(gatheringMemberRepository.findActiveGatheringsByUserId(userId, pageable)).willReturn(memberPage);
        given(gatheringMemberRepository.countActiveMembers(1L)).willReturn(1);
        given(gatheringMemberRepository.countActiveMembers(2L)).willReturn(1);

        // when
        MyGatheringListResponse response = gatheringService.getMyGatherings(userId, pageable);

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
         assertThat(firstGathering.currentUserRole()).isEqualTo("LEADER");
         assertThat(firstGathering.daysFromJoined()).isEqualTo(10);

         // 두 번째 모임 검증
         GatheringSimpleResponse secondGathering = response.gatherings().get(1);
         assertThat(secondGathering.gatheringId()).isEqualTo(2L);
         assertThat(secondGathering.gatheringName()).isEqualTo("bookbook");
         assertThat(secondGathering.isFavorite()).isFalse();
        assertThat(firstGathering.gatheringStatus()).isEqualTo("ACTIVE");
         assertThat(secondGathering.totalMembers()).isEqualTo(1);
         assertThat(secondGathering.currentUserRole()).isEqualTo("MEMBER");

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
        Page<GatheringMember> emptyPage = new PageImpl<>(List.of(), pageable, 0);

        given(gatheringMemberRepository.findActiveGatheringsByUserId(userId, pageable)).willReturn(emptyPage);

        //when
        MyGatheringListResponse response = gatheringService.getMyGatherings(userId, pageable);

        //then
        assertThat(response).isNotNull();
        assertThat(response.gatherings()).isEmpty();
        assertThat(response.totalCount()).isEqualTo(0);
        assertThat(response.totalPages()).isEqualTo(0);

        verify(gatheringMemberRepository, times(1)).findActiveGatheringsByUserId(eq(userId), any(Pageable.class));
        verify(gatheringMemberRepository, times(0)).countActiveMembers(any());
    }
}

package com.dokdok.meeting.service;

import com.dokdok.book.entity.Book;
import com.dokdok.book.repository.BookRepository;
import com.dokdok.gathering.entity.Gathering;
import com.dokdok.gathering.exception.GatheringErrorCode;
import com.dokdok.gathering.exception.GatheringException;
import com.dokdok.gathering.repository.GatheringMemberRepository;
import com.dokdok.gathering.repository.GatheringRepository;
import com.dokdok.gathering.service.GatheringValidator;
import com.dokdok.global.util.SecurityUtil;
import com.dokdok.meeting.dto.MeetingCreateRequest;
import com.dokdok.meeting.dto.MeetingResponse;
import com.dokdok.meeting.dto.MeetingStatusResponse;
import com.dokdok.meeting.entity.Meeting;
import com.dokdok.meeting.entity.MeetingMember;
import com.dokdok.meeting.entity.MeetingMemberRole;
import com.dokdok.meeting.entity.MeetingStatus;
import com.dokdok.meeting.exception.MeetingErrorCode;
import com.dokdok.meeting.exception.MeetingException;
import com.dokdok.meeting.repository.MeetingMemberRepository;
import com.dokdok.meeting.repository.MeetingRepository;
import com.dokdok.topic.repository.TopicRepository;
import com.dokdok.user.entity.User;
import com.dokdok.user.service.UserValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MeetingServiceTest {

    @InjectMocks
    private MeetingService meetingService;

    @Mock
    private MeetingRepository meetingRepository;

    @Mock
    private MeetingMemberRepository meetingMemberRepository;

    @Mock
    private TopicRepository topicRepository;

    @Mock
    private GatheringRepository gatheringRepository;

    @Mock
    private GatheringMemberRepository gatheringMemberRepository;

    @Mock
    private GatheringValidator gatheringValidator;

    @Mock
    private MeetingValidator meetingValidator;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private UserValidator userValidator;

    private Meeting meeting;
    private Long meetingId;
    private Gathering gathering;
    private User leader;

    @BeforeEach
    void setUp() {
        meetingId = 1L;
        leader = User.builder()
                .id(10L)
                .nickname("leader")
                .build();
        gathering = Gathering.builder()
                .id(100L)
                .gatheringName("gathering")
                .gatheringLeader(leader)
                .invitationLink("link")
                .build();
        meeting = Meeting.builder()
                .id(meetingId)
                .meetingName("Meeting 1")
                .meetingStatus(MeetingStatus.PENDING)
                .meetingLeader(leader)
                .gathering(gathering)
                .build();
    }

    @DisplayName("약속을 조회하면 상세 응답을 반환한다.")
    @Test
    void givenMeetingId_whenFindMeeting_thenMeetingResponse() {
        // given
        given(meetingValidator.findMeetingOrThrow(meetingId))
                .willReturn(meeting);
        given(meetingMemberRepository.findAllByMeetingId(meetingId))
                .willReturn(java.util.Collections.emptyList());
        given(topicRepository.findAllByMeetingId(meetingId))
                .willReturn(java.util.Collections.emptyList());

        // when
        MeetingResponse findMeeting = meetingService.findMeeting(meetingId);

        // then
        assertThat(findMeeting.meetingName()).isEqualTo(meeting.getMeetingName());
        assertThat(findMeeting.meetingStatus()).isEqualTo(meeting.getMeetingStatus());

    }

    @DisplayName("존재하지 않는 약속을 조회하면 예외를 던진다.")
    @Test
    void givenMissingMeetingId_whenFindMeeting_thenThrowMeetingException() {
        // given
        Long meetingId = 999L;

        given(meetingValidator.findMeetingOrThrow(meetingId))
                .willThrow(new MeetingException(MeetingErrorCode.MEETING_NOT_FOUND));

        // when + then
        assertThatThrownBy(() -> meetingService.findMeeting(meetingId))
                .isInstanceOf(MeetingException.class)
                .extracting("errorCode")
                .isEqualTo(MeetingErrorCode.MEETING_NOT_FOUND);
    }

    @DisplayName("약속 생성 요청을 처리하면 약속 응답을 반환한다.")
    @Test
    void givenMeetingCreateRequest_whenCreateMeeting_thenMeetingResponse() {
        // given
        Long gatheringId = 3L;
        Long bookId = 12L;
        Long userId = 7L;
        LocalDateTime startDate = LocalDateTime.of(2024, 1, 20, 20, 0);
        int memberCount = 5;
        MeetingCreateRequest request = MeetingCreateRequest.builder()
                .gatheringId(gatheringId)
                .bookId(bookId)
                .meetingName(null)
                .meetingStartDate(startDate)
                .meetingEndDate(null)
                .maxParticipants(null)
                .place(null)
                .build();

        User user = User.builder()
                .id(userId)
                .nickname("leader")
                .build();

        Gathering gathering = Gathering.builder()
                .id(gatheringId)
                .gatheringName("gathering")
                .gatheringLeader(user)
                .invitationLink("link")
                .build();

        Book book = Book.builder()
                .id(bookId)
                .bookName("book")
                .build();

        Meeting savedMeeting = Meeting.builder()
                .id(25L)
                .gathering(gathering)
                .book(book)
                .meetingLeader(user)
                .meetingName(book.getBookName())
                .meetingStatus(MeetingStatus.PENDING)
                .maxParticipants(memberCount)
                .meetingStartDate(startDate)
                .meetingEndDate(null)
                .build();

        given(gatheringRepository.findById(gatheringId))
                .willReturn(Optional.of(gathering));
        given(gatheringMemberRepository.countByGatheringIdAndRemovedAtIsNull(gatheringId))
                .willReturn(memberCount);
        given(bookRepository.findById(bookId))
                .willReturn(Optional.of(book));
        given(userValidator.findUserOrThrow(userId))
                .willReturn(user);
        given(meetingRepository.save(any(Meeting.class)))
                .willReturn(savedMeeting);

        // when
        MeetingResponse response = meetingService.createMeeting(request, userId);

        // then
        assertThat(response.meetingId()).isEqualTo(savedMeeting.getId());
        assertThat(response.meetingStatus()).isEqualTo(MeetingStatus.PENDING);
        assertThat(response.meetingName()).isEqualTo(book.getBookName());
        assertThat(response.schedule().startDateTime()).isEqualTo(startDate);
        assertThat(response.participants().maxCount()).isEqualTo(memberCount);
    }

    @DisplayName("모임을 찾지 못하면 약속 생성 요청이 실패한다.")
    @Test
    void givenMissingGathering_whenCreateMeeting_thenThrowMeetingException() {
        // given
        Long gatheringId = 3L;
        Long userId = 7L;
        MeetingCreateRequest request = MeetingCreateRequest.builder()
                .gatheringId(gatheringId)
                .build();

        given(gatheringRepository.findById(gatheringId))
                .willReturn(Optional.empty());

        // when + then
        assertThatThrownBy(() -> meetingService.createMeeting(request, userId))
                .isInstanceOf(MeetingException.class)
                .extracting("errorCode")
                .isEqualTo(MeetingErrorCode.GATHERING_NOT_FOUND);
    }

    @DisplayName("책을 찾지 못하면 약속 생성 요청이 실패한다.")
    @Test
    void givenMissingBook_whenCreateMeeting_thenThrowMeetingException() {
        // given
        Long gatheringId = 3L;
        Long bookId = 12L;
        Long userId = 7L;
        MeetingCreateRequest request = MeetingCreateRequest.builder()
                .gatheringId(gatheringId)
                .bookId(bookId)
                .build();

        given(gatheringRepository.findById(gatheringId))
                .willReturn(Optional.of(Gathering.builder()
                        .id(gatheringId)
                        .gatheringName("gathering")
                        .invitationLink("link")
                        .build()));
        given(bookRepository.findById(bookId))
                .willReturn(Optional.empty());

        // when + then
        assertThatThrownBy(() -> meetingService.createMeeting(request, userId))
                .isInstanceOf(MeetingException.class)
                .extracting("errorCode")
                .isEqualTo(MeetingErrorCode.BOOK_NOT_FOUND);
    }

    @DisplayName("신청된 약속을 확정하면 약속장이 멤버로 포함된다.")
    @Test
    void givenMeetingStatus_whenChangeStatus_thenMeetingStatusChange() {
        // given
        Long meetingId = 1L;
        MeetingStatus meetingStatus = MeetingStatus.CONFIRMED;

        given(meetingValidator.findMeetingOrThrow(meetingId)).willReturn(meeting);
        given(meetingRepository.existsByGatheringIdAndMeetingStatus(gathering.getId(), MeetingStatus.CONFIRMED))
                .willReturn(false);
        given(meetingMemberRepository.findByMeetingIdAndUserId(meetingId, leader.getId()))
                .willReturn(Optional.empty());

        // when
        MeetingStatusResponse response = meetingService.changeMeetingStatus(meetingId, meetingStatus);

        // then
        assertThat(meeting.getMeetingStatus()).isEqualTo(response.meetingStatus());
        ArgumentCaptor<MeetingMember> meetingMemberCaptor = ArgumentCaptor.forClass(MeetingMember.class);
        verify(meetingMemberRepository).save(meetingMemberCaptor.capture());
        MeetingMember savedMember = meetingMemberCaptor.getValue();
        assertThat(savedMember.getUser().getId()).isEqualTo(leader.getId());
        assertThat(savedMember.getMeetingRole()).isEqualTo(MeetingMemberRole.LEADER);
    }

    @DisplayName("이미 확정된 약속이 있으면 다른 약속을 확정할 수 없다.")
    @Test
    void givenConfirmedMeetingExists_whenConfirm_thenThrowMeetingException() {
        // given
        Long meetingId = 1L;
        given(meetingValidator.findMeetingOrThrow(meetingId)).willReturn(meeting);
        given(meetingRepository.existsByGatheringIdAndMeetingStatus(gathering.getId(), MeetingStatus.CONFIRMED))
                .willReturn(true);

        // when + then
        assertThatThrownBy(() -> meetingService.changeMeetingStatus(meetingId, MeetingStatus.CONFIRMED))
                .isInstanceOf(MeetingException.class)
                .extracting("errorCode")
                .isEqualTo(MeetingErrorCode.INVALID_MEETING_STATUS_CHANGE);
    }

    @DisplayName("약속장이 없으면 약속 확정이 실패한다.")
    @Test
    void givenMissingLeader_whenConfirm_thenThrowMeetingException() {
        // given
        Long meetingId = 1L;
        Meeting missingLeaderMeeting = Meeting.builder()
                .id(meetingId)
                .meetingName("Meeting 1")
                .meetingStatus(MeetingStatus.PENDING)
                .gathering(gathering)
                .build();
        given(meetingValidator.findMeetingOrThrow(meetingId)).willReturn(missingLeaderMeeting);
        given(meetingRepository.existsByGatheringIdAndMeetingStatus(gathering.getId(), MeetingStatus.CONFIRMED))
                .willReturn(false);

        // when + then
        assertThatThrownBy(() -> meetingService.changeMeetingStatus(meetingId, MeetingStatus.CONFIRMED))
                .isInstanceOf(MeetingException.class)
                .extracting("errorCode")
                .isEqualTo(MeetingErrorCode.INVALID_MEETING_STATUS_CHANGE);
    }

    @DisplayName("확정된 약속은 다시 신청 상태로 되돌릴 수 없다.")
    @Test
    void givenConfirmedMeeting_whenRollbackToPending_thenThrowMeetingException() {
        // given
        Long meetingId = 1L;
        Meeting confirmedMeeting = Meeting.builder()
                .id(meetingId)
                .meetingName("Meeting 1")
                .meetingStatus(MeetingStatus.CONFIRMED)
                .gathering(gathering)
                .meetingLeader(leader)
                .build();
        given(meetingValidator.findMeetingOrThrow(meetingId)).willReturn(confirmedMeeting);

        // when + then
        assertThatThrownBy(() -> meetingService.changeMeetingStatus(meetingId, MeetingStatus.PENDING))
                .isInstanceOf(MeetingException.class)
                .extracting("errorCode")
                .isEqualTo(MeetingErrorCode.INVALID_MEETING_STATUS_CHANGE);

    }

    @DisplayName("약속 참가 신청을 한다.")
    @Test
    void givenMeetingId_whenMeetingJoin_thenMeetingId() {
        // given
        Long meetingId = 3L;
        Long userId = 7L;
        Meeting meeting = Meeting.builder()
                .id(meetingId)
                .gathering(Gathering.builder()
                        .id(1L)
                        .gatheringName("gathering")
                        .invitationLink("link")
                        .build())
                .build();
        User user = User.builder()
                .id(userId)
                .nickname("member")
                .build();

        given(meetingValidator.findMeetingOrThrow(meetingId))
                .willReturn(meeting);
        given(meetingMemberRepository.findAnyByMeetingIdAndUserId(meetingId, userId))
                .willReturn(Optional.empty());
        given(userValidator.findUserOrThrow(userId))
                .willReturn(user);

        try (MockedStatic<SecurityUtil> securityUtilMock = mockStatic(SecurityUtil.class)) {
            securityUtilMock.when(SecurityUtil::getCurrentUserId).thenReturn(userId);

            // when
            Long response = meetingService.joinMeeting(meetingId);

            // then
            assertThat(response).isEqualTo(meetingId);
            verify(meetingValidator).validateCapacity(meetingId, meeting.getMaxParticipants());
            verify(meetingMemberRepository).save(any());
        }

    }

    @DisplayName("이미 약속 멤버면 참가 신청이 실패한다.")
    @Test
    void givenExistingMember_whenJoinMeeting_thenThrowException() {
        // given
        Long meetingId = 3L;
        Long userId = 7L;
        Meeting meeting = Meeting.builder()
                .id(meetingId)
                .gathering(Gathering.builder()
                        .id(1L)
                        .gatheringName("gathering")
                        .invitationLink("link")
                        .build())
                .build();

        given(meetingValidator.findMeetingOrThrow(meetingId))
                .willReturn(meeting);
        given(meetingMemberRepository.findAnyByMeetingIdAndUserId(meetingId, userId))
                .willReturn(Optional.of(MeetingMember.builder()
                        .meeting(meeting)
                        .user(User.builder().id(userId).build())
                        .build()));

        try (MockedStatic<SecurityUtil> securityUtilMock = mockStatic(SecurityUtil.class)) {
            securityUtilMock.when(SecurityUtil::getCurrentUserId).thenReturn(userId);

            // when
            // then
            assertThatThrownBy(() -> meetingService.joinMeeting(meetingId))
                    .isInstanceOf(MeetingException.class)
                    .extracting("errorCode")
                    .isEqualTo(MeetingErrorCode.MEETING_ALREADY_JOINED);
            verify(meetingValidator, never()).validateCapacity(any(), any());
            verify(userValidator, never()).findUserOrThrow(any());
            verify(meetingMemberRepository, never()).save(any());
        }
    }

    @DisplayName("취소 이력이 있는 멤버는 재참여 처리된다.")
    @Test
    void givenCanceledMember_whenJoinMeeting_thenRestore() {
        // given
        Long meetingId = 3L;
        Long userId = 7L;
        Meeting meeting = Meeting.builder()
                .id(meetingId)
                .gathering(Gathering.builder()
                        .id(1L)
                        .gatheringName("gathering")
                        .invitationLink("link")
                        .build())
                .build();
        MeetingMember canceledMember = MeetingMember.builder()
                .meeting(meeting)
                .user(User.builder().id(userId).build())
                .canceledAt(LocalDateTime.now().minusDays(1))
                .build();

        given(meetingValidator.findMeetingOrThrow(meetingId))
                .willReturn(meeting);
        given(meetingMemberRepository.findAnyByMeetingIdAndUserId(meetingId, userId))
                .willReturn(Optional.of(canceledMember));

        try (MockedStatic<SecurityUtil> securityUtilMock = mockStatic(SecurityUtil.class)) {
            securityUtilMock.when(SecurityUtil::getCurrentUserId).thenReturn(userId);

            // when
            Long response = meetingService.joinMeeting(meetingId);

            // then
            assertThat(response).isEqualTo(meetingId);
            assertThat(canceledMember.getCanceledAt()).isNull();
            verify(meetingValidator, never()).validateCapacity(any(), any());
            verify(userValidator, never()).findUserOrThrow(any());
            verify(meetingMemberRepository, never()).save(any());
        }
    }

    @DisplayName("모임 멤버가 아니면 약속 참가 신청에 실패한다.")
    @Test
    void givenNotGatheringMember_whenJoinMeeting_thenThrowException() {
        // given
        Long meetingId = 3L;
        Long userId = 7L;
        Meeting meeting = Meeting.builder()
                .id(meetingId)
                .gathering(Gathering.builder()
                        .id(1L)
                        .gatheringName("gathering")
                        .invitationLink("link")
                        .build())
                .build();

        given(meetingValidator.findMeetingOrThrow(meetingId))
                .willReturn(meeting);
        doThrow(new GatheringException(GatheringErrorCode.NOT_GATHERING_MEMBER))
                .when(gatheringValidator).validateMembership(meeting.getGathering().getId(), userId);

        try (MockedStatic<SecurityUtil> securityUtilMock = mockStatic(SecurityUtil.class)) {
            securityUtilMock.when(SecurityUtil::getCurrentUserId).thenReturn(userId);

            // when + then
            assertThatThrownBy(() -> meetingService.joinMeeting(meetingId))
                    .isInstanceOf(GatheringException.class)
                    .extracting("errorCode")
                    .isEqualTo(GatheringErrorCode.NOT_GATHERING_MEMBER);
        }
    }

    @DisplayName("약속 정원이 마감되면 참가 신청에 실패한다.")
    @Test
    void givenFullMeeting_whenJoinMeeting_thenThrowException() {
        // given
        Long meetingId = 3L;
        Long userId = 7L;
        Meeting meeting = Meeting.builder()
                .id(meetingId)
                .maxParticipants(2)
                .gathering(Gathering.builder()
                        .id(1L)
                        .gatheringName("gathering")
                        .invitationLink("link")
                        .build())
                .build();

        given(meetingValidator.findMeetingOrThrow(meetingId))
                .willReturn(meeting);
        given(meetingMemberRepository.findAnyByMeetingIdAndUserId(meetingId, userId))
                .willReturn(Optional.empty());
        doThrow(new MeetingException(MeetingErrorCode.MEETING_FULL))
                .when(meetingValidator).validateCapacity(meetingId, meeting.getMaxParticipants());

        try (MockedStatic<SecurityUtil> securityUtilMock = mockStatic(SecurityUtil.class)) {
            securityUtilMock.when(SecurityUtil::getCurrentUserId).thenReturn(userId);

            // when + then
            assertThatThrownBy(() -> meetingService.joinMeeting(meetingId))
                    .isInstanceOf(MeetingException.class)
                    .extracting("errorCode")
                    .isEqualTo(MeetingErrorCode.MEETING_FULL);
        }
    }

    @DisplayName("약속 참가 신청을 취소할 수 있다.")
    @Test
    void givenMeetingId_whenMeetingCancel_thenSuccess() {
        // given
        Long userId = 7L;
        Meeting meeting = Meeting.builder()
                .id(meetingId)
                .meetingStartDate(LocalDateTime.now().plusDays(2))
                .build();
        MeetingMember meetingMember = MeetingMember.builder()
                .meeting(meeting)
                .user(User.builder().id(userId).build())
                .build();

        given(meetingValidator.findMeetingOrThrow(meetingId)).willReturn(meeting);
        given(meetingValidator.getAnyMeetingMember(meetingId, userId))
                .willReturn(meetingMember);

        try (MockedStatic<SecurityUtil> securityUtilMock = mockStatic(SecurityUtil.class)) {
            securityUtilMock.when(SecurityUtil::getCurrentUserId).thenReturn(userId);

            // when
            Long response = meetingService.cancelMeeting(meetingId);

            // then
            assertThat(response).isEqualTo(meetingId);
            assertThat(meetingMember.getCanceledAt()).isNotNull();
            verify(topicRepository).softDeleteByMeetingIdAndProposedById(meetingId, userId);
        }

    }

    @DisplayName("약속에 참가하지 않은 사람은 취소할 수 없다.")
    @Test
    void givenMeetingId_whenMeetingCancel_thenException() {
        // given
        Long userId = 7L;
        Meeting meeting = Meeting.builder()
                .id(meetingId)
                .meetingStartDate(LocalDateTime.now().plusDays(2))
                .build();

        given(meetingValidator.findMeetingOrThrow(meetingId)).willReturn(meeting);
        given(meetingValidator.getAnyMeetingMember(meetingId, userId))
                .willThrow(new MeetingException(MeetingErrorCode.NOT_MEETING_MEMBER));

        try (MockedStatic<SecurityUtil> securityUtilMock = mockStatic(SecurityUtil.class)) {
            securityUtilMock.when(SecurityUtil::getCurrentUserId).thenReturn(userId);

            // when + then
            assertThatThrownBy(() -> meetingService.cancelMeeting(meetingId))
                    .isInstanceOf(MeetingException.class)
                    .extracting("errorCode")
                    .isEqualTo(MeetingErrorCode.NOT_MEETING_MEMBER);
        }

    }

    @DisplayName("신청 마감 기한 전까지만 취소 가능하다.")
    @Test
    void givenMeetingId_whenMeetingCancel_thenThrowException() {
        // given
        Long userId = 7L;
        Meeting meeting = Meeting.builder()
                .id(meetingId)
                .meetingStartDate(LocalDateTime.now().plusHours(1))
                .build();

        given(meetingValidator.findMeetingOrThrow(meetingId)).willReturn(meeting);

        try (MockedStatic<SecurityUtil> securityUtilMock = mockStatic(SecurityUtil.class)) {
            securityUtilMock.when(SecurityUtil::getCurrentUserId).thenReturn(userId);

            // when + then
            assertThatThrownBy(() -> meetingService.cancelMeeting(meetingId))
                    .isInstanceOf(MeetingException.class)
                    .extracting("errorCode")
                    .isEqualTo(MeetingErrorCode.MEETING_CANCEL_NOT_ALLOWED);
        }

    }

    @DisplayName("주제를 등록했던 사람이 참가 취소하면 주제까지 삭제된다.")
    @Test
    void givenMeetingId_whenMeetingCancel_thenDeleteWithTopics() {
        // given
        Long userId = 7L;
        Meeting meeting = Meeting.builder()
                .id(meetingId)
                .meetingStartDate(LocalDateTime.now().plusDays(2))
                .build();
        MeetingMember meetingMember = MeetingMember.builder()
                .meeting(meeting)
                .user(User.builder().id(userId).build())
                .build();

        given(meetingValidator.findMeetingOrThrow(meetingId)).willReturn(meeting);
        given(meetingValidator.getAnyMeetingMember(meetingId, userId))
                .willReturn(meetingMember);

        try (MockedStatic<SecurityUtil> securityUtilMock = mockStatic(SecurityUtil.class)) {
            securityUtilMock.when(SecurityUtil::getCurrentUserId).thenReturn(userId);

            // when
            meetingService.cancelMeeting(meetingId);

            // then
            verify(topicRepository).softDeleteByMeetingIdAndProposedById(meetingId, userId);
        }

    }
}

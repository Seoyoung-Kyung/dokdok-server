package com.dokdok.meeting.service;

import com.dokdok.book.entity.Book;
import com.dokdok.book.repository.BookRepository;
import com.dokdok.gathering.entity.Gathering;
import com.dokdok.gathering.repository.GatheringMemberRepository;
import com.dokdok.gathering.repository.GatheringRepository;
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
import com.dokdok.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

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
    private BookRepository bookRepository;

    @Mock
    private UserRepository userRepository;

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
        given(meetingRepository.findById(meetingId))
                .willReturn(Optional.of(meeting));
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

        given(meetingRepository.findById(meetingId))
                .willReturn(Optional.empty());

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
        given(userRepository.findById(userId))
                .willReturn(Optional.of(user));
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

        given(meetingRepository.findById(meetingId)).willReturn(Optional.of(meeting));
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
        given(meetingRepository.findById(meetingId)).willReturn(Optional.of(meeting));
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
        given(meetingRepository.findById(meetingId)).willReturn(Optional.of(missingLeaderMeeting));
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
        given(meetingRepository.findById(meetingId)).willReturn(Optional.of(confirmedMeeting));

        // when + then
        assertThatThrownBy(() -> meetingService.changeMeetingStatus(meetingId, MeetingStatus.PENDING))
                .isInstanceOf(MeetingException.class)
                .extracting("errorCode")
                .isEqualTo(MeetingErrorCode.INVALID_MEETING_STATUS_CHANGE);

    }
}

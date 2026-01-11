package com.dokdok.meeting.service;

import com.dokdok.book.entity.Book;
import com.dokdok.book.repository.BookRepository;
import com.dokdok.gathering.entity.Gathering;
import com.dokdok.gathering.repository.GatheringMemberRepository;
import com.dokdok.gathering.repository.GatheringRepository;
import com.dokdok.meeting.dto.MeetingCreateRequest;
import com.dokdok.meeting.dto.MeetingResponse;
import com.dokdok.meeting.entity.Meeting;
import com.dokdok.meeting.entity.MeetingStatus;
import com.dokdok.meeting.exception.MeetingErrorCode;
import com.dokdok.meeting.exception.MeetingException;
import com.dokdok.meeting.repository.MeetingMemberRepository;
import com.dokdok.meeting.repository.MeetingRepository;
import com.dokdok.topic.repository.TopicRepository;
import com.dokdok.user.entity.User;
import com.dokdok.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

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

    @Test
    void givenMeetingId_whenFindMeeting_thenMeetingResponse() {
        // given
        Long meetingId = 1L;
        Meeting meeting = Meeting.builder()
                .id(meetingId)
                .meetingName("Meeting 1")
                .meetingStatus(MeetingStatus.PENDING)
                .build();

        given(meetingRepository.findById(meetingId))
                .willReturn(Optional.of(meeting));
        given(meetingMemberRepository.findAllByMeetingId(meetingId))
                .willReturn(java.util.Collections.emptyList());
        given(topicRepository.findAllByMeetingId(meetingId))
                .willReturn(java.util.Collections.emptyList());

        // when
        MeetingResponse findMeeting = meetingService.findMeeting(meetingId);

        // then
        assertThat(findMeeting.getMeetingName()).isEqualTo(meeting.getMeetingName());
        assertThat(findMeeting.getMeetingStatus()).isEqualTo(meeting.getMeetingStatus());

    }

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
        assertThat(response.getMeetingId()).isEqualTo(savedMeeting.getId());
        assertThat(response.getMeetingStatus()).isEqualTo(MeetingStatus.PENDING);
        assertThat(response.getMeetingName()).isEqualTo(book.getBookName());
        assertThat(response.getSchedule().getStartDateTime()).isEqualTo(startDate);
        assertThat(response.getParticipants().getMaxCount()).isEqualTo(memberCount);
    }

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
}

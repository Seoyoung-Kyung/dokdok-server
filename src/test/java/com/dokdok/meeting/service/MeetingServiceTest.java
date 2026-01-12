package com.dokdok.meeting.service;

import com.dokdok.meeting.dto.MeetingResponse;
import com.dokdok.meeting.entity.Meeting;
import com.dokdok.meeting.entity.MeetingStatus;
import com.dokdok.meeting.exception.MeetingErrorCode;
import com.dokdok.meeting.exception.MeetingException;
import com.dokdok.meeting.repository.MeetingMemberRepository;
import com.dokdok.meeting.repository.MeetingRepository;
import com.dokdok.topic.repository.TopicRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
        assertThat(findMeeting.meetingName()).isEqualTo(meeting.getMeetingName());
        assertThat(findMeeting.meetingStatus()).isEqualTo(meeting.getMeetingStatus());

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
}

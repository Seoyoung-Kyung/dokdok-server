package com.dokdok.retrospective.service;

import com.dokdok.gathering.entity.Gathering;
import com.dokdok.gathering.exception.GatheringErrorCode;
import com.dokdok.gathering.exception.GatheringException;
import com.dokdok.global.exception.GlobalErrorCode;
import com.dokdok.global.exception.GlobalException;
import com.dokdok.global.util.SecurityUtil;
import com.dokdok.meeting.entity.Meeting;
import com.dokdok.meeting.exception.MeetingErrorCode;
import com.dokdok.meeting.exception.MeetingException;
import com.dokdok.meeting.repository.MeetingRepository;
import com.dokdok.retrospective.dto.response.MeetingRetrospectiveResponse;
import com.dokdok.retrospective.entity.MeetingRetrospective;
import com.dokdok.retrospective.entity.TopicRetrospectiveSummary;
import com.dokdok.retrospective.repository.RetrospectiveRepository;
import com.dokdok.retrospective.repository.TopicRetrospectiveSummaryRepository;
import com.dokdok.topic.entity.Topic;
import com.dokdok.topic.entity.TopicStatus;
import com.dokdok.topic.repository.TopicRepository;
import com.dokdok.user.entity.User;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MeetingRetrospectiveServiceTest {

	@Mock
	private MeetingRepository meetingRepository;

	@Mock
	private TopicRepository topicRepository;

	@Mock
	private RetrospectiveValidator retrospectiveValidator;

	@Mock
	private TopicRetrospectiveSummaryRepository topicRetrospectiveSummaryRepository;

	@Mock
	private RetrospectiveRepository retrospectiveRepository;

	@InjectMocks
	private MeetingRetrospectiveService meetingRetrospectiveService;

	@Test
	@DisplayName("약속이 없으면 예외가 발생한다")
	void getMeetingRetrospective_throwsWhenMeetingNotFound() {
		// given
		Long meetingId = 999L;
		Long userId = 1L;

		try (MockedStatic<SecurityUtil> securityUtilMock = mockStatic(SecurityUtil.class)) {
			securityUtilMock.when(SecurityUtil::getCurrentUserId).thenReturn(userId);

			when(meetingRepository.findById(meetingId)).thenReturn(Optional.empty());

			// when & then
			assertThatThrownBy(() -> meetingRetrospectiveService.getMeetingRetrospective(meetingId))
					.isInstanceOf(MeetingException.class)
					.hasFieldOrPropertyWithValue("errorCode", MeetingErrorCode.MEETING_NOT_FOUND);

			verify(retrospectiveValidator, never()).validateMeetingRetrospectiveAccess(any(), any(), any());
		}
	}

	@Test
	@DisplayName("모임 멤버가 아니면 예외가 발생한다")
	void getMeetingRetrospective_throwsWhenNotGatheringMember() {
		// given
		Long meetingId = 1L;
		Long userId = 999L;
		Long gatheringId = 1L;

		Gathering gathering = Gathering.builder().id(gatheringId).build();
		Meeting meeting = Meeting.builder().id(meetingId).gathering(gathering).build();

		try (MockedStatic<SecurityUtil> securityUtilMock = mockStatic(SecurityUtil.class)) {
			securityUtilMock.when(SecurityUtil::getCurrentUserId).thenReturn(userId);

			when(meetingRepository.findById(meetingId)).thenReturn(Optional.of(meeting));
			doThrow(new GatheringException(GatheringErrorCode.NOT_GATHERING_MEMBER))
					.when(retrospectiveValidator).validateMeetingRetrospectiveAccess(gatheringId, meetingId, userId);

			// when & then
			assertThatThrownBy(() -> meetingRetrospectiveService.getMeetingRetrospective(meetingId))
					.isInstanceOf(GatheringException.class)
					.hasFieldOrPropertyWithValue("errorCode", GatheringErrorCode.NOT_GATHERING_MEMBER);

			verify(topicRepository, never()).findByMeetingIdAndTopicStatusOrderByConfirmOrderAsc(any(), any());
		}
	}

	@Test
	@DisplayName("약속 멤버가 아니면 예외가 발생한다")
	void getMeetingRetrospective_throwsWhenNotMeetingMember() {
		// given
		Long meetingId = 1L;
		Long userId = 999L;
		Long gatheringId = 1L;

		Gathering gathering = Gathering.builder().id(gatheringId).build();
		Meeting meeting = Meeting.builder().id(meetingId).gathering(gathering).build();

		try (MockedStatic<SecurityUtil> securityUtilMock = mockStatic(SecurityUtil.class)) {
			securityUtilMock.when(SecurityUtil::getCurrentUserId).thenReturn(userId);

			when(meetingRepository.findById(meetingId)).thenReturn(Optional.of(meeting));
			doThrow(new MeetingException(MeetingErrorCode.NOT_GATHERING_MEETING))
					.when(retrospectiveValidator).validateMeetingRetrospectiveAccess(gatheringId, meetingId, userId);

			// when & then
			assertThatThrownBy(() -> meetingRetrospectiveService.getMeetingRetrospective(meetingId))
					.isInstanceOf(MeetingException.class)
					.hasFieldOrPropertyWithValue("errorCode", MeetingErrorCode.NOT_GATHERING_MEETING);

			verify(topicRepository, never()).findByMeetingIdAndTopicStatusOrderByConfirmOrderAsc(any(), any());
		}
	}

	@Test
	@DisplayName("확정된 토픽이 없으면 빈 목록을 반환한다")
	void getMeetingRetrospective_withNoTopics_returnsEmptyList() {
		// given
		Long meetingId = 1L;
		Long userId = 1L;
		Long gatheringId = 1L;

		Gathering gathering = Gathering.builder().id(gatheringId).build();
		Meeting meeting = Meeting.builder()
				.id(meetingId)
				.gathering(gathering)
				.meetingName("모임")
				.meetingStartDate(LocalDateTime.of(2026, 1, 15, 19, 0))
				.meetingEndDate(LocalDateTime.of(2026, 1, 15, 21, 0))
				.build();

		try (MockedStatic<SecurityUtil> securityUtilMock = mockStatic(SecurityUtil.class)) {
			securityUtilMock.when(SecurityUtil::getCurrentUserId).thenReturn(userId);

			when(meetingRepository.findById(meetingId)).thenReturn(Optional.of(meeting));
			doNothing().when(retrospectiveValidator).validateMeetingRetrospectiveAccess(gatheringId, meetingId, userId);
			when(topicRepository.findByMeetingIdAndTopicStatusOrderByConfirmOrderAsc(meetingId, TopicStatus.CONFIRMED))
					.thenReturn(List.of());
			when(topicRetrospectiveSummaryRepository.findAllByTopicIdIn(List.of())).thenReturn(List.of());
			when(retrospectiveRepository.findAllByMeetingId(meetingId)).thenReturn(List.of());

			// when
			MeetingRetrospectiveResponse response = meetingRetrospectiveService.getMeetingRetrospective(meetingId);

			// then
			assertThat(response.meetingId()).isEqualTo(meetingId);
			assertThat(response.topics()).isEmpty();
		}
	}

	@Test
	@DisplayName("코멘트가 없어도 정상적으로 조회한다")
	void getMeetingRetrospective_withNoComments_success() {
		// given
		Long meetingId = 1L;
		Long userId = 1L;
		Long gatheringId = 1L;

		Gathering gathering = Gathering.builder().id(gatheringId).build();
		Meeting meeting = Meeting.builder()
				.id(meetingId)
				.gathering(gathering)
				.meetingName("모임")
				.meetingStartDate(LocalDateTime.of(2026, 1, 15, 19, 0))
				.meetingEndDate(LocalDateTime.of(2026, 1, 15, 21, 0))
				.build();

		Topic topic = Topic.builder().id(1L).meeting(meeting).title("토픽1").build();
		TopicRetrospectiveSummary summary = TopicRetrospectiveSummary.builder()
				.id(1L)
				.topic(topic)
				.summarizedOpinions(List.of("요약1"))
				.build();

		try (MockedStatic<SecurityUtil> securityUtilMock = mockStatic(SecurityUtil.class)) {
			securityUtilMock.when(SecurityUtil::getCurrentUserId).thenReturn(userId);

			when(meetingRepository.findById(meetingId)).thenReturn(Optional.of(meeting));
			doNothing().when(retrospectiveValidator).validateMeetingRetrospectiveAccess(gatheringId, meetingId, userId);
			when(topicRepository.findByMeetingIdAndTopicStatusOrderByConfirmOrderAsc(meetingId, TopicStatus.CONFIRMED))
					.thenReturn(List.of(topic));
			when(topicRetrospectiveSummaryRepository.findAllByTopicIdIn(List.of(1L))).thenReturn(List.of(summary));
			when(retrospectiveRepository.findAllByMeetingId(meetingId)).thenReturn(List.of());

			// when
			MeetingRetrospectiveResponse response = meetingRetrospectiveService.getMeetingRetrospective(meetingId);

			// then
			assertThat(response.topics()).hasSize(1);
			assertThat(response.topics().get(0).summarizedOpinions()).containsExactly("요약1");
			assertThat(response.topics().get(0).comments()).isEmpty();
		}
	}

	@Test
	@DisplayName("인증 정보가 없으면 예외가 발생한다")
	void getMeetingRetrospective_throwsWhenUnauthenticated() {
		// given
		Long meetingId = 1L;

		try (MockedStatic<SecurityUtil> securityUtilMock = mockStatic(SecurityUtil.class)) {
			securityUtilMock.when(SecurityUtil::getCurrentUserId)
					.thenThrow(new GlobalException(GlobalErrorCode.UNAUTHORIZED));

			// when & then
			assertThatThrownBy(() -> meetingRetrospectiveService.getMeetingRetrospective(meetingId))
					.isInstanceOf(GlobalException.class)
					.hasFieldOrPropertyWithValue("errorCode", GlobalErrorCode.UNAUTHORIZED);

			verify(meetingRepository, never()).findById(any());
		}
	}
}

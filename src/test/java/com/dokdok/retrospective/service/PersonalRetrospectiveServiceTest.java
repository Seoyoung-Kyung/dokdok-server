package com.dokdok.retrospective.service;

import com.dokdok.global.exception.GlobalErrorCode;
import com.dokdok.global.exception.GlobalException;
import com.dokdok.global.util.SecurityUtil;
import com.dokdok.meeting.entity.Meeting;
import com.dokdok.meeting.entity.MeetingMember;
import com.dokdok.meeting.service.MeetingValidator;
import com.dokdok.meeting.exception.MeetingErrorCode;
import com.dokdok.meeting.exception.MeetingException;
import com.dokdok.meeting.repository.MeetingMemberRepository;
import com.dokdok.retrospective.dto.request.PersonalRetrospectiveRequest;
import com.dokdok.retrospective.dto.response.PersonalRetrospectiveDetailResponse;
import com.dokdok.retrospective.dto.response.PersonalRetrospectiveFormResponse;
import com.dokdok.retrospective.dto.response.PersonalRetrospectiveResponse;
import com.dokdok.retrospective.entity.RetrospectiveChangedThought;
import com.dokdok.retrospective.entity.RetrospectiveFreeText;
import com.dokdok.retrospective.entity.RetrospectiveOthersPerspective;
import com.dokdok.retrospective.exception.RetrospectiveErrorCode;
import com.dokdok.retrospective.exception.RetrospectiveException;
import com.dokdok.retrospective.repository.ChangedThoughtRepository;
import com.dokdok.retrospective.repository.FreeTextRepository;
import com.dokdok.retrospective.repository.OthersPerspectiveRepository;
import com.dokdok.topic.exception.TopicErrorCode;
import com.dokdok.topic.exception.TopicException;
import com.dokdok.topic.repository.TopicAnswerRepository;
import com.dokdok.retrospective.entity.PersonalMeetingRetrospective;
import com.dokdok.retrospective.repository.PersonalRetrospectiveRepository;
import com.dokdok.topic.entity.Topic;
import com.dokdok.topic.entity.TopicAnswer;
import com.dokdok.topic.repository.TopicRepository;
import com.dokdok.topic.service.TopicValidator;
import com.dokdok.user.entity.User;
import com.dokdok.user.service.UserValidator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PersonalRetrospectiveServiceTest {

    @Mock
    private PersonalRetrospectiveRepository personalRetrospectiveRepository;

    @Mock
    private MeetingValidator meetingValidator;

    @Mock
    private UserValidator userValidator;

    @Mock
    private RetrospectiveValidator retrospectiveValidator;

    @Mock
    private TopicValidator topicValidator;

    @Mock
    private TopicRepository topicRepository;

    @Mock
    private TopicAnswerRepository topicAnswerRepository;

    @Mock
    private MeetingMemberRepository meetingMemberRepository;

    @Mock
    private ChangedThoughtRepository changedThoughtRepository;

    @Mock
    private OthersPerspectiveRepository othersPerspectiveRepository;

    @Mock
    private FreeTextRepository freeTextRepository;

    @Mock
    private PersonalRetrospectiveAssembler assembler;

    @InjectMocks
    private PersonalRetrospectiveService personalRetrospectiveService;

    @Test
    @DisplayName("개인 회고를 정상적으로 생성한다")
    void createPersonalRetrospective_success() {
        // given
        Long meetingId = 1L;
        Long userId = 3L;
        Long topicId = 10L;
        Long meetingMemberId = 5L;

        Meeting meeting = Meeting.builder().id(meetingId).build();
        User user = User.builder().id(userId).build();
        Topic topic = Topic.builder().id(topicId).title("토픽 제목").build();
        MeetingMember meetingMember = MeetingMember.builder().id(meetingMemberId).build();
        TopicAnswer topicAnswer = TopicAnswer.builder()
                .id(100L)
                .topic(topic)
                .user(user)
                .content("모임 전 의견")
                .build();

        PersonalRetrospectiveRequest.ChangedThoughtRequest changedThoughtRequest =
                new PersonalRetrospectiveRequest.ChangedThoughtRequest(
                        topicId, "핵심 쟁점", "모임 후 의견"
                );

        PersonalRetrospectiveRequest.OthersPerspectiveRequest othersPerspectiveRequest =
                new PersonalRetrospectiveRequest.OthersPerspectiveRequest(
                        topicId, meetingMemberId, "타인의 의견", "인상 깊었던 이유"
                );

        PersonalRetrospectiveRequest.FreeTextRequest freeTextRequest =
                new PersonalRetrospectiveRequest.FreeTextRequest(
                        "자유 서술 제목", "자유 서술 내용"
                );

        PersonalRetrospectiveRequest request = new PersonalRetrospectiveRequest(
                List.of(changedThoughtRequest),
                List.of(othersPerspectiveRequest),
                List.of(freeTextRequest)
        );

        PersonalMeetingRetrospective saved = PersonalMeetingRetrospective.builder()
                .id(1L)
                .meeting(meeting)
                .user(user)
                .build();

        try (MockedStatic<SecurityUtil> securityUtilMock = mockStatic(SecurityUtil.class)) {
            securityUtilMock.when(SecurityUtil::getCurrentUserId).thenReturn(userId);

            when(meetingValidator.findMeetingOrThrow(meetingId)).thenReturn(meeting);
            when(userValidator.findUserOrThrow(userId)).thenReturn(user);
            doNothing().when(retrospectiveValidator).validateRetrospective(meetingId, userId);
            when(topicValidator.getTopic(topicId)).thenReturn(topic);
            when(topicValidator.getPreOpinion(topicId, userId)).thenReturn(topicAnswer);
            when(topicRepository.findById(topicId)).thenReturn(Optional.of(topic));
            when(meetingValidator.getMeetingMember(meetingId, meetingMemberId)).thenReturn(meetingMember);
            when(personalRetrospectiveRepository.save(any(PersonalMeetingRetrospective.class))).thenReturn(saved);

            // when
            PersonalRetrospectiveResponse response = personalRetrospectiveService.createPersonalRetrospective(meetingId, request);

            // then
            assertThat(response.personalMeetingRetrospectiveId()).isEqualTo(1L);
            assertThat(response.meetingId()).isEqualTo(meetingId);
            assertThat(response.userId()).isEqualTo(userId);

            verify(meetingValidator).findMeetingOrThrow(meetingId);
            verify(userValidator).findUserOrThrow(userId);
            verify(retrospectiveValidator).validateRetrospective(meetingId, userId);
            verify(topicValidator).getTopic(topicId);
            verify(topicValidator).getPreOpinion(topicId, userId);
            verify(topicRepository).findById(topicId);
            verify(meetingValidator).getMeetingMember(meetingId, meetingMemberId);
            verify(personalRetrospectiveRepository).save(any(PersonalMeetingRetrospective.class));
        }
    }

    @Test
    @DisplayName("changedThoughts 없이 개인 회고를 생성한다")
    void createPersonalRetrospective_withoutChangedThoughts_success() {
        // given
        Long meetingId = 1L;
        Long userId = 3L;

        Meeting meeting = Meeting.builder().id(meetingId).build();
        User user = User.builder().id(userId).build();

        PersonalRetrospectiveRequest request = new PersonalRetrospectiveRequest(
                null,
                null,
                null
        );

        PersonalMeetingRetrospective saved = PersonalMeetingRetrospective.builder()
                .id(1L)
                .meeting(meeting)
                .user(user)
                .build();

        try (MockedStatic<SecurityUtil> securityUtilMock = mockStatic(SecurityUtil.class)) {
            securityUtilMock.when(SecurityUtil::getCurrentUserId).thenReturn(userId);

            when(meetingValidator.findMeetingOrThrow(meetingId)).thenReturn(meeting);
            when(userValidator.findUserOrThrow(userId)).thenReturn(user);
            doNothing().when(retrospectiveValidator).validateRetrospective(meetingId, userId);
            when(personalRetrospectiveRepository.save(any(PersonalMeetingRetrospective.class))).thenReturn(saved);

            // when
            PersonalRetrospectiveResponse response = personalRetrospectiveService.createPersonalRetrospective(meetingId, request);

            // then
            assertThat(response.personalMeetingRetrospectiveId()).isEqualTo(1L);

            verify(topicValidator, never()).getTopic(any());
            verify(topicValidator, never()).getPreOpinion(any(), any());
        }
    }

    @Test
    @DisplayName("TopicAnswer가 없어도 개인 회고를 생성한다")
    void createPersonalRetrospective_withoutTopicAnswer_success() {
        // given
        Long meetingId = 1L;
        Long userId = 3L;
        Long topicId = 10L;

        Meeting meeting = Meeting.builder().id(meetingId).build();
        User user = User.builder().id(userId).build();
        Topic topic = Topic.builder().id(topicId).title("주제 제목").build();

        PersonalRetrospectiveRequest.ChangedThoughtRequest changedThoughtRequest =
                new PersonalRetrospectiveRequest.ChangedThoughtRequest(
                        topicId, "핵심 쟁점", "약속 후 의견"
                );

        PersonalRetrospectiveRequest request = new PersonalRetrospectiveRequest(
                List.of(changedThoughtRequest),
                null,
                null
        );

        PersonalMeetingRetrospective saved = PersonalMeetingRetrospective.builder()
                .id(1L)
                .meeting(meeting)
                .user(user)
                .build();

        try (MockedStatic<SecurityUtil> securityUtilMock = mockStatic(SecurityUtil.class)) {
            securityUtilMock.when(SecurityUtil::getCurrentUserId).thenReturn(userId);

            when(meetingValidator.findMeetingOrThrow(meetingId)).thenReturn(meeting);
            when(userValidator.findUserOrThrow(userId)).thenReturn(user);
            doNothing().when(retrospectiveValidator).validateRetrospective(meetingId, userId);
            when(topicValidator.getTopic(topicId)).thenReturn(topic);
            when(topicValidator.getPreOpinion(topicId, userId)).thenReturn(null);
            when(personalRetrospectiveRepository.save(any(PersonalMeetingRetrospective.class))).thenReturn(saved);

            // when
            PersonalRetrospectiveResponse response = personalRetrospectiveService.createPersonalRetrospective(meetingId, request);

            // then
            assertThat(response.personalMeetingRetrospectiveId()).isEqualTo(1L);
            verify(personalRetrospectiveRepository).save(any(PersonalMeetingRetrospective.class));
        }
    }

    @Test
    @DisplayName("약속이 없으면 예외가 발생한다")
    void createPersonalRetrospective_throwsWhenMeetingNotFound() {
        // given
        Long meetingId = 999L;
        PersonalRetrospectiveRequest request = new PersonalRetrospectiveRequest(null, null, null);

        try (MockedStatic<SecurityUtil> securityUtilMock = mockStatic(SecurityUtil.class)) {
            securityUtilMock.when(SecurityUtil::getCurrentUserId).thenReturn(3L);

            when(meetingValidator.findMeetingOrThrow(meetingId))
                    .thenThrow(new IllegalArgumentException("약속을 찾을 수 없습니다."));

            // when & then
            assertThatThrownBy(() -> personalRetrospectiveService.createPersonalRetrospective(meetingId, request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("약속을 찾을 수 없습니다.");

            verify(personalRetrospectiveRepository, never()).save(any());
        }
    }

    @Test
    @DisplayName("사용자가 없으면 예외가 발생한다")
    void createPersonalRetrospective_throwsWhenUserNotFound() {
        // given
        Long meetingId = 1L;
        Long userId = 999L;
        Meeting meeting = Meeting.builder().id(meetingId).build();
        PersonalRetrospectiveRequest request = new PersonalRetrospectiveRequest(null, null, null);

        try (MockedStatic<SecurityUtil> securityUtilMock = mockStatic(SecurityUtil.class)) {
            securityUtilMock.when(SecurityUtil::getCurrentUserId).thenReturn(userId);

            when(meetingValidator.findMeetingOrThrow(meetingId)).thenReturn(meeting);
            when(userValidator.findUserOrThrow(userId))
                    .thenThrow(new IllegalArgumentException("사용자를 찾을 수 없습니다."));

            // when & then
            assertThatThrownBy(() -> personalRetrospectiveService.createPersonalRetrospective(meetingId, request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("사용자를 찾을 수 없습니다.");

            verify(personalRetrospectiveRepository, never()).save(any());
        }
    }

    @Test
    @DisplayName("이미 회고가 존재하면 예외가 발생한다")
    void createPersonalRetrospective_throwsWhenAlreadyExists() {
        // given
        Long meetingId = 1L;
        Long userId = 3L;
        Meeting meeting = Meeting.builder().id(meetingId).build();
        User user = User.builder().id(userId).build();
        PersonalRetrospectiveRequest request = new PersonalRetrospectiveRequest(null, null, null);

        try (MockedStatic<SecurityUtil> securityUtilMock = mockStatic(SecurityUtil.class)) {
            securityUtilMock.when(SecurityUtil::getCurrentUserId).thenReturn(userId);

            when(meetingValidator.findMeetingOrThrow(meetingId)).thenReturn(meeting);
            when(userValidator.findUserOrThrow(userId)).thenReturn(user);
            doThrow(new IllegalStateException("이미 해당 약속에 대한 회고가 존재합니다."))
                    .when(retrospectiveValidator).validateRetrospective(meetingId, userId);

            // when & then
            assertThatThrownBy(() -> personalRetrospectiveService.createPersonalRetrospective(meetingId, request))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("이미 해당 약속에 대한 회고가 존재합니다.");

            verify(personalRetrospectiveRepository, never()).save(any());
        }
    }

    @Test
    @DisplayName("인증 정보가 없으면 예외가 발생한다")
    void createPersonalRetrospective_throwsWhenUnauthenticated() {
        // given
        Long meetingId = 1L;
        PersonalRetrospectiveRequest request = new PersonalRetrospectiveRequest(null, null, null);

        try (MockedStatic<SecurityUtil> securityUtilMock = mockStatic(SecurityUtil.class)) {
            securityUtilMock.when(SecurityUtil::getCurrentUserId)
                    .thenThrow(new GlobalException(GlobalErrorCode.UNAUTHORIZED));

            // when & then
            assertThatThrownBy(() -> personalRetrospectiveService.createPersonalRetrospective(meetingId, request))
                    .isInstanceOf(GlobalException.class)
                    .hasFieldOrPropertyWithValue("errorCode", GlobalErrorCode.UNAUTHORIZED);

            verify(personalRetrospectiveRepository, never()).save(any());
        }
    }

    @Test
    @DisplayName("주제가 없으면 예외가 발생한다")
    void createPersonalRetrospective_throwsWhenTopicNotFound() {
        // given
        Long meetingId = 1L;
        Long userId = 3L;
        Long topicId = 999L;

        Meeting meeting = Meeting.builder().id(meetingId).build();
        User user = User.builder().id(userId).build();

        PersonalRetrospectiveRequest.ChangedThoughtRequest changedThoughtRequest =
                new PersonalRetrospectiveRequest.ChangedThoughtRequest(
                        topicId, "핵심 쟁점", "모임 후 의견"
                );

        PersonalRetrospectiveRequest request = new PersonalRetrospectiveRequest(
                List.of(changedThoughtRequest),
                null,
                null
        );

        try (MockedStatic<SecurityUtil> securityUtilMock = mockStatic(SecurityUtil.class)) {
            securityUtilMock.when(SecurityUtil::getCurrentUserId).thenReturn(userId);

            when(meetingValidator.findMeetingOrThrow(meetingId)).thenReturn(meeting);
            when(userValidator.findUserOrThrow(userId)).thenReturn(user);
            doNothing().when(retrospectiveValidator).validateRetrospective(meetingId, userId);
            when(topicValidator.getTopic(topicId))
                    .thenThrow(new IllegalArgumentException("주제를 찾을 수 없습니다."));

            // when & then
            assertThatThrownBy(() -> personalRetrospectiveService.createPersonalRetrospective(meetingId, request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("주제를 찾을 수 없습니다.");

            verify(personalRetrospectiveRepository, never()).save(any());
        }
    }

    @Test
    @DisplayName("여러 개의 changedThoughts, othersPerspectives, freeTexts를 포함하여 생성한다")
    void createPersonalRetrospective_withMultipleItems_success() {
        // given
        Long meetingId = 1L;
        Long userId = 3L;
        Long meetingMemberId1 = 5L;
        Long meetingMemberId2 = 6L;

        Meeting meeting = Meeting.builder().id(meetingId).build();
        User user = User.builder().id(userId).build();
        Topic topic1 = Topic.builder().id(10L).title("토픽1").build();
        Topic topic2 = Topic.builder().id(20L).title("토픽2").build();
        MeetingMember meetingMember1 = MeetingMember.builder().id(meetingMemberId1).build();
        MeetingMember meetingMember2 = MeetingMember.builder().id(meetingMemberId2).build();

        List<PersonalRetrospectiveRequest.ChangedThoughtRequest> changedThoughts = List.of(
                new PersonalRetrospectiveRequest.ChangedThoughtRequest(10L, "쟁점1", "의견1"),
                new PersonalRetrospectiveRequest.ChangedThoughtRequest(20L, "쟁점2", "의견2")
        );

        List<PersonalRetrospectiveRequest.OthersPerspectiveRequest> othersPerspectives = List.of(
                new PersonalRetrospectiveRequest.OthersPerspectiveRequest(10L, meetingMemberId1, "의견1", "이유1"),
                new PersonalRetrospectiveRequest.OthersPerspectiveRequest(20L, meetingMemberId2, "의견2", "이유2")
        );

        List<PersonalRetrospectiveRequest.FreeTextRequest> freeTexts = List.of(
                new PersonalRetrospectiveRequest.FreeTextRequest("제목1", "내용1"),
                new PersonalRetrospectiveRequest.FreeTextRequest("제목2", "내용2")
        );

        PersonalRetrospectiveRequest request = new PersonalRetrospectiveRequest(
                changedThoughts, othersPerspectives, freeTexts
        );

        PersonalMeetingRetrospective saved = PersonalMeetingRetrospective.builder()
                .id(1L)
                .meeting(meeting)
                .user(user)
                .build();

        try (MockedStatic<SecurityUtil> securityUtilMock = mockStatic(SecurityUtil.class)) {
            securityUtilMock.when(SecurityUtil::getCurrentUserId).thenReturn(userId);

            when(meetingValidator.findMeetingOrThrow(meetingId)).thenReturn(meeting);
            when(userValidator.findUserOrThrow(userId)).thenReturn(user);
            doNothing().when(retrospectiveValidator).validateRetrospective(meetingId, userId);
            when(topicValidator.getTopic(10L)).thenReturn(topic1);
            when(topicValidator.getTopic(20L)).thenReturn(topic2);
            when(topicValidator.getPreOpinion(anyLong(), eq(userId))).thenReturn(null);
            when(topicRepository.findById(10L)).thenReturn(Optional.of(topic1));
            when(topicRepository.findById(20L)).thenReturn(Optional.of(topic2));
            when(meetingValidator.getMeetingMember(meetingId, meetingMemberId1)).thenReturn(meetingMember1);
            when(meetingValidator.getMeetingMember(meetingId, meetingMemberId2)).thenReturn(meetingMember2);
            when(personalRetrospectiveRepository.save(any(PersonalMeetingRetrospective.class))).thenReturn(saved);

            // when
            PersonalRetrospectiveResponse response = personalRetrospectiveService.createPersonalRetrospective(meetingId, request);

            // then
            assertThat(response.personalMeetingRetrospectiveId()).isEqualTo(1L);

            verify(topicValidator, times(2)).getTopic(anyLong());
            verify(topicValidator, times(2)).getPreOpinion(anyLong(), eq(userId));
            verify(topicRepository, times(2)).findById(anyLong());
            verify(meetingValidator).getMeetingMember(meetingId, meetingMemberId1);
            verify(meetingValidator).getMeetingMember(meetingId, meetingMemberId2);
            verify(personalRetrospectiveRepository).save(any(PersonalMeetingRetrospective.class));
        }
    }

    // ==================== getPersonalRetrospectiveForm 테스트 ====================

    @Test
    @DisplayName("개인 회고 입력 폼을 정상적으로 조회한다")
    void getPersonalRetrospectiveForm_success() {
        // given
        Long meetingId = 1L;
        Long userId = 3L;

        User user1 = User.builder().id(userId).nickname("사용자1").build();
        User user2 = User.builder().id(4L).nickname("사용자2").build();

        Topic topic1 = Topic.builder().id(10L).title("주제1").build();
        Topic topic2 = Topic.builder().id(20L).title("주제2").build();
        List<Topic> topics = List.of(topic1, topic2);

        TopicAnswer answer1 = TopicAnswer.builder()
                .id(100L)
                .topic(topic1)
                .user(user1)
                .content("사전 의견1")
                .build();
        List<TopicAnswer> topicAnswers = List.of(answer1);

        MeetingMember member1 = MeetingMember.builder().id(1L).user(user1).build();
        MeetingMember member2 = MeetingMember.builder().id(2L).user(user2).build();
        List<MeetingMember> meetingMembers = List.of(member1, member2);

        PersonalRetrospectiveFormResponse expectedResponse = PersonalRetrospectiveFormResponse.of(
                meetingId,
                List.of(new PersonalRetrospectiveFormResponse.PreOpinions(10L, "주제1", "사전 의견1")),
                List.of(
                        new PersonalRetrospectiveFormResponse.Topics(10L, "주제1"),
                        new PersonalRetrospectiveFormResponse.Topics(20L, "주제2")
                ),
                List.of(
                        new PersonalRetrospectiveFormResponse.MeetingMembers(1L, "사용자1"),
                        new PersonalRetrospectiveFormResponse.MeetingMembers(2L, "사용자2")
                )
        );

        try (MockedStatic<SecurityUtil> securityUtilMock = mockStatic(SecurityUtil.class)) {
            securityUtilMock.when(SecurityUtil::getCurrentUserId).thenReturn(userId);

            doNothing().when(meetingValidator).validateMeeting(meetingId);
            doNothing().when(meetingValidator).validateMeetingMember(meetingId, userId);
            doNothing().when(retrospectiveValidator).validateRetrospective(meetingId, userId);
            when(topicValidator.getConfirmedTopics(meetingId)).thenReturn(topics);
            when(topicAnswerRepository.findByMeetingIdUserId(meetingId, userId)).thenReturn(topicAnswers);
            when(meetingMemberRepository.findByMeetingId(meetingId)).thenReturn(meetingMembers);
            when(assembler.assembleCreate(meetingId, topics, topicAnswers, meetingMembers))
                    .thenReturn(expectedResponse);

            // when
            PersonalRetrospectiveFormResponse response = personalRetrospectiveService.getPersonalRetrospectiveForm(meetingId);

            // then
            assertThat(response.meetingId()).isEqualTo(meetingId);
            assertThat(response.topics()).hasSize(2);
            assertThat(response.preOpinions()).hasSize(1);
            assertThat(response.meetingMembers()).hasSize(2);

            verify(meetingValidator).validateMeeting(meetingId);
            verify(meetingValidator).validateMeetingMember(meetingId, userId);
            verify(retrospectiveValidator).validateRetrospective(meetingId, userId);
            verify(topicValidator).getConfirmedTopics(meetingId);
            verify(topicAnswerRepository).findByMeetingIdUserId(meetingId, userId);
            verify(meetingMemberRepository).findByMeetingId(meetingId);
            verify(assembler).assembleCreate(meetingId, topics, topicAnswers, meetingMembers);
        }
    }

    @Test
    @DisplayName("사전 의견이 없어도 개인 회고 입력 폼을 조회한다")
    void getPersonalRetrospectiveForm_withoutPreOpinions_success() {
        // given
        Long meetingId = 1L;
        Long userId = 3L;

        User user1 = User.builder().id(userId).nickname("사용자1").build();
        Topic topic1 = Topic.builder().id(10L).title("주제1").build();
        List<Topic> topics = List.of(topic1);
        List<TopicAnswer> topicAnswers = List.of(); // 빈 리스트

        MeetingMember member1 = MeetingMember.builder().id(1L).user(user1).build();
        List<MeetingMember> meetingMembers = List.of(member1);

        PersonalRetrospectiveFormResponse expectedResponse = PersonalRetrospectiveFormResponse.of(
                meetingId,
                List.of(), // 빈 사전 의견
                List.of(new PersonalRetrospectiveFormResponse.Topics(10L, "주제1")),
                List.of(new PersonalRetrospectiveFormResponse.MeetingMembers(1L, "사용자1"))
        );

        try (MockedStatic<SecurityUtil> securityUtilMock = mockStatic(SecurityUtil.class)) {
            securityUtilMock.when(SecurityUtil::getCurrentUserId).thenReturn(userId);

            doNothing().when(meetingValidator).validateMeeting(meetingId);
            doNothing().when(meetingValidator).validateMeetingMember(meetingId, userId);
            doNothing().when(retrospectiveValidator).validateRetrospective(meetingId, userId);
            when(topicValidator.getConfirmedTopics(meetingId)).thenReturn(topics);
            when(topicAnswerRepository.findByMeetingIdUserId(meetingId, userId)).thenReturn(topicAnswers);
            when(meetingMemberRepository.findByMeetingId(meetingId)).thenReturn(meetingMembers);
            when(assembler.assembleCreate(meetingId, topics, topicAnswers, meetingMembers))
                    .thenReturn(expectedResponse);

            // when
            PersonalRetrospectiveFormResponse response = personalRetrospectiveService.getPersonalRetrospectiveForm(meetingId);

            // then
            assertThat(response.preOpinions()).isEmpty();
            assertThat(response.topics()).hasSize(1);
        }
    }

    @Test
    @DisplayName("약속이 존재하지 않으면 예외가 발생한다 - 폼 조회")
    void getPersonalRetrospectiveForm_throwsWhenMeetingNotFound() {
        // given
        Long meetingId = 999L;
        Long userId = 3L;

        try (MockedStatic<SecurityUtil> securityUtilMock = mockStatic(SecurityUtil.class)) {
            securityUtilMock.when(SecurityUtil::getCurrentUserId).thenReturn(userId);

            doThrow(new MeetingException(MeetingErrorCode.MEETING_NOT_FOUND))
                    .when(meetingValidator).validateMeeting(meetingId);

            // when & then
            assertThatThrownBy(() -> personalRetrospectiveService.getPersonalRetrospectiveForm(meetingId))
                    .isInstanceOf(MeetingException.class)
                    .hasFieldOrPropertyWithValue("errorCode", MeetingErrorCode.MEETING_NOT_FOUND);

            verify(topicValidator, never()).getConfirmedTopics(any());
        }
    }

    @Test
    @DisplayName("약속 멤버가 아니면 예외가 발생한다 - 폼 조회")
    void getPersonalRetrospectiveForm_throwsWhenNotMeetingMember() {
        // given
        Long meetingId = 1L;
        Long userId = 999L;

        try (MockedStatic<SecurityUtil> securityUtilMock = mockStatic(SecurityUtil.class)) {
            securityUtilMock.when(SecurityUtil::getCurrentUserId).thenReturn(userId);

            doNothing().when(meetingValidator).validateMeeting(meetingId);
            doThrow(new MeetingException(MeetingErrorCode.NOT_MEETING_MEMBER))
                    .when(meetingValidator).validateMeetingMember(meetingId, userId);

            // when & then
            assertThatThrownBy(() -> personalRetrospectiveService.getPersonalRetrospectiveForm(meetingId))
                    .isInstanceOf(MeetingException.class)
                    .hasFieldOrPropertyWithValue("errorCode", MeetingErrorCode.NOT_MEETING_MEMBER);

            verify(topicValidator, never()).getConfirmedTopics(any());
        }
    }

    @Test
    @DisplayName("이미 회고가 존재하면 예외가 발생한다 - 폼 조회")
    void getPersonalRetrospectiveForm_throwsWhenRetrospectiveAlreadyExists() {
        // given
        Long meetingId = 1L;
        Long userId = 3L;

        try (MockedStatic<SecurityUtil> securityUtilMock = mockStatic(SecurityUtil.class)) {
            securityUtilMock.when(SecurityUtil::getCurrentUserId).thenReturn(userId);

            doNothing().when(meetingValidator).validateMeeting(meetingId);
            doNothing().when(meetingValidator).validateMeetingMember(meetingId, userId);
            doThrow(new RetrospectiveException(RetrospectiveErrorCode.RETROSPECTIVE_ALREADY_EXISTS))
                    .when(retrospectiveValidator).validateRetrospective(meetingId, userId);

            // when & then
            assertThatThrownBy(() -> personalRetrospectiveService.getPersonalRetrospectiveForm(meetingId))
                    .isInstanceOf(RetrospectiveException.class)
                    .hasFieldOrPropertyWithValue("errorCode", RetrospectiveErrorCode.RETROSPECTIVE_ALREADY_EXISTS);

            verify(topicValidator, never()).getConfirmedTopics(any());
        }
    }

    @Test
    @DisplayName("확정된 주제가 없으면 예외가 발생한다 - 폼 조회")
    void getPersonalRetrospectiveForm_throwsWhenNoConfirmedTopics() {
        // given
        Long meetingId = 1L;
        Long userId = 3L;

        try (MockedStatic<SecurityUtil> securityUtilMock = mockStatic(SecurityUtil.class)) {
            securityUtilMock.when(SecurityUtil::getCurrentUserId).thenReturn(userId);

            doNothing().when(meetingValidator).validateMeeting(meetingId);
            doNothing().when(meetingValidator).validateMeetingMember(meetingId, userId);
            doNothing().when(retrospectiveValidator).validateRetrospective(meetingId, userId);
            when(topicValidator.getConfirmedTopics(meetingId))
                    .thenThrow(new TopicException(TopicErrorCode.TOPIC_NOT_FOUND));

            // when & then
            assertThatThrownBy(() -> personalRetrospectiveService.getPersonalRetrospectiveForm(meetingId))
                    .isInstanceOf(TopicException.class)
                    .hasFieldOrPropertyWithValue("errorCode", TopicErrorCode.TOPIC_NOT_FOUND);

            verify(topicAnswerRepository, never()).findByMeetingIdUserId(any(), any());
        }
    }

    @Test
    @DisplayName("인증 정보가 없으면 예외가 발생한다 - 폼 조회")
    void getPersonalRetrospectiveForm_throwsWhenUnauthenticated() {
        // given
        Long meetingId = 1L;

        try (MockedStatic<SecurityUtil> securityUtilMock = mockStatic(SecurityUtil.class)) {
            securityUtilMock.when(SecurityUtil::getCurrentUserId)
                    .thenThrow(new GlobalException(GlobalErrorCode.UNAUTHORIZED));

            // when & then
            assertThatThrownBy(() -> personalRetrospectiveService.getPersonalRetrospectiveForm(meetingId))
                    .isInstanceOf(GlobalException.class)
                    .hasFieldOrPropertyWithValue("errorCode", GlobalErrorCode.UNAUTHORIZED);

            verify(meetingValidator, never()).validateMeeting(any());
        }
    }

    // ==================== getPersonalRetrospectiveEditForm 테스트 ====================

    @Test
    @DisplayName("개인 회고 수정 폼을 정상적으로 조회한다")
    void getPersonalRetrospectiveEditForm_success() {
        // given
        Long meetingId = 1L;
        Long retrospectiveId = 100L;
        Long userId = 3L;

        List<RetrospectiveChangedThought> changedThoughts = List.of();
        List<RetrospectiveOthersPerspective> othersPerspectives = List.of();
        List<RetrospectiveFreeText> freeTexts = List.of();

        PersonalRetrospectiveDetailResponse expectedResponse = PersonalRetrospectiveDetailResponse.from(
                retrospectiveId,
                List.of(new PersonalRetrospectiveDetailResponse.ChangedThought(10L, "핵심 쟁점", "사후 의견")),
                List.of(new PersonalRetrospectiveDetailResponse.OthersPerspective(10L, 5L, "타인 의견", "인상 깊었던 이유")),
                List.of(new PersonalRetrospectiveDetailResponse.FreeText("자유 서술 제목", "자유 서술 내용"))
        );

        try (MockedStatic<SecurityUtil> securityUtilMock = mockStatic(SecurityUtil.class)) {
            securityUtilMock.when(SecurityUtil::getCurrentUserId).thenReturn(userId);

            doNothing().when(meetingValidator).validateMeeting(meetingId);
            doNothing().when(meetingValidator).validateMeetingMember(meetingId, userId);
            doNothing().when(retrospectiveValidator).validateRetrospective(retrospectiveId);
            when(changedThoughtRepository.findByPersonalMeetingRetrospective(retrospectiveId))
                    .thenReturn(changedThoughts);
            when(othersPerspectiveRepository.findByPersonalMeetingRetrospective(retrospectiveId))
                    .thenReturn(othersPerspectives);
            when(freeTextRepository.findByPersonalMeetingRetrospective_Id(retrospectiveId))
                    .thenReturn(freeTexts);
            when(assembler.assembleDetail(retrospectiveId, changedThoughts, othersPerspectives, freeTexts))
                    .thenReturn(expectedResponse);

            // when
            PersonalRetrospectiveDetailResponse response =
                    personalRetrospectiveService.getPersonalRetrospectiveEditForm(meetingId, retrospectiveId);

            // then
            assertThat(response.retrospectiveId()).isEqualTo(retrospectiveId);
            assertThat(response.changedThoughts()).hasSize(1);
            assertThat(response.othersPerspectives()).hasSize(1);
            assertThat(response.freeTexts()).hasSize(1);

            verify(meetingValidator).validateMeeting(meetingId);
            verify(meetingValidator).validateMeetingMember(meetingId, userId);
            verify(retrospectiveValidator).validateRetrospective(retrospectiveId);
            verify(changedThoughtRepository).findByPersonalMeetingRetrospective(retrospectiveId);
            verify(othersPerspectiveRepository).findByPersonalMeetingRetrospective(retrospectiveId);
            verify(freeTextRepository).findByPersonalMeetingRetrospective_Id(retrospectiveId);
            verify(assembler).assembleDetail(retrospectiveId, changedThoughts, othersPerspectives, freeTexts);
        }
    }

    @Test
    @DisplayName("회고 내용이 없어도 수정 폼을 조회한다")
    void getPersonalRetrospectiveEditForm_withEmptyContent_success() {
        // given
        Long meetingId = 1L;
        Long retrospectiveId = 100L;
        Long userId = 3L;

        List<RetrospectiveChangedThought> changedThoughts = List.of();
        List<RetrospectiveOthersPerspective> othersPerspectives = List.of();
        List<RetrospectiveFreeText> freeTexts = List.of();

        PersonalRetrospectiveDetailResponse expectedResponse = PersonalRetrospectiveDetailResponse.from(
                retrospectiveId,
                List.of(),
                List.of(),
                List.of()
        );

        try (MockedStatic<SecurityUtil> securityUtilMock = mockStatic(SecurityUtil.class)) {
            securityUtilMock.when(SecurityUtil::getCurrentUserId).thenReturn(userId);

            doNothing().when(meetingValidator).validateMeeting(meetingId);
            doNothing().when(meetingValidator).validateMeetingMember(meetingId, userId);
            doNothing().when(retrospectiveValidator).validateRetrospective(retrospectiveId);
            when(changedThoughtRepository.findByPersonalMeetingRetrospective(retrospectiveId))
                    .thenReturn(changedThoughts);
            when(othersPerspectiveRepository.findByPersonalMeetingRetrospective(retrospectiveId))
                    .thenReturn(othersPerspectives);
            when(freeTextRepository.findByPersonalMeetingRetrospective_Id(retrospectiveId))
                    .thenReturn(freeTexts);
            when(assembler.assembleDetail(retrospectiveId, changedThoughts, othersPerspectives, freeTexts))
                    .thenReturn(expectedResponse);

            // when
            PersonalRetrospectiveDetailResponse response =
                    personalRetrospectiveService.getPersonalRetrospectiveEditForm(meetingId, retrospectiveId);

            // then
            assertThat(response.changedThoughts()).isEmpty();
            assertThat(response.othersPerspectives()).isEmpty();
            assertThat(response.freeTexts()).isEmpty();
        }
    }

    @Test
    @DisplayName("약속이 존재하지 않으면 예외가 발생한다 - 수정 폼 조회")
    void getPersonalRetrospectiveEditForm_throwsWhenMeetingNotFound() {
        // given
        Long meetingId = 999L;
        Long retrospectiveId = 100L;
        Long userId = 3L;

        try (MockedStatic<SecurityUtil> securityUtilMock = mockStatic(SecurityUtil.class)) {
            securityUtilMock.when(SecurityUtil::getCurrentUserId).thenReturn(userId);

            doThrow(new MeetingException(MeetingErrorCode.MEETING_NOT_FOUND))
                    .when(meetingValidator).validateMeeting(meetingId);

            // when & then
            assertThatThrownBy(() ->
                    personalRetrospectiveService.getPersonalRetrospectiveEditForm(meetingId, retrospectiveId))
                    .isInstanceOf(MeetingException.class)
                    .hasFieldOrPropertyWithValue("errorCode", MeetingErrorCode.MEETING_NOT_FOUND);

            verify(changedThoughtRepository, never()).findByPersonalMeetingRetrospective(any());
        }
    }

    @Test
    @DisplayName("약속 멤버가 아니면 예외가 발생한다 - 수정 폼 조회")
    void getPersonalRetrospectiveEditForm_throwsWhenNotMeetingMember() {
        // given
        Long meetingId = 1L;
        Long retrospectiveId = 100L;
        Long userId = 999L;

        try (MockedStatic<SecurityUtil> securityUtilMock = mockStatic(SecurityUtil.class)) {
            securityUtilMock.when(SecurityUtil::getCurrentUserId).thenReturn(userId);

            doNothing().when(meetingValidator).validateMeeting(meetingId);
            doThrow(new MeetingException(MeetingErrorCode.NOT_MEETING_MEMBER))
                    .when(meetingValidator).validateMeetingMember(meetingId, userId);

            // when & then
            assertThatThrownBy(() ->
                    personalRetrospectiveService.getPersonalRetrospectiveEditForm(meetingId, retrospectiveId))
                    .isInstanceOf(MeetingException.class)
                    .hasFieldOrPropertyWithValue("errorCode", MeetingErrorCode.NOT_MEETING_MEMBER);

            verify(changedThoughtRepository, never()).findByPersonalMeetingRetrospective(any());
        }
    }

    @Test
    @DisplayName("개인 회고가 존재하지 않으면 예외가 발생한다 - 수정 폼 조회")
    void getPersonalRetrospectiveEditForm_throwsWhenRetrospectiveNotFound() {
        // given
        Long meetingId = 1L;
        Long retrospectiveId = 999L;
        Long userId = 3L;

        try (MockedStatic<SecurityUtil> securityUtilMock = mockStatic(SecurityUtil.class)) {
            securityUtilMock.when(SecurityUtil::getCurrentUserId).thenReturn(userId);

            doNothing().when(meetingValidator).validateMeeting(meetingId);
            doNothing().when(meetingValidator).validateMeetingMember(meetingId, userId);
            doThrow(new RetrospectiveException(RetrospectiveErrorCode.RETROSPECTIVE_NOT_FOUND))
                    .when(retrospectiveValidator).validateRetrospective(retrospectiveId);

            // when & then
            assertThatThrownBy(() ->
                    personalRetrospectiveService.getPersonalRetrospectiveEditForm(meetingId, retrospectiveId))
                    .isInstanceOf(RetrospectiveException.class)
                    .hasFieldOrPropertyWithValue("errorCode", RetrospectiveErrorCode.RETROSPECTIVE_NOT_FOUND);

            verify(changedThoughtRepository, never()).findByPersonalMeetingRetrospective(any());
        }
    }

    @Test
    @DisplayName("인증 정보가 없으면 예외가 발생한다 - 수정 폼 조회")
    void getPersonalRetrospectiveEditForm_throwsWhenUnauthenticated() {
        // given
        Long meetingId = 1L;
        Long retrospectiveId = 100L;

        try (MockedStatic<SecurityUtil> securityUtilMock = mockStatic(SecurityUtil.class)) {
            securityUtilMock.when(SecurityUtil::getCurrentUserId)
                    .thenThrow(new GlobalException(GlobalErrorCode.UNAUTHORIZED));

            // when & then
            assertThatThrownBy(() ->
                    personalRetrospectiveService.getPersonalRetrospectiveEditForm(meetingId, retrospectiveId))
                    .isInstanceOf(GlobalException.class)
                    .hasFieldOrPropertyWithValue("errorCode", GlobalErrorCode.UNAUTHORIZED);

            verify(meetingValidator, never()).validateMeeting(any());
        }
    }
}
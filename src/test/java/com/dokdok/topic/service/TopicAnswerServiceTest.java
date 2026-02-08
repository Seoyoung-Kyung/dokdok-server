package com.dokdok.topic.service;

import com.dokdok.gathering.service.GatheringValidator;
import com.dokdok.meeting.service.MeetingValidator;
import com.dokdok.topic.dto.request.TopicAnswerRequest;
import com.dokdok.topic.dto.response.TopicAnswerResponse;
import com.dokdok.topic.entity.Topic;
import com.dokdok.topic.entity.TopicAnswer;
import com.dokdok.topic.exception.TopicException;
import com.dokdok.topic.exception.TopicErrorCode;
import com.dokdok.topic.repository.TopicAnswerRepository;
import com.dokdok.topic.repository.TopicRepository;
import com.dokdok.user.entity.User;
import com.dokdok.global.exception.GlobalException;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class TopicAnswerServiceTest {

    @Mock
    private TopicAnswerRepository topicAnswerRepository;

    @Mock
    private TopicRepository topicRepository;

    @Mock
    private GatheringValidator gatheringValidator;

    @Mock
    private MeetingValidator meetingValidator;

    @Mock
    private TopicValidator topicValidator;

    @InjectMocks
    private TopicAnswerService topicAnswerService;

    @BeforeEach
    void setUpSecurityContext() {
        User user = User.builder()
                .id(1L)
                .nickname("tester")
                .kakaoId(1L)
                .build();
        com.dokdok.oauth2.CustomOAuth2User principal = com.dokdok.oauth2.CustomOAuth2User.builder()
                .user(user)
                .attributes(java.util.Collections.emptyMap())
                .build();
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                principal,
                null,
                principal.getAuthorities()
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("토픽 답변 생성 시 저장 요청과 응답 DTO를 확인한다")
    void createAnswer_savesAnswerAndReturnsResponse() {
        Topic topic = Topic.builder().id(12L).build();
        TopicAnswer saved = TopicAnswer.builder()
                .id(100L)
                .topic(topic)
                .content("이 책을 읽고 ...")
                .isSubmitted(false)
                .build();

        given(topicRepository.getReferenceById(12L)).willReturn(topic);
        doNothing().when(topicValidator).validateTopicInMeeting(12L, 1L);
        given(topicAnswerRepository.save(any(TopicAnswer.class)))
                .willReturn(saved);

        TopicAnswerRequest request = new TopicAnswerRequest("이 책을 읽고 ...");

        TopicAnswerResponse response = topicAnswerService.createAnswer(
                1L, 1L, 12L, request
        );

        ArgumentCaptor<TopicAnswer> captor = ArgumentCaptor.forClass(TopicAnswer.class);
        verify(topicAnswerRepository).save(captor.capture());

        TopicAnswer captured = captor.getValue();
        assertThat(captured.getTopic()).isEqualTo(topic);
        assertThat(captured.getUser().getId()).isEqualTo(1L);
        assertThat(captured.getContent()).isEqualTo("이 책을 읽고 ...");

        assertThat(response.topicId()).isEqualTo(12L);
        assertThat(response.isSubmitted()).isFalse();
    }

    @Test
    @DisplayName("토픽이 없으면 예외가 발생한다")
    void createAnswer_throwsWhenTopicMissing() {
        doThrow(new TopicException(TopicErrorCode.TOPIC_NOT_FOUND))
                .when(topicValidator).validateTopicInMeeting(12L, 1L);

        TopicAnswerRequest request = new TopicAnswerRequest("이 책을 읽고 ...");

        assertThatThrownBy(() -> topicAnswerService.createAnswer(
                1L, 1L, 12L, request
        )).isInstanceOf(TopicException.class);

        verifyNoInteractions(topicAnswerRepository);
    }

    @Test
    @DisplayName("인증 정보가 없으면 예외가 발생한다")
    void createAnswer_throwsWhenUnauthenticated() {
        SecurityContextHolder.clearContext();
        TopicAnswerRequest request = new TopicAnswerRequest("이 책을 읽고 ...");

        assertThatThrownBy(() -> topicAnswerService.createAnswer(
                1L, 1L, 12L, request
        )).isInstanceOf(GlobalException.class);
    }

    @Test
    @DisplayName("내 토픽 답변 수정 시 내용이 갱신되고 응답 DTO를 반환한다")
    void updateMyAnswer_updatesContentAndReturnsResponse() {
        Topic topic = Topic.builder().id(12L).build();
        User user = User.builder().id(1L).build();
        TopicAnswer answer = TopicAnswer.builder()
                .id(100L)
                .topic(topic)
                .user(user)
                .content("기존 내용")
                .isSubmitted(false)
                .build();

        given(topicValidator.getTopicAnswer(12L, 1L)).willReturn(answer);
        doNothing().when(topicValidator).validateTopicInMeeting(12L, 1L);

        TopicAnswerRequest request = new TopicAnswerRequest("수정된 내용");

        TopicAnswerResponse response = topicAnswerService.updateMyAnswer(
                1L, 1L, 12L, request
        );

        assertThat(answer.getContent()).isEqualTo("수정된 내용");
        assertThat(response.topicId()).isEqualTo(12L);
        assertThat(response.isSubmitted()).isFalse();
    }

    @Test
    @DisplayName("이미 제출된 답변은 수정할 수 없다")
    void updateMyAnswer_throwsWhenAlreadySubmitted() {
        Topic topic = Topic.builder().id(12L).build();
        User user = User.builder().id(1L).build();
        TopicAnswer answer = TopicAnswer.builder()
                .id(100L)
                .topic(topic)
                .user(user)
                .content("기존 내용")
                .isSubmitted(true)
                .build();

        given(topicValidator.getTopicAnswer(12L, 1L)).willReturn(answer);
        doNothing().when(topicValidator).validateTopicInMeeting(12L, 1L);

        TopicAnswerRequest request = new TopicAnswerRequest("수정된 내용");

        assertThatThrownBy(() -> topicAnswerService.updateMyAnswer(
                1L, 1L, 12L, request
        )).isInstanceOf(TopicException.class);
    }

    @Test
    @DisplayName("내 토픽 답변이 없으면 수정 시 예외가 발생한다")
    void updateMyAnswer_throwsWhenAnswerMissing() {
        given(topicValidator.getTopicAnswer(12L, 1L))
                .willThrow(new TopicException(TopicErrorCode.TOPIC_ANSWER_NOT_FOUND));
        doNothing().when(topicValidator).validateTopicInMeeting(12L, 1L);

        TopicAnswerRequest request = new TopicAnswerRequest("수정된 내용");

        assertThatThrownBy(() -> topicAnswerService.updateMyAnswer(
                1L, 1L, 12L, request
        )).isInstanceOf(TopicException.class);
    }

    @Test
    @DisplayName("내 토픽 답변 제출 시 제출 상태로 변경된다")
    void submitMyAnswer_updatesSubmittedState() {
        Topic topic = Topic.builder().id(12L).build();
        User user = User.builder().id(1L).build();
        TopicAnswer answer = TopicAnswer.builder()
                .id(100L)
                .topic(topic)
                .user(user)
                .content("기존 내용")
                .isSubmitted(false)
                .build();

        given(topicValidator.getTopicAnswer(12L, 1L)).willReturn(answer);
        doNothing().when(topicValidator).validateTopicInMeeting(12L, 1L);

        com.dokdok.topic.dto.response.TopicAnswerSubmitResponse response =
                topicAnswerService.submitMyAnswer(1L, 1L, 12L);

        assertThat(answer.getIsSubmitted()).isTrue();
        assertThat(response.topicId()).isEqualTo(12L);
        assertThat(response.isSubmitted()).isTrue();
    }

    @Test
    @DisplayName("이미 제출된 답변은 다시 제출할 수 없다")
    void submitMyAnswer_throwsWhenAlreadySubmitted() {
        Topic topic = Topic.builder().id(12L).build();
        User user = User.builder().id(1L).build();
        TopicAnswer answer = TopicAnswer.builder()
                .id(100L)
                .topic(topic)
                .user(user)
                .content("기존 내용")
                .isSubmitted(true)
                .build();

        given(topicValidator.getTopicAnswer(12L, 1L)).willReturn(answer);
        doNothing().when(topicValidator).validateTopicInMeeting(12L, 1L);

        assertThatThrownBy(() -> topicAnswerService.submitMyAnswer(
                1L, 1L, 12L
        )).isInstanceOf(TopicException.class);
    }

}
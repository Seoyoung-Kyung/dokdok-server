package com.dokdok.topic;

import com.dokdok.topic.dto.request.TopicAnswerRequest;
import com.dokdok.topic.dto.response.TopicAnswerResponse;
import com.dokdok.topic.entity.Topic;
import com.dokdok.topic.entity.TopicAnswer;
import com.dokdok.topic.exception.TopicException;
import com.dokdok.topic.repository.TopicAnswerRepository;
import com.dokdok.topic.repository.TopicRepository;
import com.dokdok.topic.service.TopicAnswerService;
import com.dokdok.user.entity.User;
import com.dokdok.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class TopicAnswerServiceTest {

    @Mock
    private TopicAnswerRepository topicAnswerRepository;

    @Mock
    private TopicRepository topicRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TopicAnswerService topicAnswerService;

    @Test
    @DisplayName("토픽 답변 생성 시 저장 요청과 응답 DTO를 확인한다")
    void createAnswer_savesAnswerAndReturnsResponse() {
        Topic topic = Topic.builder().id(12L).build();
        User user = User.builder().id(1L).build();
        TopicAnswer saved = TopicAnswer.builder()
                .id(100L)
                .topic(topic)
                .user(user)
                .content("이 책을 읽고 ...")
                .isSubmitted(false)
                .build();

        given(topicRepository.findById(12L)).willReturn(Optional.of(topic));
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(topicAnswerRepository.save(any(TopicAnswer.class)))
                .willReturn(saved);

        TopicAnswerRequest request = new TopicAnswerRequest("이 책을 읽고 ...");

        TopicAnswerResponse response = topicAnswerService.createAnswer(
                1L, 1L, 12L, 1L, request
        );

        ArgumentCaptor<TopicAnswer> captor = ArgumentCaptor.forClass(TopicAnswer.class);
        verify(topicAnswerRepository).save(captor.capture());

        TopicAnswer captured = captor.getValue();
        assertThat(captured.getTopic()).isEqualTo(topic);
        assertThat(captured.getUser()).isEqualTo(user);
        assertThat(captured.getContent()).isEqualTo("이 책을 읽고 ...");

        assertThat(response.topicId()).isEqualTo(12L);
        assertThat(response.isSubmitted()).isFalse();
    }

    @Test
    @DisplayName("토픽이 없으면 예외가 발생한다")
    void createAnswer_throwsWhenTopicMissing() {
        given(topicRepository.findById(12L)).willReturn(Optional.empty());

        TopicAnswerRequest request = new TopicAnswerRequest("이 책을 읽고 ...");

        assertThatThrownBy(() -> topicAnswerService.createAnswer(
                1L, 1L, 12L, 1L, request
        )).isInstanceOf(TopicException.class);

        verifyNoInteractions(topicAnswerRepository);
    }

    @Test
    @DisplayName("사용자가 없으면 예외가 발생한다")
    void createAnswer_throwsWhenUserMissing() {
        Topic topic = Topic.builder().id(12L).build();
        given(topicRepository.findById(12L)).willReturn(Optional.of(topic));
        given(userRepository.findById(1L)).willReturn(Optional.empty());

        TopicAnswerRequest request = new TopicAnswerRequest("이 책을 읽고 ...");

        assertThatThrownBy(() -> topicAnswerService.createAnswer(
                1L, 1L, 12L, 1L, request
        )).isInstanceOf(TopicException.class);

        verifyNoInteractions(topicAnswerRepository);
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

        given(topicAnswerRepository.findByTopicIdAndUserId(12L, 1L))
                .willReturn(Optional.of(answer));

        TopicAnswerRequest request = new TopicAnswerRequest("수정된 내용");

        TopicAnswerResponse response = topicAnswerService.updateMyAnswer(
                1L, 1L, 12L, 1L, request
        );

        assertThat(answer.getContent()).isEqualTo("수정된 내용");
        assertThat(response.topicId()).isEqualTo(12L);
        assertThat(response.isSubmitted()).isFalse();
    }

    @Test
    @DisplayName("내 토픽 답변이 없으면 수정 시 예외가 발생한다")
    void updateMyAnswer_throwsWhenAnswerMissing() {
        given(topicAnswerRepository.findByTopicIdAndUserId(12L, 1L))
                .willReturn(Optional.empty());

        TopicAnswerRequest request = new TopicAnswerRequest("수정된 내용");

        assertThatThrownBy(() -> topicAnswerService.updateMyAnswer(
                1L, 1L, 12L, 1L, request
        )).isInstanceOf(TopicException.class);
    }
}

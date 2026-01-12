package com.dokdok.topic;

import com.dokdok.topic.dto.request.TopicAnswerRequest;
import com.dokdok.topic.dto.response.TopicAnswerResponse;
import com.dokdok.topic.entity.Topic;
import com.dokdok.topic.entity.TopicAnswer;
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

import java.math.BigDecimal;
import java.util.NoSuchElementException;
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
                .bookRating(new BigDecimal("4.5"))
                .content("이 책을 읽고 ...")
                .isSubmitted(false)
                .build();

        given(topicRepository.findById(12L)).willReturn(Optional.of(topic));
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(topicAnswerRepository.save(any(TopicAnswer.class)))
                .willReturn(saved);

        TopicAnswerRequest request = new TopicAnswerRequest(
                "이 책을 읽고 ...",
                new BigDecimal("4.5")
        );

        TopicAnswerResponse response = topicAnswerService.createAnswer(
                1L, 1L, 12L, 1L, request
        );

        ArgumentCaptor<TopicAnswer> captor = ArgumentCaptor.forClass(TopicAnswer.class);
        verify(topicAnswerRepository).save(captor.capture());

        TopicAnswer captured = captor.getValue();
        assertThat(captured.getTopic()).isEqualTo(topic);
        assertThat(captured.getUser()).isEqualTo(user);
        assertThat(captured.getBookRating()).isEqualByComparingTo("4.5");
        assertThat(captured.getContent()).isEqualTo("이 책을 읽고 ...");

        assertThat(response.topicId()).isEqualTo(12L);
        assertThat(response.isSubmitted()).isFalse();
    }

    @Test
    @DisplayName("토픽이 없으면 예외가 발생한다")
    void createAnswer_throwsWhenTopicMissing() {
        given(topicRepository.findById(12L)).willReturn(Optional.empty());

        TopicAnswerRequest request = new TopicAnswerRequest(
                "이 책을 읽고 ...",
                new BigDecimal("4.5")
        );

        assertThatThrownBy(() -> topicAnswerService.createAnswer(
                1L, 1L, 12L, 1L, request
        )).isInstanceOf(NoSuchElementException.class);

        verifyNoInteractions(topicAnswerRepository);
    }

    @Test
    @DisplayName("사용자가 없으면 예외가 발생한다")
    void createAnswer_throwsWhenUserMissing() {
        Topic topic = Topic.builder().id(12L).build();
        given(topicRepository.findById(12L)).willReturn(Optional.of(topic));
        given(userRepository.findById(1L)).willReturn(Optional.empty());

        TopicAnswerRequest request = new TopicAnswerRequest(
                "이 책을 읽고 ...",
                new BigDecimal("4.5")
        );

        assertThatThrownBy(() -> topicAnswerService.createAnswer(
                1L, 1L, 12L, 1L, request
        )).isInstanceOf(NoSuchElementException.class);

        verifyNoInteractions(topicAnswerRepository);
    }
}

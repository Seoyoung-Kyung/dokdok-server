package com.dokdok.topic.service;

import com.dokdok.topic.dto.request.TopicAnswerRequest;
import com.dokdok.topic.dto.response.TopicAnswerResponse;
import com.dokdok.topic.entity.Topic;
import com.dokdok.topic.entity.TopicAnswer;
import com.dokdok.topic.repository.TopicAnswerRepository;
import com.dokdok.topic.repository.TopicRepository;
import com.dokdok.user.entity.User;
import com.dokdok.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TopicAnswerService {

    private final TopicAnswerRepository topicAnswerRepository;
    private final TopicRepository topicRepository;
    private final UserRepository userRepository;

    @Transactional
    public TopicAnswerResponse createAnswer(
            Long gatheringId,
            Long meetingId,
            Long topicId,
            Long userId,
            TopicAnswerRequest request
    ) {
        // TODO: gatheringId/meetingId/topicId 관계 검증
        // TODO: bookRating 0.5 단위 검증

        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(); // TODO: GlobalException으로 교체

        User user = userRepository.findById(userId)
                .orElseThrow(); // TODO: GlobalException으로 교체

        TopicAnswer saved = topicAnswerRepository.save(
                TopicAnswer.create(topic, user, request.bookRating(), request.content())
        );

        return new TopicAnswerResponse(
                topic.getId(),
                saved.getIsSubmitted(),
                saved.getUpdatedAt()
        );
    }
}

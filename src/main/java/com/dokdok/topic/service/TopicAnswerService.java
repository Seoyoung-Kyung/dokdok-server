package com.dokdok.topic.service;

import com.dokdok.topic.dto.request.TopicAnswerRequest;
import com.dokdok.topic.dto.response.TopicAnswerDetailResponse;
import com.dokdok.topic.dto.response.TopicAnswerResponse;
import com.dokdok.topic.entity.Topic;
import com.dokdok.topic.entity.TopicAnswer;
import com.dokdok.topic.exception.TopicErrorCode;
import com.dokdok.topic.exception.TopicException;
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

        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new TopicException(TopicErrorCode.TOPIC_NOT_FOUND));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new TopicException(TopicErrorCode.USER_NOT_FOUND));

        TopicAnswer saved = topicAnswerRepository.save(
                TopicAnswer.create(topic, user, request.content())
        );

        return TopicAnswerResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public TopicAnswerDetailResponse getMyAnswer(
            Long gatheringId,
            Long meetingId,
            Long topicId,
            Long userId
    ) {
        // TODO: gatheringId/meetingId/topicId 관계 검증

        TopicAnswer answer = topicAnswerRepository.findByTopicIdAndUserId(topicId, userId)
                .orElseThrow(() -> new TopicException(TopicErrorCode.TOPIC_ANSWER_NOT_FOUND));

        return TopicAnswerDetailResponse.from(answer);
    }
}

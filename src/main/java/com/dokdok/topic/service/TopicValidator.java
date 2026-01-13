package com.dokdok.topic.service;

import com.dokdok.topic.entity.TopicAnswer;
import com.dokdok.topic.entity.Topic;
import com.dokdok.topic.exception.TopicErrorCode;
import com.dokdok.topic.exception.TopicException;
import com.dokdok.topic.repository.TopicAnswerRepository;
import com.dokdok.topic.repository.TopicRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TopicValidator {

    private final TopicRepository topicRepository;
    private final TopicAnswerRepository topicAnswerRepository;

    public void validateTopicInMeeting(Long topicId, Long meetingId) {
        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new TopicException(TopicErrorCode.TOPIC_NOT_FOUND));
        if (!meetingId.equals(topic.getMeeting().getId())) {
            throw new TopicException(TopicErrorCode.TOPIC_NOT_FOUND);
        }
    }

    public TopicAnswer getTopicAnswer(Long topicId, Long userId) {
        return topicAnswerRepository.findByTopicIdAndUserId(topicId, userId)
                .orElseThrow(() -> new TopicException(TopicErrorCode.TOPIC_ANSWER_NOT_FOUND));
    }
}

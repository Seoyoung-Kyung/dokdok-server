package com.dokdok.topic.service;

import com.dokdok.gathering.service.GatheringValidator;
import com.dokdok.global.util.SecurityUtil;
import com.dokdok.meeting.service.MeetingValidator;
import com.dokdok.topic.dto.request.TopicAnswerRequest;
import com.dokdok.topic.dto.response.TopicAnswerDetailResponse;
import com.dokdok.topic.dto.response.TopicAnswerResponse;
import com.dokdok.topic.dto.response.TopicAnswerSubmitResponse;
import com.dokdok.topic.entity.Topic;
import com.dokdok.topic.entity.TopicAnswer;
import com.dokdok.topic.exception.TopicErrorCode;
import com.dokdok.topic.exception.TopicException;
import com.dokdok.topic.repository.TopicAnswerRepository;
import com.dokdok.topic.repository.TopicRepository;
import com.dokdok.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TopicAnswerService {

    private final TopicAnswerRepository topicAnswerRepository;
    private final TopicRepository topicRepository;
    private final GatheringValidator gatheringValidator;
    private final MeetingValidator meetingValidator;
    private final TopicValidator topicValidator;

    @Transactional
    public TopicAnswerResponse createAnswer(
            Long gatheringId,
            Long meetingId,
            Long topicId,
            TopicAnswerRequest request
    ) {
        Long userId = SecurityUtil.getCurrentUserId();

        gatheringValidator.validateMembership(gatheringId, userId);
        meetingValidator.validateMeetingInGathering(meetingId, gatheringId);
        topicValidator.validateTopicInMeeting(topicId, meetingId);

        boolean exists = topicAnswerRepository.existsByTopicIdAndUserId(topicId, userId);
        if (exists) {
            throw new TopicException(TopicErrorCode.TOPIC_ANSWER_ALREADY_EXISTS);
        }

        Topic topic = topicRepository.getReferenceById(topicId);
        User user = SecurityUtil.getCurrentUserEntity();

        TopicAnswer saved = topicAnswerRepository.save(
                TopicAnswer.create(topic, user, request.content())
        );

        return TopicAnswerResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public TopicAnswerDetailResponse getMyAnswer(
            Long gatheringId,
            Long meetingId,
            Long topicId
    ) {
        Long userId = SecurityUtil.getCurrentUserId();

        gatheringValidator.validateMembership(gatheringId, userId);
        meetingValidator.validateMeetingInGathering(meetingId, gatheringId);
        topicValidator.validateTopicInMeeting(topicId, meetingId);

        TopicAnswer answer = topicValidator.getTopicAnswer(topicId, userId);

        return TopicAnswerDetailResponse.from(answer);
    }

    @Transactional
    public TopicAnswerResponse updateMyAnswer(
            Long gatheringId,
            Long meetingId,
            Long topicId,
            TopicAnswerRequest request
    ) {
        Long userId = SecurityUtil.getCurrentUserId();

        gatheringValidator.validateMembership(gatheringId, userId);
        meetingValidator.validateMeetingInGathering(meetingId, gatheringId);
        topicValidator.validateTopicInMeeting(topicId, meetingId);

        TopicAnswer answer = topicValidator.getTopicAnswer(topicId, userId);

        if (Boolean.TRUE.equals(answer.getIsSubmitted())) {
            throw new TopicException(TopicErrorCode.TOPIC_ANSWER_ALREADY_SUBMITTED);
        }

        answer.updateContent(request.content());

        return TopicAnswerResponse.from(answer);
    }

    @Transactional
    public TopicAnswerSubmitResponse submitMyAnswer(
            Long gatheringId,
            Long meetingId,
            Long topicId
    ) {
        Long userId = SecurityUtil.getCurrentUserId();

        gatheringValidator.validateMembership(gatheringId, userId);
        meetingValidator.validateMeetingInGathering(meetingId, gatheringId);
        topicValidator.validateTopicInMeeting(topicId, meetingId);

        TopicAnswer answer = topicValidator.getTopicAnswer(topicId, userId);

        if (Boolean.TRUE.equals(answer.getIsSubmitted())) {
            throw new TopicException(TopicErrorCode.TOPIC_ANSWER_ALREADY_SUBMITTED);
        }

        answer.submit();

        return TopicAnswerSubmitResponse.from(answer);
    }

}

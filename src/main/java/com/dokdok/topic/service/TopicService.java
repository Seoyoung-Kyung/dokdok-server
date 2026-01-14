package com.dokdok.topic.service;

import com.dokdok.gathering.service.GatheringValidator;
import com.dokdok.global.util.SecurityUtil;
import com.dokdok.meeting.entity.Meeting;
import com.dokdok.meeting.entity.MeetingMember;
import com.dokdok.meeting.service.MeetingValidator;
import com.dokdok.topic.dto.request.ConfirmTopicsRequest;
import com.dokdok.topic.dto.request.SuggestTopicRequest;
import com.dokdok.topic.dto.response.ConfirmTopicsResponse;
import com.dokdok.topic.dto.response.SuggestTopicResponse;
import com.dokdok.topic.dto.response.TopicsPageResponse;
import com.dokdok.topic.entity.Topic;
import com.dokdok.topic.entity.TopicStatus;
import com.dokdok.topic.exception.TopicErrorCode;
import com.dokdok.topic.exception.TopicException;
import com.dokdok.topic.repository.TopicRepository;
import com.dokdok.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Optional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@RequestMapping("/api/gatherings/{gatheringId}/meetings/{meetingId}")
public class TopicService {

    private final TopicRepository topicRepository;
    private final MeetingValidator meetingValidator;
    private final GatheringValidator gatheringValidator;
    private final TopicValidator topicValidator;

    @Transactional
    public SuggestTopicResponse createTopic(
            Long gatheringId,
            Long meetingId,
            SuggestTopicRequest request
    ) {
        Long userId = SecurityUtil.getCurrentUserId();

        gatheringValidator.validateMembership(gatheringId, userId);

        meetingValidator.validateMemberInGathering(meetingId, gatheringId);

        meetingValidator.validateMeetingStatus(meetingId);

        MeetingMember meetingMember = meetingValidator.getMeetingMember(meetingId, userId);

        Meeting meeting = meetingMember.getMeeting();
        User user = meetingMember.getUser();

        Topic topic = Topic.of(
                meeting,
                user,
                request.title(),
                request.description(),
                request.topicType()
        );

        topicRepository.save(topic);

        return SuggestTopicResponse.from(topic, user);
    }

    @Transactional(readOnly = true)
    public TopicsPageResponse getTopics(
            Long gatheringId,
            Long meetingId,
            Pageable pageable
    ) {
        Long userId = SecurityUtil.getCurrentUserId();

        gatheringValidator.validateMembership(gatheringId, userId);

        meetingValidator.validateMemberInGathering(meetingId, gatheringId);

        Pageable noSortPageable =
                PageRequest.of(pageable.getPageNumber(), pageable.getPageSize());

        Page<Topic> topicPage =
                topicRepository.findTopicsByMeetingId(meetingId, noSortPageable);

        return TopicsPageResponse.from(topicPage);
    }

    @Transactional
    public ConfirmTopicsResponse confirmTopics(
            Long gatheringId,
            Long meetingId,
            ConfirmTopicsRequest request
    ) {
        Long userId = SecurityUtil.getCurrentUserId();

        gatheringValidator.validateMembership(gatheringId, userId);
        meetingValidator.validateMemberInGathering(meetingId, gatheringId);

        List<Long> topicIds = request.topicIds();

        List<Topic> topics = topicRepository.findAllByIdInAndMeetingId(topicIds, meetingId);
        if (topics.size() != topicIds.size()) {
            throw new TopicException(TopicErrorCode.TOPIC_NOT_FOUND);
        }

        Map<Long, Topic> topicMap =
                topics.stream()
                        .collect(Collectors.toMap(Topic::getId, Function.identity()));

        for (int i = 0; i < topicIds.size(); i++) {
            Long topicId = topicIds.get(i);
            Topic topic = topicMap.get(topicId);
            if (topic == null) {
                throw new TopicException(TopicErrorCode.TOPIC_NOT_FOUND);
            }
            topic.updateStatus(TopicStatus.CONFIRMED);
            topic.updateConfirmOrder(i + 1);
        }

        return ConfirmTopicsResponse.from(meetingId);
    }

    @Transactional
    public void deleteTopic(
            Long gatheringId,
            Long meetingId,
            Long topicId
    ) {
        Long userId = SecurityUtil.getCurrentUserId();

        gatheringValidator.validateMembership(gatheringId, userId);

        meetingValidator.validateMemberInGathering(meetingId, gatheringId);

        topicValidator.validateTopicInMeeting(topicId, meetingId);

        Topic topic = topicValidator.getDeletableTopic(topicId, userId);

        topic.softDelete();
    }
}
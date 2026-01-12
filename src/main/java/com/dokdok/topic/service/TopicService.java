package com.dokdok.topic.service;

import com.dokdok.gathering.service.GatheringValidator;
import com.dokdok.global.exception.GlobalErrorCode;
import com.dokdok.global.exception.GlobalException;
import com.dokdok.global.util.SecurityUtil;
import com.dokdok.meeting.entity.Meeting;
import com.dokdok.meeting.entity.MeetingMember;
import com.dokdok.meeting.service.MeetingValidator;
import com.dokdok.topic.dto.request.SuggestTopicRequest;
import com.dokdok.topic.dto.response.SuggestTopicResponse;
import com.dokdok.topic.entity.Topic;
import com.dokdok.topic.repository.TopicRepository;
import com.dokdok.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TopicService {

    private final TopicRepository topicRepository;
    private final MeetingValidator meetingValidator;
    private final GatheringValidator gatheringValidator;

    @Transactional
    public SuggestTopicResponse createTopic(
            Long gatheringId,
            Long meetingId,
            SuggestTopicRequest request
    ) {
        Long userId = SecurityUtil.getCurrentUserIdOptional()
                .orElseThrow(() -> new GlobalException(GlobalErrorCode.UNAUTHORIZED));

        gatheringValidator.validateMembership(gatheringId, userId);

        meetingValidator.validateMemberInGathering(meetingId, gatheringId);

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

}
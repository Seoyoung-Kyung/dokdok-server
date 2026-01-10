package com.dokdok.topic.service;

import com.dokdok.gathering.repository.GatheringMemberRepository;
import com.dokdok.gathering.service.GatheringMemberValidator;
import com.dokdok.global.exception.GlobalErrorCode;
import com.dokdok.global.exception.GlobalException;
import com.dokdok.meeting.entity.Meeting;
import com.dokdok.meeting.repository.MeetingRepository;
import com.dokdok.topic.dto.SuggestTopicRequest;
import com.dokdok.topic.dto.SuggestTopicResponse;
import com.dokdok.topic.entity.Topic;
import com.dokdok.topic.repository.TopicRepository;
import com.dokdok.user.entity.User;
import com.dokdok.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TopicService {

    private final TopicRepository topicRepository;
    private final MeetingRepository meetingRepository;
    private final UserRepository userRepository;
    private final GatheringMemberValidator gatheringMemberValidator;

    @Transactional
    public SuggestTopicResponse createTopic(
            Long gatheringId,
            Long meetingId,
            Long userId,
            SuggestTopicRequest request
    ) {
        gatheringMemberValidator.validateMembership(gatheringId, userId);

        Meeting meeting = getMeeting(meetingId, gatheringId);

        User user = getUser(userId);

        Topic topic = Topic.create(
                meeting,
                user,
                request.title(),
                request.description(),
                request.topicType()
        );

        topicRepository.save(topic);

        return SuggestTopicResponse.from(topic, user);
    }

    private Meeting getMeeting(Long meetingId, Long gatheringId) {
        return meetingRepository.findByIdAndGatheringId(meetingId, gatheringId)
                .orElseThrow(() -> new GlobalException(GlobalErrorCode.NOT_FOUND));
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new GlobalException(GlobalErrorCode.NOT_FOUND));
    }
}
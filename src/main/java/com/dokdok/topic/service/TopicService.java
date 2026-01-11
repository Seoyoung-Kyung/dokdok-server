package com.dokdok.topic.service;

import com.dokdok.gathering.service.GatheringMemberService;
import com.dokdok.meeting.entity.Meeting;
import com.dokdok.meeting.entity.MeetingMember;
import com.dokdok.meeting.service.MeetingMemberService;
import com.dokdok.meeting.service.MeetingService;
import com.dokdok.topic.dto.SuggestTopicRequest;
import com.dokdok.topic.dto.SuggestTopicResponse;
import com.dokdok.topic.entity.Topic;
import com.dokdok.topic.repository.TopicRepository;
import com.dokdok.user.entity.User;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TopicService {

    private final TopicRepository topicRepository;
    private final MeetingService meetingService;
    private final MeetingMemberService meetingMemberService;
    private final GatheringMemberService gatheringMemberService;

    @Transactional
    public SuggestTopicResponse createTopic(
            Long gatheringId,
            Long meetingId,
            Long userId,
            SuggestTopicRequest request
    ) {
        gatheringMemberService.validateMembership(gatheringId, userId);

        meetingService.validateMemberInGathering(meetingId, gatheringId);

        MeetingMember meetingMember = meetingMemberService.getMeetingMember(meetingId, userId);

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
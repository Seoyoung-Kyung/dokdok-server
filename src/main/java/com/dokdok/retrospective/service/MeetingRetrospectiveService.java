package com.dokdok.retrospective.service;

import com.dokdok.global.util.SecurityUtil;
import com.dokdok.meeting.entity.Meeting;
import com.dokdok.meeting.exception.MeetingErrorCode;
import com.dokdok.meeting.exception.MeetingException;
import com.dokdok.meeting.repository.MeetingRepository;
import com.dokdok.retrospective.dto.response.MeetingRetrospectiveResponse;
import com.dokdok.retrospective.entity.MeetingRetrospective;
import com.dokdok.retrospective.entity.TopicRetrospectiveSummary;
import com.dokdok.retrospective.repository.RetrospectiveRepository;
import com.dokdok.retrospective.repository.TopicRetrospectiveSummaryRepository;
import com.dokdok.topic.entity.Topic;
import com.dokdok.topic.entity.TopicStatus;
import com.dokdok.topic.repository.TopicRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MeetingRetrospectiveService {

    private final MeetingRepository meetingRepository;
    private final TopicRepository topicRepository;
    private final RetrospectiveValidator retrospectiveValidator;
    private final TopicRetrospectiveSummaryRepository topicRetrospectiveSummaryRepository;
    private final RetrospectiveRepository retrospectiveRepository;

    public MeetingRetrospectiveResponse getMeetingRetrospective(Long meetingId){
        // Meeting 조회
        Long userId = SecurityUtil.getCurrentUserId();
        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new MeetingException(MeetingErrorCode.MEETING_NOT_FOUND));

        // 권한 검증
        retrospectiveValidator.validateMeetingRetrospectiveAccess(
                meeting.getGathering().getId(),
                meetingId,
                userId
        );

        // 확정된 토픽 조회 (confirmOrder 순)
        List<Topic> topics = topicRepository.findByMeetingIdAndTopicStatusOrderByConfirmOrderAsc(meetingId, TopicStatus.CONFIRMED);

        // 토픽별 요약 조회
        List<Long> topicIds = topics.stream()
                .map(Topic::getId)
                .toList();

        Map<Long, TopicRetrospectiveSummary> summaryMap = topicRetrospectiveSummaryRepository
                .findAllByTopicIdIn(topicIds)
                .stream()
                .collect(Collectors.toMap(s-> s.getTopic().getId(), s->s));

        // 토픽별 회고 조회
        List<MeetingRetrospective> allComments = retrospectiveRepository.findAllByMeetingId(meetingId);

        Map<Long, List<MeetingRetrospective>> commentsByTopic = allComments.stream()
                .filter(r-> r.getTopic() != null)
                .collect(Collectors.groupingBy(r-> r.getTopic().getId()));

        List<MeetingRetrospectiveResponse.TopicResponse> topicResponses = topics.stream()
                .map(topic -> MeetingRetrospectiveResponse.TopicResponse.from(
                        topic,
                        summaryMap.get(topic.getId()),
                        buildCommentResponses(commentsByTopic.get(topic.getId()))
                ))
                .toList();

        return MeetingRetrospectiveResponse.from(meeting,topicResponses);
    }

    private List<MeetingRetrospectiveResponse.CommentResponse> buildCommentResponses(List<MeetingRetrospective> comments) {
        if (comments == null || comments.isEmpty()) {
            return List.of();
        }
        return comments.stream()
                .map(MeetingRetrospectiveResponse.CommentResponse::from)
                .toList();
    }
}

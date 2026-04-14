package com.dokdok.meeting.service;

import com.dokdok.gathering.service.GatheringValidator;
import com.dokdok.meeting.dto.MeetingDetailResponse;
import com.dokdok.meeting.dto.MeetingRetrospectiveStatus;
import com.dokdok.meeting.entity.Meeting;
import com.dokdok.meeting.entity.MeetingMember;
import com.dokdok.meeting.repository.MeetingMemberRepository;
import com.dokdok.retrospective.repository.PersonalRetrospectiveRepository;
import com.dokdok.retrospective.repository.TopicRetrospectiveSummaryRepository;
import com.dokdok.storage.service.StorageService;
import com.dokdok.topic.entity.Topic;
import com.dokdok.topic.entity.TopicStatus;
import com.dokdok.topic.repository.TopicRepository;
import com.dokdok.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class MeetingFetchService {

    private final MeetingValidator meetingValidator;
    private final GatheringValidator gatheringValidator;
    private final MeetingMemberRepository meetingMemberRepository;
    private final TopicRepository topicRepository;
    private final TopicRetrospectiveSummaryRepository topicRetrospectiveSummaryRepository;
    private final PersonalRetrospectiveRepository personalRetrospectiveRepository;
    private final StorageService storageService;

    /**
     * 약속 상세 조회
     */
    @Transactional(readOnly = true)
    public MeetingDetailResponse findMeetingAsync(Long meetingId, Long userId) {
        Meeting meeting = meetingValidator.findMeetingOrThrow(meetingId);
        gatheringValidator.validateMembership(meeting.getGathering().getId(), userId);

        List<MeetingMember> meetingMembers =
                meetingMemberRepository.findAllByMeetingId(meetingId);
        Map<Long, String> profileImageUrlMap = buildProfileImageUrlMap(meetingMembers);

        LocalDateTime confirmedTopicDate = topicRepository
                .findConfirmedTopicDateByMeetingId(meetingId, TopicStatus.CONFIRMED);
        boolean confirmedTopic = confirmedTopicDate != null;

        MeetingRetrospectiveStatus retrospectiveStatus = resolveMeetingRetrospectiveStatus(meetingId, meeting);

        boolean personalRetrospectiveWritten = personalRetrospectiveRepository
                .existsByMeetingIdAndUserId(meetingId, userId);

        return MeetingDetailResponse.from(
                meeting,
                meetingMembers,
                userId,
                confirmedTopic,
                confirmedTopicDate,
                retrospectiveStatus,
                personalRetrospectiveWritten,
                profileImageUrlMap
        );
    }

    private MeetingRetrospectiveStatus resolveMeetingRetrospectiveStatus(Long meetingId, Meeting meeting) {
        if (meeting.isRetrospectivePublished()) {
            return MeetingRetrospectiveStatus.FINAL_PUBLISHED;
        }

        List<Long> topicIds = topicRepository.findConfirmedTopics(meetingId).stream()
                .map(Topic::getId)
                .toList();
        if (topicIds.isEmpty()) {
            return MeetingRetrospectiveStatus.NOT_CREATED;
        }

        boolean hasSummary = !topicRetrospectiveSummaryRepository.findAllByTopicIdIn(topicIds).isEmpty();
        return hasSummary
                ? MeetingRetrospectiveStatus.AI_SUMMARY_COMPLETED
                : MeetingRetrospectiveStatus.NOT_CREATED;
    }

    public Map<Long, String> buildProfileImageUrlMap(List<MeetingMember> members) {
        Map<Long, String> result = new HashMap<>();
        members.forEach(member -> {
            User user = member.getUser();
            if (user == null || user.getId() == null) return;
            result.put(user.getId(), storageService.getPublicProfileImageUrl(user.getProfileImageUrl()));
        });
        return result;
    }
}

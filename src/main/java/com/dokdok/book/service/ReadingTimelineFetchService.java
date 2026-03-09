package com.dokdok.book.service;

import com.dokdok.book.dto.response.PersonalReadingRecordListResponse;
import com.dokdok.book.dto.response.ReadingTimelinePreOpinionResponse;
import com.dokdok.book.entity.PersonalReadingRecord;
import com.dokdok.book.entity.ReflectionRecordType;
import com.dokdok.book.repository.PersonalReadingRecordRepository;
import com.dokdok.meeting.entity.Meeting;
import com.dokdok.meeting.repository.MeetingRepository;
import com.dokdok.retrospective.dto.projection.ChangedThoughtProjection;
import com.dokdok.retrospective.dto.projection.FreeTextProjection;
import com.dokdok.retrospective.dto.projection.OtherPerspectiveProjection;
import com.dokdok.retrospective.dto.response.RetrospectiveRecordResponse;
import com.dokdok.retrospective.dto.response.RetrospectiveSummaryResponse;
import com.dokdok.retrospective.entity.PersonalMeetingRetrospective;
import com.dokdok.retrospective.entity.TopicRetrospectiveSummary;
import com.dokdok.retrospective.repository.ChangedThoughtRepository;
import com.dokdok.retrospective.repository.FreeTextRepository;
import com.dokdok.retrospective.repository.OthersPerspectiveRepository;
import com.dokdok.retrospective.repository.PersonalRetrospectiveRepository;
import com.dokdok.retrospective.repository.TopicRetrospectiveSummaryRepository;
import com.dokdok.retrospective.service.PersonalRetrospectiveAssembler;
import com.dokdok.topic.entity.Topic;
import com.dokdok.topic.entity.TopicAnswer;
import com.dokdok.topic.entity.TopicStatus;
import com.dokdok.topic.repository.TopicAnswerRepository;
import com.dokdok.topic.repository.TopicRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReadingTimelineFetchService {

    private final PersonalReadingRecordRepository personalReadingRecordRepository;
    private final PersonalRetrospectiveRepository personalRetrospectiveRepository;
    private final ChangedThoughtRepository changedThoughtRepository;
    private final OthersPerspectiveRepository othersPerspectiveRepository;
    private final FreeTextRepository freeTextRepository;
    private final TopicRepository topicRepository;
    private final TopicAnswerRepository topicAnswerRepository;
    private final MeetingRepository meetingRepository;
    private final TopicRetrospectiveSummaryRepository topicRetrospectiveSummaryRepository;
    private final PersonalRetrospectiveAssembler personalRetrospectiveAssembler;

    public Map<Long, PersonalReadingRecordListResponse> fetchReadingRecords(
            List<Long> recordIds,
            Long personalBookId,
            Long userId
    ) {
        if (recordIds.isEmpty()) {
            return Map.of();
        }
        List<PersonalReadingRecord> records =
                personalReadingRecordRepository.findByIdInAndPersonalBook_IdAndUserId(
                        recordIds, personalBookId, userId
                );
        Map<Long, PersonalReadingRecordListResponse> map = new HashMap<>();
        for (PersonalReadingRecord record : records) {
            map.put(record.getId(), PersonalReadingRecordListResponse.from(record));
        }
        return map;
    }

    public Map<Long, RetrospectiveRecordResponse> fetchRetrospectives(
            List<Long> retrospectiveIds,
            Long userId
    ) {
        if (retrospectiveIds.isEmpty()) {
            return Map.of();
        }

        List<PersonalMeetingRetrospective> retrospectives =
                personalRetrospectiveRepository.findByIdsWithMeeting(retrospectiveIds, userId);

        if (retrospectives.isEmpty()) {
            return Map.of();
        }

        List<Long> ids = retrospectives.stream()
                .map(PersonalMeetingRetrospective::getId)
                .toList();

        Map<Long, List<ChangedThoughtProjection>> changedThoughtsMap =
                changedThoughtRepository.findByRetrospectiveIds(ids)
                        .stream()
                        .collect(groupingBy(ChangedThoughtProjection::retrospectiveId));

        Map<Long, List<OtherPerspectiveProjection>> othersPerspectivesMap =
                othersPerspectiveRepository.findByRetrospectiveIds(ids)
                        .stream()
                        .collect(groupingBy(OtherPerspectiveProjection::retrospectiveId));

        Map<Long, List<FreeTextProjection>> freeTextsMap =
                freeTextRepository.findByRetrospectiveIds(ids)
                        .stream()
                        .collect(groupingBy(FreeTextProjection::retrospectiveId));

        List<RetrospectiveRecordResponse> responses = personalRetrospectiveAssembler.assembleRecords(
                retrospectives,
                changedThoughtsMap,
                othersPerspectivesMap,
                freeTextsMap
        );

        Map<Long, RetrospectiveRecordResponse> map = new HashMap<>();
        for (RetrospectiveRecordResponse response : responses) {
            map.put(response.retrospectiveId(), response);
        }
        return map;
    }

    public Map<Long, ReadingTimelinePreOpinionResponse> fetchPreOpinions(
            List<Long> meetingIds,
            Long userId
    ) {
        if (meetingIds.isEmpty()) {
            return Map.of();
        }

        List<Meeting> meetings = meetingRepository.findByIdInWithGathering(meetingIds);
        Map<Long, Meeting> meetingMap = new HashMap<>();
        for (Meeting meeting : meetings) {
            meetingMap.put(meeting.getId(), meeting);
        }

        List<Topic> topics = topicRepository.findTopicsInfoByMeetingIds(meetingIds);
        Map<Long, List<Topic>> topicsByMeeting = topics.stream()
                .collect(groupingBy(topic -> topic.getMeeting().getId()));

        List<TopicAnswer> answers = topicAnswerRepository.findByMeetingIdsUserId(meetingIds, userId);
        Map<Long, Map<Long, TopicAnswer>> answersByMeeting = new HashMap<>();
        for (TopicAnswer answer : answers) {
            Long meetingId = answer.getTopic().getMeeting().getId();
            answersByMeeting
                    .computeIfAbsent(meetingId, key -> new HashMap<>())
                    .put(answer.getTopic().getId(), answer);
        }

        Map<Long, ReadingTimelinePreOpinionResponse> map = new HashMap<>();
        for (Long meetingId : meetingIds) {
            Meeting meeting = meetingMap.get(meetingId);
            if (meeting == null) {
                continue;
            }
            List<Topic> meetingTopics = topicsByMeeting.getOrDefault(meetingId, List.of());
            meetingTopics = meetingTopics.stream()
                    .sorted(Comparator
                            .comparing(Topic::getConfirmOrder, Comparator.nullsLast(Integer::compareTo))
                            .thenComparing(Topic::getId))
                    .toList();

            Map<Long, TopicAnswer> answerMap = answersByMeeting.getOrDefault(meetingId, Map.of());
            List<ReadingTimelinePreOpinionResponse.TopicAnswerInfo> items = meetingTopics.stream()
                    .map(topic -> new ReadingTimelinePreOpinionResponse.TopicAnswerInfo(
                            topic.getTitle(),
                            topic.getDescription(),
                            topic.getConfirmOrder(),
                            answerMap.containsKey(topic.getId())
                                    ? answerMap.get(topic.getId()).getContent()
                                    : null
                    ))
                    .toList();

            ReadingTimelinePreOpinionResponse response = new ReadingTimelinePreOpinionResponse(
                    "PRE_OPINION",
                    meeting.getGathering().getId(),
                    meeting.getId(),
                    meeting.getGathering().getGatheringName(),
                    meeting.getMeetingStartDate(),
                    items
            );
            map.put(meetingId, response);
        }

        return map;
    }

    public Map<Long, RetrospectiveRecordResponse> fetchGroupRetrospectives(List<Long> meetingIds) {
        if (meetingIds.isEmpty()) {
            return Map.of();
        }

        List<Meeting> meetings = meetingRepository.findByIdInWithGathering(meetingIds);
        Map<Long, RetrospectiveRecordResponse> map = new HashMap<>();
        for (Meeting meeting : meetings) {
            if (!meeting.isRetrospectivePublished() || meeting.getRetrospectivePublishedAt() == null) {
                continue;
            }
            map.put(meeting.getId(), RetrospectiveRecordResponse.of(
                    meeting.getId(),
                    meeting.getGathering().getGatheringName(),
                    ReflectionRecordType.MEETING_RETROSPECTIVE,
                    meeting.getRetrospectivePublishedAt(),
                    List.of(),
                    List.of()
            ));
        }
        return map;
    }

    public Map<Long, RetrospectiveSummaryResponse> fetchMeetingRetrospectiveSummaries(
            List<Long> meetingIds
    ) {
        if (meetingIds.isEmpty()) {
            return Map.of();
        }

        Map<Long, Meeting> meetingMap = meetingRepository.findByIdInWithGathering(meetingIds)
                .stream()
                .collect(Collectors.toMap(Meeting::getId, m -> m));

        List<Topic> confirmedTopics = topicRepository.findTopicsInfoByMeetingIds(meetingIds)
                .stream()
                .filter(t -> TopicStatus.CONFIRMED.equals(t.getTopicStatus()))
                .toList();

        Map<Long, List<Topic>> topicsByMeeting = confirmedTopics.stream()
                .collect(groupingBy(t -> t.getMeeting().getId()));

        List<Long> topicIds = confirmedTopics.stream().map(Topic::getId).toList();

        Map<Long, TopicRetrospectiveSummary> summaryByTopic =
                topicRetrospectiveSummaryRepository.findAllByTopicIdIn(topicIds)
                        .stream()
                        .collect(Collectors.toMap(s -> s.getTopic().getId(), s -> s));

        Map<Long, RetrospectiveSummaryResponse> result = new HashMap<>();
        for (Long meetingId : meetingIds) {
            Meeting meeting = meetingMap.get(meetingId);
            if (meeting == null) {
                continue;
            }
            List<Topic> meetingTopics = topicsByMeeting.getOrDefault(meetingId, List.of())
                    .stream()
                    .sorted(Comparator.comparing(Topic::getConfirmOrder,
                            Comparator.nullsLast(Integer::compareTo)))
                    .toList();

            List<RetrospectiveSummaryResponse.TopicSummaryResponse> topicResponses =
                    meetingTopics.stream()
                            .map(t -> RetrospectiveSummaryResponse.TopicSummaryResponse
                                    .from(t, summaryByTopic.get(t.getId())))
                            .toList();

            result.put(meetingId, RetrospectiveSummaryResponse.from(meeting, topicResponses));
        }
        return result;
    }
}

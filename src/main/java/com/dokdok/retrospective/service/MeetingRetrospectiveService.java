package com.dokdok.retrospective.service;

import com.dokdok.global.response.CursorResponse;
import com.dokdok.global.util.SecurityUtil;
import com.dokdok.meeting.entity.Meeting;
import com.dokdok.meeting.service.MeetingValidator;
import com.dokdok.retrospective.dto.request.MeetingRetrospectiveRequest;
import com.dokdok.retrospective.dto.response.CommentCursor;
import com.dokdok.retrospective.dto.response.MeetingRetrospectiveResponse;
import com.dokdok.retrospective.entity.MeetingRetrospective;
import com.dokdok.retrospective.entity.TopicRetrospectiveSummary;
import com.dokdok.retrospective.exception.RetrospectiveErrorCode;
import com.dokdok.retrospective.exception.RetrospectiveException;
import com.dokdok.retrospective.repository.RetrospectiveRepository;
import com.dokdok.retrospective.repository.TopicRetrospectiveSummaryRepository;
import com.dokdok.storage.service.StorageService;
import com.dokdok.topic.entity.Topic;
import com.dokdok.topic.entity.TopicStatus;
import com.dokdok.topic.repository.TopicRepository;
import com.dokdok.topic.service.TopicValidator;
import com.dokdok.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MeetingRetrospectiveService {

    private final TopicRepository topicRepository;
    private final RetrospectiveValidator retrospectiveValidator;
    private final TopicRetrospectiveSummaryRepository topicRetrospectiveSummaryRepository;
    private final RetrospectiveRepository retrospectiveRepository;
    private final MeetingValidator meetingValidator;
    private final TopicValidator topicValidator;
    private final StorageService storageService;

    /**
     * 공동 회고 조회 ( 토픽 정보 + 요약 + 키포인트, 코멘트 제외 )
     */
    public MeetingRetrospectiveResponse getMeetingRetrospective(Long meetingId){
        Long userId = SecurityUtil.getCurrentUserId();

        Meeting meeting = meetingValidator.findMeetingOrThrow(meetingId);

        // 권한 검증
        retrospectiveValidator.validateMeetingRetrospectiveAccess(
                meeting.getGathering().getId(),
                meetingId,
                userId
        );

        // 확정된 토픽 조회 (confirmOrder 순)
        List<Topic> topics = topicRepository.findByMeetingIdAndTopicStatusOrderByConfirmOrderAsc(meetingId, TopicStatus.CONFIRMED);

        Map<Long, TopicRetrospectiveSummary> summaryMap = buildSummaryMap(topics);

        List<MeetingRetrospectiveResponse.TopicResponse> topicResponses = topics.stream()
                .map(topic -> MeetingRetrospectiveResponse.TopicResponse.from(
                        topic,
                        summaryMap.get(topic.getId())
                ))
                .toList();

        return MeetingRetrospectiveResponse.from(meeting, topicResponses);
    }

    /**
     * 토픽별 코멘트 조회 (무한 스크롤)
     */
    public CursorResponse<MeetingRetrospectiveResponse.CommentResponse, CommentCursor> getTopicComments(
            Long meetingId,
            Long topicId,
            int pageSize,
            LocalDateTime cursorCreatedAt,
            Long cursorCommentId
    ) {
        Long userId = SecurityUtil.getCurrentUserId();
        Meeting meeting = meetingValidator.findMeetingOrThrow(meetingId);

        retrospectiveValidator.validateMeetingRetrospectiveAccess(meeting.getGathering().getId(), meetingId, userId);

        topicValidator.getTopicInMeeting(topicId, meetingId);

        return fetchComments(topicId, pageSize, cursorCreatedAt, cursorCommentId);
    }

    @Transactional
    public MeetingRetrospectiveResponse.CommentResponse createMeetingRetrospective(
            Long meetingId,
            MeetingRetrospectiveRequest request
    ){
        // user 조회
        User user = SecurityUtil.getCurrentUser().getUser();
        Long userId = SecurityUtil.getCurrentUserId();

        // Meeting 조회
        Meeting meeting = meetingValidator.findMeetingOrThrow(meetingId);

        // 권한 검증
        retrospectiveValidator.validateMeetingRetrospectiveAccess(meeting.getGathering().getId(),meetingId,userId);

        // Topic 조회
        Topic topic = topicValidator.getTopicInMeeting(request.topicId(), meetingId);

        // save
        MeetingRetrospective retrospective = MeetingRetrospective.of(meeting, user, topic, request.comment());
        MeetingRetrospective saved = retrospectiveRepository.save(retrospective);

        return buildCommentResponse(saved);
    }

    @Transactional
    public void deleteMeetingRetrospective(Long meetingId, Long meetingRetrospectiveId) {
        Long userId = SecurityUtil.getCurrentUserId();

        MeetingRetrospective retrospective = retrospectiveRepository
                .findByIdAndMeetingId(meetingRetrospectiveId, meetingId)
                .orElseThrow(() -> new RetrospectiveException(RetrospectiveErrorCode.MEETING_RETROSPECTIVE_NOT_FOUND));

        retrospectiveValidator.validateMeetingRetrospectiveDeletePermission(retrospective, userId);

        retrospectiveRepository.delete(retrospective);
    }

    private Map<Long, TopicRetrospectiveSummary> buildSummaryMap(List<Topic> topics) {
        List<Long> topicIds = topics.stream()
                .map(Topic::getId)
                .toList();

        return topicRetrospectiveSummaryRepository
                .findAllByTopicIdIn(topicIds)
                .stream()
                .collect(Collectors.toMap(
                        summary -> summary.getTopic().getId(),
                        summary -> summary
                ));
    }

    private CursorResponse<MeetingRetrospectiveResponse.CommentResponse, CommentCursor> fetchComments(
            Long topicId,
            int pageSize,
            LocalDateTime cursorCreatedAt,
            Long cursorCommentId
    ) {
        Pageable pageable = PageRequest.of(0, pageSize + 1);
        boolean isFirstPage = cursorCreatedAt == null || cursorCommentId == null;

        List<MeetingRetrospective> comments = isFirstPage
                ? retrospectiveRepository.findByTopicIdFirstPage(topicId, pageable)
                : retrospectiveRepository.findByTopicIdAfterCursor(topicId, cursorCreatedAt, cursorCommentId, pageable);

        boolean hasNext = comments.size() > pageSize;
        List<MeetingRetrospective> pageComments = hasNext
                ? comments.subList(0, pageSize)
                : comments;

        List<MeetingRetrospectiveResponse.CommentResponse> items = pageComments.stream()
                .map(this::buildCommentResponse)
                .toList();

        CommentCursor nextCursor = buildNextCursor(pageComments, hasNext);
        Integer totalCount = isFirstPage ? retrospectiveRepository.countByTopicId(topicId) : null;

        return CursorResponse.of(items, pageSize, hasNext, nextCursor, totalCount);
    }

    private MeetingRetrospectiveResponse.CommentResponse buildCommentResponse(MeetingRetrospective comment) {
        String profileImageUrl = comment.getCreatedBy().getProfileImageUrl();
        String presignedUrl = storageService.getPresignedProfileImage(profileImageUrl);
        return MeetingRetrospectiveResponse.CommentResponse.from(comment, presignedUrl);
    }

    private CommentCursor buildNextCursor(List<MeetingRetrospective> comments, boolean hasNext) {
        if (!hasNext || comments.isEmpty()) {
            return null;
        }
        MeetingRetrospective lastComment = comments.get(comments.size() - 1);
        return CommentCursor.from(lastComment);
    }
}

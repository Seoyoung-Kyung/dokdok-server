package com.dokdok.meeting.service;

import com.dokdok.book.entity.Book;
import com.dokdok.book.exception.BookErrorCode;
import com.dokdok.book.exception.BookException;
import com.dokdok.book.repository.BookRepository;
import com.dokdok.gathering.entity.Gathering;
import com.dokdok.gathering.exception.GatheringErrorCode;
import com.dokdok.gathering.exception.GatheringException;
import com.dokdok.gathering.service.GatheringValidator;
import com.dokdok.gathering.repository.GatheringMemberRepository;
import com.dokdok.gathering.repository.GatheringRepository;
import com.dokdok.global.util.SecurityUtil;
import com.dokdok.meeting.dto.*;
import com.dokdok.meeting.entity.Meeting;
import com.dokdok.meeting.entity.MeetingMember;
import com.dokdok.meeting.entity.MeetingMemberRole;
import com.dokdok.meeting.entity.MeetingStatus;
import com.dokdok.meeting.exception.MeetingErrorCode;
import com.dokdok.meeting.exception.MeetingException;
import com.dokdok.meeting.repository.MeetingMemberRepository;
import com.dokdok.meeting.repository.MeetingRepository;
import com.dokdok.topic.entity.Topic;
import com.dokdok.topic.entity.TopicType;
import com.dokdok.topic.repository.TopicRepository;
import com.dokdok.user.entity.User;
import com.dokdok.user.service.UserValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Service
@RequiredArgsConstructor
public class MeetingService {

    private final MeetingRepository meetingRepository;
    private final MeetingMemberRepository meetingMemberRepository;
    private final TopicRepository topicRepository;
    private final GatheringRepository gatheringRepository;
    private final GatheringMemberRepository gatheringMemberRepository;
    private final GatheringValidator gatheringValidator;
    private final MeetingValidator meetingValidator;
    private final BookRepository bookRepository;
    private final UserValidator userValidator;

    /**
     * 특정 약속의 정보를 확인할 수 있다. 모임에 속한 사용자만 조회 가능
     * @param meetingId 약속 식별자
     * @return 약속 응답 정보
     */
    @Transactional(readOnly = true)
    public MeetingResponse findMeeting(Long meetingId) {

        Long userId = SecurityUtil.getCurrentUserId();
        Meeting meeting = meetingValidator.findMeetingOrThrow(meetingId);

        // 모임 멤버만 약속 조회 가능
        gatheringValidator.validateMembership(meeting.getGathering().getId(), userId);

        List<MeetingMember> meetingMembers = meetingMemberRepository.findAllByMeetingId(meetingId);
        List<Topic> topics = topicRepository.findAllByMeetingId(meetingId);

        return MeetingResponse.from(meeting, meetingMembers, topics);
    }

    /**
     * 모임원이 약속 생성 신청을 할 수 있다. 모임에 속한 사용자만 생성 가능
     * @param request 약속 생성 신청 폼
     * @return 약속 응답 정보
     */
    @Transactional
    public MeetingResponse createMeeting(MeetingCreateRequest request) {
        Long userId = SecurityUtil.getCurrentUserId();

        // 모임 멤버만 약속 생성 가능
        gatheringValidator.validateMembership(request.gatheringId(), userId);

        Gathering gathering = gatheringRepository.findById(request.gatheringId())
                .orElseThrow(() -> new GatheringException(GatheringErrorCode.GATHERING_NOT_FOUND));

        Book book = bookRepository.findById(request.bookId())
                .orElseThrow(() -> new BookException(BookErrorCode.BOOK_NOT_FOUND));

        User user = userValidator.findUserOrThrow(userId);

        Integer maxParticipants = request.maxParticipants();
        if (maxParticipants == null) {
            maxParticipants = gatheringMemberRepository
                    .countByGatheringIdAndRemovedAtIsNull(gathering.getId());
        }

        // 최대 참가 인원 검증
        validateMaxParticipants(maxParticipants, gathering.getId());

        Meeting meeting = Meeting.create(request, gathering, book, user, maxParticipants);
        Meeting savedMeeting = meetingRepository.save(meeting);

        return MeetingResponse.from(savedMeeting, List.of(), List.of());
    }

    /**
     * 약속 상태를 변경한다. 모임장만 상태 변경 가능
     * @param meetingId 약속 식별자
     * @param meetingStatus 약속 상태
     * @return 약속 상태 응답 정보
     */
    @Transactional
    public MeetingStatusResponse changeMeetingStatus(Long meetingId, MeetingStatus meetingStatus) {

        Long userId = SecurityUtil.getCurrentUserId();
        Meeting meeting = meetingValidator.findMeetingOrThrow(meetingId);

        // 모임장만 상태 변경 가능
        gatheringValidator.validateLeader(meeting.getGathering().getId(), userId);

        if (meetingStatus == MeetingStatus.CONFIRMED) {
            validateConfirmable(meeting);
            ensureLeaderMember(meeting);
        }

        meeting.changeStatus(meetingStatus);

        return MeetingStatusResponse.from(meeting);
    }

    /**
     * 최대 참가 인원의 유효성을 검증한다.
     * @param maxParticipants 최대 참가 인원
     * @param gatheringId 모임 식별자
     */
    private void validateMaxParticipants(Integer maxParticipants, Long gatheringId) {
        if (maxParticipants == null) {
            return;
        }
        if (maxParticipants < 1) {
            throw new MeetingException(MeetingErrorCode.INVALID_MAX_PARTICIPANTS);
        }

        int totalGatheringMembers = gatheringMemberRepository
                .countByGatheringIdAndRemovedAtIsNull(gatheringId);
        if (maxParticipants > totalGatheringMembers) {
            throw new MeetingException(MeetingErrorCode.INVALID_MAX_PARTICIPANTS);
        }
    }

    /**
     * 동일 모임 내 확정된 약속이 이미 존재하는지 검증한다.
     */
    private void validateConfirmable(Meeting meeting) {
        Long gatheringId = meeting.getGathering().getId();
        boolean hasConfirmedMeeting = meetingRepository
                .existsByGatheringIdAndMeetingStatus(gatheringId, MeetingStatus.CONFIRMED);
        if (hasConfirmedMeeting) {
            throw new MeetingException(MeetingErrorCode.INVALID_MEETING_STATUS_CHANGE,
                    "이미 확정된 약속이 존재합니다.");
        }
    }

    /**
     * 약속장을 미팅 멤버로 포함하고 역할을 LEADER로 설정한다.
     */
    private void ensureLeaderMember(Meeting meeting) {
        User leader = meeting.getMeetingLeader();
        if (leader == null) {
            throw new MeetingException(MeetingErrorCode.INVALID_MEETING_STATUS_CHANGE,
                    "약속장 정보를 찾을 수 없습니다.");
        }

        MeetingMember meetingMember = meetingMemberRepository
                .findByMeetingIdAndUserId(meeting.getId(), leader.getId())
                .orElseGet(() -> MeetingMember.builder()
                        .meeting(meeting)
                        .user(leader)
                        .build());

        meetingMember.changeRole(MeetingMemberRole.LEADER);
        meetingMemberRepository.save(meetingMember);
    }

    /**
     * 약속 참가를 신청한다.
     * @param meetingId 약속 식별자
     * @return 신청 완료된 약속 식별자
     */
    @Transactional
    public Long joinMeeting(Long meetingId) {

        Long userId = SecurityUtil.getCurrentUserId();

        Meeting meeting = meetingValidator.findMeetingOrThrow(meetingId);

        gatheringValidator.validateMembership(meeting.getGathering().getId(), userId);

        if (restoreCanceledMemberIfExists(meetingId, userId, meeting.getMaxParticipants())) {
            return meetingId;
        }

        meetingValidator.validateCapacity(meetingId, meeting.getMaxParticipants());

        User user = userValidator.findUserOrThrow(userId);

        saveMeetingMember(meeting, user);

        return meetingId;
    }

    /**
     * 기존 취소 이력이 있으면 정원 검증 후 복구하고, 이미 참여 상태면 예외를 던진다.
     */
    private boolean restoreCanceledMemberIfExists(Long meetingId, Long userId, Integer maxParticipants) {
        MeetingMember existingMember = meetingMemberRepository.findAnyByMeetingIdAndUserId(meetingId, userId)
                .orElse(null);
        if (existingMember == null) {
            return false;
        }
        if (existingMember.getCanceledAt() == null) {
            throw new MeetingException(MeetingErrorCode.MEETING_ALREADY_JOINED);
        }

        // 복구 전 정원 검증
        meetingValidator.validateCapacity(meetingId, maxParticipants);

        existingMember.restore();
        return true;
    }

    /**
     * 약속 참여 멤버를 생성 후 저장한다.
     */
    private void saveMeetingMember(Meeting meeting, User user) {
        MeetingMember meetingMember = MeetingMember.builder()
                .meeting(meeting)
                .user(user)
                .build();

        meetingMemberRepository.save(meetingMember);
    }

    /**
     * 약속 참가 신청을 취소한다. 약속 시작까지 24시간 미만 남았으면 취소 불가
     * @param meetingId 약속 식별자
     * @return 취소한 약속 식별자
     */
    @Transactional
    public Long cancelMeeting(Long meetingId) {
        Long userId = SecurityUtil.getCurrentUserId();
        Meeting meeting = meetingValidator.findMeetingOrThrow(meetingId);

        // 약속 시간 24간 이내이면 취소 불가능
        LocalDateTime meetingStartDate = meeting.getMeetingStartDate();
        if (meetingStartDate != null
                && meetingStartDate.isBefore(LocalDateTime.now().plusHours(24))) {
            throw new MeetingException(MeetingErrorCode.MEETING_CANCEL_NOT_ALLOWED);
        }

        // 이미 취소한 약속인지 확인
        MeetingMember meetingMember = meetingValidator.getAnyMeetingMember(meetingId, userId);
        if (meetingMember.getCanceledAt() != null) {
            throw new MeetingException(MeetingErrorCode.MEETING_ALREADY_CANCELED);
        }

        meetingMember.cancel();
        // 참가 취소자가 주제까지 제안한 경우 주제 soft delete
        topicRepository.softDeleteByMeetingIdAndProposedById(meetingId, userId);

        return meetingId;
    }

    /**
     * 약속 제안자가 약속을 수정한다.
     * 진행 전 상태의 약속만 수정 가능
     * 현재 참여 인원 수보다 작을 순 없음
     * maxParticipants가 null일 경우 기존 값 유지
     * endDate는 startDate 보다 이전일 수 없다
     * @param meetingId 약속 식별자
     * @param request 수정 요청 폼
     * @return 수정 완료된 응답
     */
    @Transactional
    public MeetingUpdateResponse updateMeeting(Long meetingId, MeetingUpdateRequest request) {
        Long userId = SecurityUtil.getCurrentUserId();

        Meeting meeting = meetingValidator.findMeetingOrThrow(meetingId);
        Long originMeetingId = meeting.getId();
        meetingValidator.validateMeetingLeader(meeting, userId);

        validateUpdatableStatus(meeting);

        Long gatheringId = meeting.getGathering().getId();
        validateMaxParticipants(request.maxParticipants(), gatheringId);

        int curMemberCount = meetingValidator.countActiveMembers(originMeetingId);
        validateMaxParticipantsNotLessThanCurrent(request.maxParticipants(), curMemberCount);

        validateMeetingDates(request, meeting);

        meeting.update(request);

        return MeetingUpdateResponse.from(meeting);
    }

    /**
     * 현재 모임 인원보다 약속 수정 요청에 대한 인원 수가 많지 않은지 확인한다.
     */
    private void validateMaxParticipantsNotLessThanCurrent(Integer maxParticipants, int currentParticipantCount) {
        if (maxParticipants != null && maxParticipants < currentParticipantCount) {
            throw new MeetingException(
                    MeetingErrorCode.MAX_PARTICIPANTS_LESS_THAN_CURRENT
            );
        }
    }

    /**
     * 수정 가능한 약속 상태인지 확인한다. (PENDING, CONFIRMED만 가능)
     */
    private void validateUpdatableStatus(Meeting meeting) {
        if (meeting.getMeetingStatus() == MeetingStatus.DONE) {
            throw new MeetingException(
                    MeetingErrorCode.INVALID_MEETING_STATUS_CHANGE,
                    "종료된 약속은 수정할 수 없습니다."
            );
        }
    }

    /**
     * 종료 일시가 시작 일시보다 이전인지 확인한다.
     */
    private void validateMeetingDates(MeetingUpdateRequest request, Meeting meeting) {
        LocalDateTime startDate = request.startDate() != null
                ? request.startDate()
                : meeting.getMeetingStartDate();
        LocalDateTime endDate = request.endDate() != null
                ? request.endDate()
                : meeting.getMeetingEndDate();
        if (startDate != null && endDate != null && endDate.isBefore(startDate)) {
            throw new MeetingException(
                    MeetingErrorCode.INVALID_MEETING_STATUS_CHANGE,
                    "종료 일시는 시작 일시보다 이전일 수 없습니다."
            );
        }
    }

    /**
     * 약속 리스트를 조회한다.
     * 약속 : 모임에서 약속이 확정된 전체 약속
     * 다가오는 약속 : 약속이 확정된 것들 중 3일 이내인 약속
     * 완료된 약속 : 약속이 완전히 끝난 약속
     * 내가 참여한 약속 : 완전히 끝난 약속 중 내가 참여한 약속
     * @param gatheringId 모임 식별자
     * @param filter 약속 리스트 필터
     * @param pageable 페이징 정보
     * @return MeetingListResponse
     */
    public MeetingListResponse meetingList(Long gatheringId, MeetingListFilter filter, Pageable pageable) {
        Long userId = SecurityUtil.getCurrentUserId();
        gatheringValidator.validateMembership(gatheringId, userId);

        return switch (filter) {
            case ALL -> getAllMeetings(gatheringId, pageable, userId);
            case UPCOMING -> getUpcomingMeetings(gatheringId, pageable, userId);
            case DONE -> getDoneMeetings(gatheringId, pageable, userId);
            case JOINED -> getJoinedMeetings(gatheringId, pageable, userId);
        };

    }

    /**
     * 약속 탭별 뱃지 카운트 수를 반환한다.
     * @param gatheringId 모임 식별자
     * @return MeetingTabCountsResponse
     */
    public MeetingTabCountsResponse getMeetingTabCounts(Long gatheringId) {
        Long userId = SecurityUtil.getCurrentUserId();
        gatheringValidator.validateMembership(gatheringId, userId);

        int allCount = meetingRepository
                .countByGatheringIdAndMeetingStatus(gatheringId, MeetingStatus.CONFIRMED);
        int doneCount = meetingRepository
                .countByGatheringIdAndMeetingStatus(gatheringId, MeetingStatus.DONE);

        LocalDateTime now = LocalDateTime.now();
        int upcomingCount = meetingRepository.countUpcomingMeetings(
                gatheringId,
                MeetingStatus.CONFIRMED,
                now,
                now.plusDays(3)
        );

        int joinedCount = meetingMemberRepository.countMeetingsByUserIdAndStatus(
                userId,
                gatheringId,
                MeetingStatus.DONE
        );

        return MeetingTabCountsResponse.builder()
                .all(allCount)
                .upcoming(upcomingCount)
                .done(doneCount)
                .joined(joinedCount)
                .build();
    }

    /**
     * 모임의 약속 중 확정된 리스트를 전부 반환한다.
     */
    private MeetingListResponse getAllMeetings(
            Long gatheringId,
            Pageable pageable,
            Long userId
    ) {
        Page<Meeting> meetingPage = meetingRepository.findByGatheringIdAndMeetingStatus(
                gatheringId,
                MeetingStatus.CONFIRMED,
                pageable
        );
        return buildMeetingListResponse(meetingPage, userId, gatheringId);
    }

    /**
     * 다가오는 약속 리스트를 반환한다.
     */
    private MeetingListResponse getUpcomingMeetings(
            Long gatheringId,
            Pageable pageable,
            Long userId
    ) {
        LocalDateTime now = LocalDateTime.now();

        Page<Meeting> meetingPage = meetingRepository
                .findByGatheringIdAndMeetingStatusAndMeetingStartDateBetween(
                        gatheringId,
                        MeetingStatus.CONFIRMED,
                        now,
                        now.plusDays(3),
                        pageable
                );

        return buildMeetingListResponse(meetingPage, userId, gatheringId);
    }

    /**
     * 완료된 약속 리스트를 반환한다.
     */
    private MeetingListResponse getDoneMeetings(
            Long gatheringId,
            Pageable pageable,
            Long userId
    ) {
        Page<Meeting> meetingPage = meetingRepository.findByGatheringIdAndMeetingStatus(
                gatheringId,
                MeetingStatus.DONE,
                pageable
        );
        return buildMeetingListResponse(meetingPage, userId, gatheringId);
    }

    /**
     * 완료된 약속 중 내가 참여했던 약속 리스트를 반환한다.
     */
    private MeetingListResponse getJoinedMeetings(
            Long gatheringId,
            Pageable pageable,
            Long userId
    ) {
        Page<Meeting> meetingPage = meetingMemberRepository.findMeetingsByUserIdAndStatus(
                userId,
                gatheringId,
                MeetingStatus.DONE,
                pageable
        );
        return buildMeetingListResponse(meetingPage, userId, gatheringId);
    }

    private MeetingListResponse buildMeetingListResponse(
            Page<Meeting> meetingPage,
            Long userId,
            Long gatheringId
    ) {
        List<Meeting> meetings = meetingPage.getContent();
        if (meetings.isEmpty()) {
            return MeetingListResponse.builder()
                    .items(List.of())
                    .totalCount((int) meetingPage.getTotalElements())
                    .currentPage(meetingPage.getNumber())
                    .pageSize(meetingPage.getSize())
                    .totalPages(meetingPage.getTotalPages())
                    .build();
        }

        List<Long> meetingIds = meetings.stream()
                .map(Meeting::getId)
                .toList();

        Map<Long, List<TopicType>> topicTypesByMeetingId = findTopicTypes(meetingIds);
        Set<Long> joinedMeetingIds = new HashSet<>(
                meetingMemberRepository.findActiveMeetingIdsByUserIdAndGatheringId(userId, gatheringId)
        );

        List<MeetingListResponse.Item> items = new ArrayList<>();
        for (Meeting meeting : meetings) {
            List<TopicType> topicTypes = topicTypesByMeetingId.getOrDefault(meeting.getId(), List.of());
            boolean joined = joinedMeetingIds.contains(meeting.getId());

            items.add(MeetingListResponse.Item.builder()
                    .meetingId(meeting.getId())
                    .meetingName(meeting.getMeetingName())
                    .bookName(meeting.getBook().getBookName())
                    .startDateTime(meeting.getMeetingStartDate())
                    .endDateTime(meeting.getMeetingEndDate())
                    .topicTypes(topicTypes)
                    .joined(joined)
                    .meetingStatus(meeting.getMeetingStatus())
                    .build());
        }

        return MeetingListResponse.builder()
                .items(items)
                .totalCount((int) meetingPage.getTotalElements())
                .currentPage(meetingPage.getNumber())
                .pageSize(meetingPage.getSize())
                .totalPages(meetingPage.getTotalPages())
                .build();
    }

    /**
     * 약속의 주제 타입 리스트를 반환한다.
     */
    private Map<Long, List<TopicType>> findTopicTypes(List<Long> meetingIds) {
        List<Object[]> rows = topicRepository.findTopicTypesByMeetingIds(meetingIds);
        Map<Long, Set<TopicType>> result = new HashMap<>();

        for (Object[] row : rows) {
            Long meetingId = (Long) row[0];
            TopicType topicType = (TopicType) row[1];
            result.computeIfAbsent(meetingId, key -> new LinkedHashSet<>()).add(topicType);
        }

        Map<Long, List<TopicType>> listResult = new HashMap<>();
        for (Map.Entry<Long, Set<TopicType>> entry : result.entrySet()) {
            listResult.put(entry.getKey(), new ArrayList<>(entry.getValue()));
        }
        return listResult;
    }
}

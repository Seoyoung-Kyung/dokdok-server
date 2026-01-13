package com.dokdok.meeting.service;

import com.dokdok.book.entity.Book;
import com.dokdok.book.repository.BookRepository;
import com.dokdok.gathering.entity.Gathering;
import com.dokdok.gathering.repository.GatheringMemberRepository;
import com.dokdok.gathering.repository.GatheringRepository;
import com.dokdok.meeting.dto.MeetingCreateRequest;
import com.dokdok.meeting.dto.MeetingResponse;
import com.dokdok.meeting.dto.MeetingStatusResponse;
import com.dokdok.meeting.entity.Meeting;
import com.dokdok.meeting.entity.MeetingMember;
import com.dokdok.meeting.entity.MeetingMemberRole;
import com.dokdok.meeting.entity.MeetingStatus;
import com.dokdok.meeting.exception.MeetingErrorCode;
import com.dokdok.meeting.exception.MeetingException;
import com.dokdok.meeting.repository.MeetingMemberRepository;
import com.dokdok.meeting.repository.MeetingRepository;
import com.dokdok.topic.entity.Topic;
import com.dokdok.topic.repository.TopicRepository;
import com.dokdok.user.entity.User;
import com.dokdok.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MeetingService {

    private final MeetingRepository meetingRepository;
    private final MeetingMemberRepository meetingMemberRepository;
    private final TopicRepository topicRepository;
    private final GatheringRepository gatheringRepository;
    private final GatheringMemberRepository gatheringMemberRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;

    /**
     * 특정 약속의 정보를 확인할 수 있다.
     * @param meetingId 약속 식별자
     * @return 약속 응답 정보
     */
    @Transactional(readOnly = true)
    public MeetingResponse findMeeting(Long meetingId) {

        // todo : 모임에 속해있는 사용자만 확일할 수 있는 제약 사항 추가 -> 시큐리티 role로 확인할지, 따로 메서드로 만들지
        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new MeetingException(MeetingErrorCode.MEETING_NOT_FOUND));

        List<MeetingMember> meetingMembers = meetingMemberRepository.findAllByMeetingId(meetingId);
        List<Topic> topics = topicRepository.findAllByMeetingId(meetingId);

        return MeetingResponse.from(meeting, meetingMembers, topics);
    }

    /**
     * 모임원이 약속 생성 신청을 할 수 있다.
     * @param request 약속 생성 신청 폼
     * @param userId 신청인 식별자
     * @return 약속 응답 정보
     */
    @Transactional
    public MeetingResponse createMeeting(MeetingCreateRequest request, Long userId) {
        // todo : 모임에 속해있는 사용자만 약속을 생성할 수 있는 제약 사항 추가
        Gathering gathering = gatheringRepository.findById(request.gatheringId())
                .orElseThrow(() -> new MeetingException(MeetingErrorCode.GATHERING_NOT_FOUND));

        // todo : 개별 Book, User 에러코드 생기면 그 Exception으로 변경하는 게 좋을 듯함
        Book book = bookRepository.findById(request.bookId())
                .orElseThrow(() -> new MeetingException(MeetingErrorCode.BOOK_NOT_FOUND));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new MeetingException(MeetingErrorCode.USER_NOT_FOUND));

        Integer maxParticipants = request.maxParticipants();
        if (maxParticipants == null) {
            maxParticipants = gatheringMemberRepository
                    .countByGatheringIdAndRemovedAtIsNull(gathering.getId());
        }

        Meeting meeting = Meeting.create(request, gathering, book, user, maxParticipants);
        Meeting savedMeeting = meetingRepository.save(meeting);

        return MeetingResponse.from(savedMeeting, List.of(), List.of());
    }

    /**
     * 약속 상태를 변경한다. - PENDING 상태일 때만 CONFIRMED로 변경 가능
     * @param meetingId 약속 식별자
     * @param meetingStatus 약속 상태
     * @return 약속 상태 응답 정보
     */
    @Transactional
    public MeetingStatusResponse changeMeetingStatus(Long meetingId, MeetingStatus meetingStatus) {

        // todo : 모임장만 바꿀 수 있도록 meetingValidator 머지된 후 추가 예정
        Meeting meeting = getMeetingOrThrow(meetingId);

        if (meetingStatus == MeetingStatus.CONFIRMED) {
            validateConfirmable(meeting);
            ensureLeaderMember(meeting);
        }

        meeting.changeStatus(meetingStatus);

        return MeetingStatusResponse.from(meeting);
    }

    /**
     * meetingId로 약속을 조회하고 없으면 예외를 던진다.
     */
    private Meeting getMeetingOrThrow(Long meetingId) {
        return meetingRepository.findById(meetingId)
                .orElseThrow(() -> new MeetingException(MeetingErrorCode.MEETING_NOT_FOUND));
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
}

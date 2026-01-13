package com.dokdok.meeting.service;

import com.dokdok.meeting.entity.Meeting;
import com.dokdok.meeting.entity.MeetingMember;
import com.dokdok.meeting.entity.MeetingStatus;
import com.dokdok.meeting.exception.MeetingErrorCode;
import com.dokdok.meeting.exception.MeetingException;
import com.dokdok.meeting.repository.MeetingMemberRepository;
import com.dokdok.meeting.repository.MeetingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MeetingValidator {

    private final MeetingRepository meetingRepository;
    private final MeetingMemberRepository meetingMemberRepository;

    /**
     * 약속이 모임에 속하는지 검증한다.
     */
    public void validateMemberInGathering(Long meetingId, Long gatheringId) {
        boolean isMemberInGathering = meetingRepository
                .existsByIdAndGatheringId(meetingId, gatheringId);

        if (!isMemberInGathering) {
            throw new MeetingException(MeetingErrorCode.NOT_GATHERING_MEETING);
        }
    }

    /**
     * meetingId로 약속을 조회하고 없으면 예외를 던진다.
     */
    public Meeting findMeetingOrThrow(Long meetingId) {
        return meetingRepository.findById(meetingId)
                .orElseThrow(() -> new MeetingException(MeetingErrorCode.MEETING_NOT_FOUND));
    }

    /**
     * 약속 상태가 PENDING인지 검증한다.
     */
    public void validateMeetingStatus(Long meetingId) {
        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new MeetingException(MeetingErrorCode.MEETING_NOT_FOUND));

        if (meeting.getMeetingStatus() != MeetingStatus.PENDING) {
            throw new MeetingException(MeetingErrorCode.MEETING_ALREADY_CONFIRMED);
        }
    }

    /**
     * 사용자의 약속 참여 여부를 확인한다.
     */
    public boolean isMeetingMember(Long meetingId, Long userId) {
        return meetingMemberRepository.findByMeetingIdAndUserId(meetingId, userId).isPresent();
    }

    /**
     * 약속 정원 마감 여부를 검증한다.
     */
    public void validateCapacity(Long meetingId, Integer maxParticipants) {
        if (maxParticipants == null) {
            return;
        }
        Integer currentCount = meetingMemberRepository.countActiveMembers(meetingId);
        if (currentCount >= maxParticipants) {
            throw new MeetingException(MeetingErrorCode.MEETING_FULL);
        }
    }

    public MeetingMember getMeetingMember(Long meetingId, Long userId) {
        return meetingMemberRepository.findByMeetingIdAndUserId(meetingId, userId)
                .orElseThrow(() -> new MeetingException(MeetingErrorCode.NOT_MEETING_MEMBER));
    }

    public MeetingMember getAnyMeetingMember(Long meetingId, Long userId) {
        return meetingMemberRepository.findAnyByMeetingIdAndUserId(meetingId, userId)
                .orElseThrow(() -> new MeetingException(MeetingErrorCode.NOT_MEETING_MEMBER));
    }
}

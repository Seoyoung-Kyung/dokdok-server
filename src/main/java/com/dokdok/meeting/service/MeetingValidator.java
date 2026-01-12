package com.dokdok.meeting.service;

import com.dokdok.meeting.entity.MeetingMember;
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

    public void validateMemberInGathering(Long meetingId, Long gatheringId) {
        boolean isMemberInGathering = meetingRepository
                .existsByIdAndGatheringId(meetingId, gatheringId);

        if (!isMemberInGathering) {
            throw new MeetingException(MeetingErrorCode.NOT_GATHERING_MEETING);
        }
    }

    public MeetingMember getMeetingMember(Long meetingId, Long userId) {
        return meetingMemberRepository.findByMeetingIdAndUserId(meetingId, userId)
                .orElseThrow(() -> new MeetingException(MeetingErrorCode.NOT_MEETING_MEMBER));
    }
}

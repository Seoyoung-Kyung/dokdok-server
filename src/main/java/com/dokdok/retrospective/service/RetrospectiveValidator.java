package com.dokdok.retrospective.service;

import com.dokdok.gathering.exception.GatheringErrorCode;
import com.dokdok.gathering.exception.GatheringException;
import com.dokdok.gathering.repository.GatheringMemberRepository;
import com.dokdok.meeting.exception.MeetingErrorCode;
import com.dokdok.meeting.exception.MeetingException;
import com.dokdok.meeting.repository.MeetingMemberRepository;
import com.dokdok.meeting.repository.MeetingRepository;
import com.dokdok.retrospective.exception.RetrospectiveErrorCode;
import com.dokdok.retrospective.exception.RetrospectiveException;
import com.dokdok.retrospective.repository.PersonalRetrospectiveRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RetrospectiveValidator {

    private final PersonalRetrospectiveRepository personalRetrospectiveRepository;
    private final GatheringMemberRepository gatheringMemberRepository;
    private final MeetingMemberRepository meetingMemberRepository;

    public void validateRetrospective(Long meetingId, Long userId){
        boolean exists = personalRetrospectiveRepository.existsByMeetingIdAndUserId(meetingId, userId);

        if(exists) {
            throw new RetrospectiveException(RetrospectiveErrorCode.RETROSPECTIVE_ALREADY_EXISTS);
        }
    }

    public void validateMeetingRetrospectiveAccess(Long gatheringId, Long meetingId, Long userId){
        gatheringMemberRepository.findByGatheringIdAndUserId(gatheringId,userId)
                .orElseThrow(() -> new GatheringException(GatheringErrorCode.NOT_GATHERING_MEMBER));

        meetingMemberRepository.findByMeetingIdAndUserId(meetingId, userId)
                .orElseThrow(() -> new MeetingException(MeetingErrorCode.NOT_GATHERING_MEETING));
    public void validateRetrospective(Long retrospectiveId){
        boolean exists = personalRetrospectiveRepository.existsById(retrospectiveId);

        if(!exists) {
            throw new RetrospectiveException(RetrospectiveErrorCode.RETROSPECTIVE_NOT_FOUND);
        }
    }

}

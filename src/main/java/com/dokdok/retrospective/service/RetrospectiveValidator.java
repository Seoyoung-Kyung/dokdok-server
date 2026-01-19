package com.dokdok.retrospective.service;

import com.dokdok.gathering.exception.GatheringErrorCode;
import com.dokdok.gathering.exception.GatheringException;
import com.dokdok.gathering.repository.GatheringMemberRepository;
import com.dokdok.gathering.service.GatheringValidator;
import com.dokdok.meeting.exception.MeetingErrorCode;
import com.dokdok.meeting.exception.MeetingException;
import com.dokdok.meeting.repository.MeetingMemberRepository;
import com.dokdok.meeting.repository.MeetingRepository;
import com.dokdok.meeting.service.MeetingValidator;
import com.dokdok.retrospective.dto.response.PersonalRetrospectiveResponse;
import com.dokdok.retrospective.entity.PersonalMeetingRetrospective;
import com.dokdok.retrospective.exception.RetrospectiveErrorCode;
import com.dokdok.retrospective.exception.RetrospectiveException;
import com.dokdok.retrospective.repository.PersonalRetrospectiveRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RetrospectiveValidator {

    private final PersonalRetrospectiveRepository personalRetrospectiveRepository;
    private final GatheringValidator gatheringValidator;
    private final MeetingValidator meetingValidator;

    public void validateRetrospective(Long meetingId, Long userId){
        boolean exists = personalRetrospectiveRepository.existsByMeetingIdAndUserId(meetingId, userId);

        if(exists) {
            throw new RetrospectiveException(RetrospectiveErrorCode.RETROSPECTIVE_ALREADY_EXISTS);
        }
    }

    public void validateMeetingRetrospectiveAccess(Long gatheringId, Long meetingId, Long userId) {
        gatheringValidator.validateMembership(gatheringId, userId);

        meetingValidator.validateMeetingInGathering(meetingId, gatheringId);
    }

    public void validateRetrospective(Long retrospectiveId){
        boolean exists = personalRetrospectiveRepository.existsById(retrospectiveId);

        if(!exists) {
            throw new RetrospectiveException(RetrospectiveErrorCode.RETROSPECTIVE_NOT_FOUND);
        }
    }

    public PersonalMeetingRetrospective getRetrospective(Long retrospectiveId){

        return personalRetrospectiveRepository.findById(retrospectiveId)
                .orElseThrow(() -> new RetrospectiveException(RetrospectiveErrorCode.RETROSPECTIVE_NOT_FOUND));

    }

}

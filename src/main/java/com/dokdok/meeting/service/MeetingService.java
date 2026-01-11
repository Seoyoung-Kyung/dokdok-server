package com.dokdok.meeting.service;

import com.dokdok.global.exception.GlobalErrorCode;
import com.dokdok.global.exception.GlobalException;
import com.dokdok.meeting.repository.MeetingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MeetingService {

    private final MeetingRepository meetingRepository;

    public void validateMemberInGathering(Long meetingId, Long gatheringId) {
        boolean isMemberInGathering = meetingRepository
                .existsByIdAndGatheringId(meetingId, gatheringId);

        // TODO: 예의 코드 상의 후 수정 예정
        if (!isMemberInGathering) {
            throw new GlobalException(GlobalErrorCode.NOT_GATHERING_MEETING);
        }
    }
}

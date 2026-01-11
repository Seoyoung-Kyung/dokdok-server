package com.dokdok.meeting.service;

import com.dokdok.global.exception.GlobalErrorCode;
import com.dokdok.global.exception.GlobalException;
import com.dokdok.meeting.entity.MeetingMember;
import com.dokdok.meeting.repository.MeetingMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MeetingMemberService {

    private final MeetingMemberRepository meetingMemberRepository;

    // TODO: 예의 코드 상의 후 수정 예정
    public MeetingMember getMeetingMember(Long meetingId, Long userId) {
        return meetingMemberRepository.findByMeetingIdAndUserId(meetingId, userId)
                .orElseThrow(() -> new GlobalException(GlobalErrorCode.NOT_MEETING_MEMBER));
    }
}

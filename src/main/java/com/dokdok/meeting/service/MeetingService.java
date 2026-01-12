package com.dokdok.meeting.service;

import com.dokdok.meeting.dto.MeetingResponse;
import com.dokdok.meeting.entity.Meeting;
import com.dokdok.meeting.entity.MeetingMember;
import com.dokdok.meeting.exception.MeetingErrorCode;
import com.dokdok.meeting.exception.MeetingException;
import com.dokdok.meeting.repository.MeetingMemberRepository;
import com.dokdok.meeting.repository.MeetingRepository;
import com.dokdok.topic.entity.Topic;
import com.dokdok.topic.repository.TopicRepository;
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

    /**
     * 특정 약속의 정보를 확인할 수 있다.
     * @param meetingId 약속 식별자
     * @return 미팅 응답 정보
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
}

package com.dokdok.meeting.repository;

import com.dokdok.meeting.entity.Meeting;
import com.dokdok.meeting.entity.MeetingStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MeetingRepository extends JpaRepository<Meeting, Long> {
    boolean existsByGatheringIdAndMeetingStatus(Long gatheringId, MeetingStatus meetingStatus);
}

package com.dokdok.meeting.repository;

import com.dokdok.meeting.entity.Meeting;
import com.dokdok.meeting.entity.MeetingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface MeetingRepository extends JpaRepository<Meeting, Long> {
    boolean existsByIdAndGatheringId(Long meetingId, Long gatheringId);
  
    boolean existsByGatheringIdAndMeetingStatus(Long gatheringId, MeetingStatus meetingStatus);

    int countByGatheringIdAndMeetingStatus(Long gatheringId, MeetingStatus meetingStatus);
}

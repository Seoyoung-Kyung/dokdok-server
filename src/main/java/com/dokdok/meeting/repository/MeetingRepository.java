package com.dokdok.meeting.repository;

import com.dokdok.meeting.entity.Meeting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


import java.util.Optional;

@Repository
public interface MeetingRepository extends JpaRepository<Meeting, Long> {

    boolean existsByIdAndGatheringId(Long meetingId, Long gatheringId);
}
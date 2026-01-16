package com.dokdok.meeting.repository;

import com.dokdok.meeting.entity.Meeting;
import com.dokdok.meeting.entity.MeetingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface MeetingRepository extends JpaRepository<Meeting, Long> {
    boolean existsByIdAndGatheringId(Long meetingId, Long gatheringId);
  
    boolean existsByGatheringIdAndMeetingStatus(Long gatheringId, MeetingStatus meetingStatus);

    int countByGatheringIdAndMeetingStatus(Long gatheringId, MeetingStatus meetingStatus);

    @EntityGraph(attributePaths = {"book"})
    Page<Meeting> findByGatheringIdAndMeetingStatus(
            Long gatheringId,
            MeetingStatus meetingStatus,
            Pageable pageable
    );

    @EntityGraph(attributePaths = {"book"})
    Page<Meeting> findByGatheringIdAndMeetingStatusAndMeetingStartDateBetween(
            Long gatheringId,
            MeetingStatus meetingStatus,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable
    );

    @Query("""
            SELECT count(m)
            FROM Meeting m
            WHERE m.gathering.id = :gatheringId
            AND m.meetingStatus = :meetingStatus
            AND m.meetingStartDate BETWEEN :startDate AND :endDate
            """)
    int countUpcomingMeetings(
            @Param("gatheringId") Long gatheringId,
            @Param("meetingStatus") MeetingStatus meetingStatus,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );
}

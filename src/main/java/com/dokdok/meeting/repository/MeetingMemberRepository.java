package com.dokdok.meeting.repository;

import com.dokdok.meeting.entity.MeetingMember;
import com.dokdok.meeting.entity.MeetingStatus;
import com.dokdok.meeting.entity.Meeting;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MeetingMemberRepository extends JpaRepository<MeetingMember, Long> {

    List<MeetingMember> findAllByMeetingId(Long meetingId);

    @Query("""                                                                                                                                                           
      SELECT mm FROM MeetingMember mm
      JOIN FETCH mm.user u
      JOIN FETCH mm.meeting
      WHERE mm.meeting.id = :meetingId
      AND mm.user.id = :userId
      AND mm.canceledAt IS NULL
      """)
    Optional<MeetingMember> findByMeetingIdAndUserId(
            @Param("meetingId") Long meetingId,
            @Param("userId") Long userId
    );

    @Query("""
      SELECT mm FROM MeetingMember mm
      JOIN FETCH mm.user u
      JOIN FETCH mm.meeting
      WHERE mm.meeting.id = :meetingId
      AND mm.user.id = :userId
      """)
    Optional<MeetingMember> findAnyByMeetingIdAndUserId(
            @Param("meetingId") Long meetingId,
            @Param("userId") Long userId
    );

    @Query("""
      SELECT count(mm) FROM MeetingMember mm
      WHERE mm.meeting.id = :meetingId
      AND mm.canceledAt IS NULL
      """)
    int countActiveMembers(@Param("meetingId") Long meetingId);

    boolean existsByMeetingIdAndUserId(
            Long meetingId,
            Long userId
    );

    @Query("""
      SELECT mm.meeting.id FROM MeetingMember mm
      WHERE mm.user.id = :userId
      AND mm.canceledAt IS NULL
      AND mm.meeting.gathering.id = :gatheringId
      """)
    List<Long> findActiveMeetingIdsByUserIdAndGatheringId(
            @Param("userId") Long userId,
            @Param("gatheringId") Long gatheringId
    );

    @Query("""
      SELECT count(mm) FROM MeetingMember mm
      JOIN mm.meeting m
      WHERE mm.user.id = :userId
      AND mm.canceledAt IS NULL
      AND m.gathering.id = :gatheringId
      AND m.meetingStatus = :meetingStatus
      """)
    int countMeetingsByUserIdAndStatus(
            @Param("userId") Long userId,
            @Param("gatheringId") Long gatheringId,
            @Param("meetingStatus") MeetingStatus meetingStatus
    );

    @Query(
            value = """
              SELECT m FROM MeetingMember mm
              JOIN mm.meeting m
              JOIN FETCH m.book
              WHERE mm.user.id = :userId
              AND mm.canceledAt IS NULL
              AND m.gathering.id = :gatheringId
              AND m.meetingStatus = :meetingStatus
              """,
            countQuery = """
              SELECT count(mm) FROM MeetingMember mm
              JOIN mm.meeting m
              WHERE mm.user.id = :userId
              AND mm.canceledAt IS NULL
              AND m.gathering.id = :gatheringId
              AND m.meetingStatus = :meetingStatus
              """
    )

    Page<Meeting> findMeetingsByUserIdAndStatus(
            @Param("userId") Long userId,
            @Param("gatheringId") Long gatheringId,
            @Param("meetingStatus") MeetingStatus meetingStatus,
            Pageable pageable
    );

    @Query("""
                SELECT mm
                FROM MeetingMember mm
                JOIN FETCH mm.user u
                WHERE mm.meeting.id = :meetingId
                AND mm.user.id <> :userId
            """)
    List<MeetingMember> findOtherMembersByMeetingId(
            @Param("meetingId") Long meetingId,
            @Param("userId") Long userId
    );
}

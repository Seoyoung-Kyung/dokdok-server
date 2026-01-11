package com.dokdok.meeting.repository;

import com.dokdok.meeting.entity.MeetingMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MeetingMemberRepository extends JpaRepository<MeetingMember, Long> {

    @Query("""                                                                                                                                                           
      SELECT mm FROM MeetingMember mm
      JOIN FETCH mm.user u
      JOIN FETCH mm.meeting
      WHERE mm.meeting.id = :meetingId
      AND mm.user.id = :userId
      """)
    Optional<MeetingMember> findByMeetingIdAndUserId(
            @Param("meetingId") Long meetingId,
            @Param("userId") Long userId
    );
}
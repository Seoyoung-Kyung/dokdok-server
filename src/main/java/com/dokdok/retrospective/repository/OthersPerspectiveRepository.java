package com.dokdok.retrospective.repository;

import com.dokdok.retrospective.entity.RetrospectiveOthersPerspective;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OthersPerspectiveRepository extends JpaRepository<RetrospectiveOthersPerspective, Long> {

    @Query("""
            SELECT op
            FROM RetrospectiveOthersPerspective op
            JOIN FETCH op.topic t
            JOIN FETCH op.meetingMember mm
            WHERE op.personalMeetingRetrospective.id = :retrospectiveId
            """)
    List<RetrospectiveOthersPerspective> findByPersonalMeetingRetrospective(Long retrospectiveId);

    @Query("""
            SELECT op
            FROM RetrospectiveOthersPerspective op
            LEFT JOIN FETCH op.topic t
            JOIN FETCH op.meetingMember mm
            WHERE op.personalMeetingRetrospective.id IN :retrospectiveIds
            """)
    List<RetrospectiveOthersPerspective> findByRetrospectiveIds(List<Long> retrospectiveIds);
}
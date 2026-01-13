package com.dokdok.topic.repository;

import com.dokdok.topic.entity.Topic;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TopicRepository extends JpaRepository<Topic, Long> {

    List<Topic> findAllByMeetingId(Long meetingId);

    boolean existsByIdAndMeetingId(Long topicId, Long meetingId);

    @Modifying
    @Query("""
      UPDATE Topic t
      SET t.deletedAt = CURRENT_TIMESTAMP
      WHERE t.meeting.id = :meetingId
      AND t.proposedBy.id = :userId
      AND t.deletedAt IS NULL
      """)
    void softDeleteByMeetingIdAndProposedById(
            @Param("meetingId") Long meetingId,
            @Param("userId") Long userId
    );

    @Query("SELECT t " +
            "FROM Topic t " +
            "JOIN FETCH t.proposedBy " +
            "JOIN FETCH t.meeting " +
            "WHERE t.meeting.id = :meetingId " +
            "AND t.deletedAt IS NULL " +
            "ORDER BY t.likeCount DESC, t.id ASC")
    Page<Topic> findTopicsByMeetingId(
            @Param("meetingId") Long meetingId,
            Pageable pageable
    );
}

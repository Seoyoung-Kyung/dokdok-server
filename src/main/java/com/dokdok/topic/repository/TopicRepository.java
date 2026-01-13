package com.dokdok.topic.repository;

import com.dokdok.topic.entity.Topic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param;

import java.util.List;

@Repository
public interface TopicRepository extends JpaRepository<Topic, Long> {

    List<Topic> findAllByMeetingId(Long meetingId);

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
}

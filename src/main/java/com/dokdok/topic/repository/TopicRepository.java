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
import java.util.Optional;

@Repository
public interface TopicRepository extends JpaRepository<Topic, Long> {

    List<Topic> findAllByMeetingId(Long meetingId);

    List<Topic> findAllByIdInAndMeetingId(List<Long> topicIds, Long meetingId);

    @Query("SELECT t " +
            "FROM Topic t " +
            "LEFT JOIN FETCH t.meeting m " +
            "LEFT JOIN FETCH t.proposedBy u " +
            "WHERE t.id = :topicId")
    Optional<Topic> findDetailById(Long topicId);

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

    @Query("SELECT t " +
            "FROM Topic t " +
            "LEFT JOIN FETCH t.meeting m " +
            "LEFT JOIN FETCH m.gathering g " +
            "WHERE t.id = :topicId " +
            "AND (t.proposedBy.id = :userId " +
            "OR g.gatheringLeader.id = :userId " +
            "OR m.meetingLeader.id = :userId)")
    Optional<Topic> findTopicWithDeletePermission(
            @Param("topicId") Long topicId,
            @Param("userId") Long userId
    );

    @Modifying
    @Query("""
                UPDATE Topic t
                SET t.likeCount = t.likeCount + 1
                WHERE t.id = :topicId
            """)
    void increaseLikeCount(@Param("topicId") Long topicId);

    @Modifying
    @Query("""
                UPDATE Topic t
                SET t.likeCount = t.likeCount - 1
                WHERE t.id = :topicId
                  AND t.likeCount > 0
            """)
    void decreaseLikeCount(@Param("topicId") Long topicId);

    Optional<Topic> findByIdAndDeletedAtIsNull(Long topicId);
}
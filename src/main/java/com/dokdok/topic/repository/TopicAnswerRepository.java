package com.dokdok.topic.repository;

import com.dokdok.topic.entity.TopicAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.swing.*;
import java.util.List;
import java.util.Optional;

public interface TopicAnswerRepository extends JpaRepository<TopicAnswer, Long> {
    Optional<TopicAnswer> findByTopicIdAndUserId(Long topicId, Long userId);

    @Query("""
            SELECT ta
            FROM TopicAnswer ta
            WHERE ta.topic.id = :topicId
            AND ta.user.id = :userId
            """)
    TopicAnswer findPreOpinion(Long topicId, Long userId);

    boolean existsByTopicIdAndUserId(Long topicId, Long userId);

    @Query("""
                    SELECT ta
                    FROM TopicAnswer ta
                    JOIN FETCH ta.topic t
                    WHERE t.meeting.id = :meetingId
                    AND ta.user.id = :userId
            """)
    List<TopicAnswer> findByMeetingIdUserId(Long meetingId, Long userId);

    @Modifying
    @Query("""
            UPDATE TopicAnswer ta
            SET ta.deletedAt = CURRENT_TIMESTAMP
            WHERE ta.topic.id IN (
                SELECT t.id
                FROM Topic t
                WHERE t.meeting.id = :meetingId
            )
            AND ta.deletedAt IS NULL
            """)
    void softDeleteByMeetingId(@Param("meetingId") Long meetingId);
}

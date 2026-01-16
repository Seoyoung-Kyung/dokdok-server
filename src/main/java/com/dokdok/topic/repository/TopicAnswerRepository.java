package com.dokdok.topic.repository;

import com.dokdok.topic.entity.TopicAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

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
}

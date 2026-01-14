package com.dokdok.topic.repository;

import com.dokdok.topic.entity.TopicAnswer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TopicAnswerRepository extends JpaRepository<TopicAnswer, Long> {
    Optional<TopicAnswer> findByTopicIdAndUserId(Long topicId, Long userId);

    boolean existsByTopicIdAndUserId(Long topicId, Long userId);
}

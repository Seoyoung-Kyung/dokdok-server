package com.dokdok.topic.repository;

import com.dokdok.topic.entity.Topic;
import com.dokdok.topic.entity.TopicLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TopicLikeRepository extends JpaRepository<TopicLike, Long> {

    boolean existsByTopicId(Long topicId);

    void deleteByTopicIdAndUserId(Long topicId, Long id);
}

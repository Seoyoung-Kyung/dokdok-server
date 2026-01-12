package com.dokdok.topic.repository;

import com.dokdok.topic.entity.Topic;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TopicRepository extends JpaRepository<Topic, Long> {
    List<Topic> findAllByMeetingId(Long meetingId);
}

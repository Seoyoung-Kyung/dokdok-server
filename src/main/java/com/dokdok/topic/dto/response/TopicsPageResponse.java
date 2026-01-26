package com.dokdok.topic.dto.response;

import com.dokdok.global.response.CursorResponse;
import com.dokdok.topic.entity.Topic;

import java.util.List;
import java.util.Set;

/**
 * 주제 목록 페이지네이션 응답을 위한 헬퍼 클래스
 */
public class TopicsPageResponse {

    public static CursorResponse<TopicsResponse.TopicDto, TopicsCursor> from(
            List<Topic> topics,
            int pageSize,
            boolean hasNext,
            Set<Long> deletableTopicIds
    ) {
        List<TopicsResponse.TopicDto> topicDtos = topics.stream()
                .map(topic -> TopicsResponse.TopicDto.from(
                        topic,
                        deletableTopicIds.contains(topic.getId())
                ))
                .toList();

        TopicsCursor cursor = null;
        if (hasNext && !topics.isEmpty()) {
            Topic lastTopic = topics.get(topics.size() - 1);
            cursor = TopicsCursor.from(lastTopic);
        }

        return CursorResponse.of(topicDtos, pageSize, hasNext, cursor);
    }
}

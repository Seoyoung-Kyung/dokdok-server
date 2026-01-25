package com.dokdok.topic.dto.response;

import com.dokdok.topic.entity.Topic;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.List;
import java.util.Set;

@Builder
public record TopicsPageResponse(
        @Schema(description = "주제 목록")
        List<TopicsResponse.TopicDto> items,

        @Schema(description = "페이지 크기")
        Integer pageSize,

        @Schema(description = "다음 페이지 존재 여부")
        Boolean hasNext,

        @Schema(description = "다음 페이지 커서 (다음 페이지가 없으면 null)")
        NextCursor nextCursor
) {
    @Builder
    @Schema(description = "다음 페이지 조회를 위한 커서")
    public record NextCursor(
            @Schema(description = "마지막 항목의 좋아요 수 (정렬 기준)")
            Integer likeCount,

            @Schema(description = "마지막 항목의 주제 ID (동점 대비)")
            Long topicId
    ) {
        public static NextCursor from(Topic topic) {
            return NextCursor.builder()
                    .likeCount(topic.getLikeCount())
                    .topicId(topic.getId())
                    .build();
        }
    }

    public static TopicsPageResponse from(
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

        NextCursor cursor = null;
        if (hasNext && !topics.isEmpty()) {
            Topic lastTopic = topics.get(topics.size() - 1);
            cursor = NextCursor.from(lastTopic);
        }

        return TopicsPageResponse.builder()
                .items(topicDtos)
                .pageSize(pageSize)
                .hasNext(hasNext)
                .nextCursor(cursor)
                .build();
    }
}

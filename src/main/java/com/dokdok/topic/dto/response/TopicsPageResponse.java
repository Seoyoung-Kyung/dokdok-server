package com.dokdok.topic.dto.response;

import com.dokdok.topic.entity.Topic;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Set;

@Builder
public record TopicsPageResponse(
        @Schema(description = "주제 목록")
        List<TopicsResponse.TopicDto> topics,

        @Schema(description = "전체 주제 개수")
        Integer totalCount,

        @Schema(description = "현재 페이지 번호 (0부터 시작)")
        Integer currentPage,

        @Schema(description = "페이지 크기")
        Integer pageSize,

        @Schema(description = "전체 페이지 수")
        Integer totalPages
) {
    public static TopicsPageResponse from(Page<Topic> page, Set<Long> deletableTopicIds) {
        List<TopicsResponse.TopicDto> topicDtos = page.getContent().stream()
                .map(topic -> TopicsResponse.TopicDto.from(
                        topic,
                        deletableTopicIds.contains(topic.getId())
                ))
                .toList();

        return TopicsPageResponse.builder()
                .topics(topicDtos)
                .totalCount((int) page.getTotalElements())
                .currentPage(page.getNumber())
                .pageSize(page.getSize())
                .totalPages(page.getTotalPages())
                .build();
    }
}

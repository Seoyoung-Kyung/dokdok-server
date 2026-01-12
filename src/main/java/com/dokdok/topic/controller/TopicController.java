package com.dokdok.topic.controller;

import com.dokdok.global.response.ApiResponse;
import com.dokdok.topic.api.TopicApi;
import com.dokdok.topic.dto.request.SuggestTopicRequest;
import com.dokdok.topic.dto.response.SuggestTopicResponse;
import com.dokdok.topic.dto.response.TopicsPageResponse;
import com.dokdok.topic.service.TopicService;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/gatherings/{gatheringId}/meetings/{meetingId}")
public class TopicController implements TopicApi {

    private final TopicService topicService;

    @Override
    public ResponseEntity<ApiResponse<SuggestTopicResponse>> createTopic(
            Long gatheringId,
            Long meetingId,
            SuggestTopicRequest request
    ) {
        SuggestTopicResponse response = topicService.createTopic(gatheringId, meetingId, request);

        return ApiResponse.created(response, "주제 제안이 완료되었습니다.");
    }

    @Override
    @GetMapping(value = "/topics")
    public ResponseEntity<ApiResponse<TopicsPageResponse>> getTopics(
            Long gatheringId,
            Long meetingId,
            @ParameterObject
            @PageableDefault(size = 10) Pageable pageable
    ) {

        TopicsPageResponse response = topicService.getTopics(gatheringId, meetingId, pageable);

        return ApiResponse.success(response, "제안된 주제 조회를 완료했습니다.");
    }

}
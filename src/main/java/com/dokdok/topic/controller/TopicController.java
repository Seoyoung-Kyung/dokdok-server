package com.dokdok.topic.controller;

import com.dokdok.global.response.ApiResponse;
import com.dokdok.topic.api.TopicApi;
import com.dokdok.topic.dto.request.SuggestTopicRequest;
import com.dokdok.topic.dto.response.SuggestTopicResponse;
import com.dokdok.topic.service.TopicService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class TopicController implements TopicApi {

    private final TopicService topicService;

    @Override
    public ResponseEntity<ApiResponse<SuggestTopicResponse>> createTopic(
            Long gatheringId,
            Long meetingId,
            Long userId,
            SuggestTopicRequest request
    ) {
        SuggestTopicResponse response = topicService.createTopic(gatheringId, meetingId, userId, request);

        return ApiResponse.created(response, "주제 제안이 완료되었습니다.");
    }

}
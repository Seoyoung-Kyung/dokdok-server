package com.dokdok.topic.controller;

import com.dokdok.global.response.ApiResponse;
import com.dokdok.topic.api.TopicAnswerApi;
import com.dokdok.topic.dto.request.TopicAnswerRequest;
import com.dokdok.topic.dto.response.TopicAnswerResponse;
import com.dokdok.topic.service.TopicAnswerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class TopicAnswerController implements TopicAnswerApi {

    private final TopicAnswerService topicAnswerService;

    @Override
    public ResponseEntity<ApiResponse<TopicAnswerResponse>> createAnswer(
            Long gatheringId,
            Long meetingId,
            Long topicId,
            TopicAnswerRequest request
    ) {
        // TODO: @AuthenticationPrincipal에서 userId 추출
        Long userId = 1L;

        TopicAnswerResponse response = topicAnswerService.createAnswer(
                gatheringId, meetingId, topicId, userId, request
        );

        return ApiResponse.created(response, "답변이 저장되었습니다.");
    }
}

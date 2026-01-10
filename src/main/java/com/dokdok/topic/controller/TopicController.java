package com.dokdok.topic.controller;

import com.dokdok.global.response.ApiResponse;
import com.dokdok.topic.dto.SuggestTopicRequest;
import com.dokdok.topic.dto.SuggestTopicResponse;
import com.dokdok.topic.service.TopicService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/gatherings/{gatheringId}/meetings/{meetingId}")
public class TopicController {

    private final TopicService topicService;

    @PostMapping("/topics")
    public ResponseEntity<ApiResponse<SuggestTopicResponse>> createTopic(
            @PathVariable Long gatheringId,
            @PathVariable Long meetingId,
            @RequestParam Long userId,
            @RequestBody @Valid SuggestTopicRequest request
    ) {
        SuggestTopicResponse response = topicService.createTopic(gatheringId, meetingId, userId, request);

        return ApiResponse.created(response, "주제 제안이 완료되었습니다.");
    }
}
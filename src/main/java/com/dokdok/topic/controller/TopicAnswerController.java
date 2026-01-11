package com.dokdok.topic.controller;

import com.dokdok.global.response.ApiResponse;
import com.dokdok.topic.dto.request.TopicAnswerRequest;
import com.dokdok.topic.dto.response.TopicAnswerResponse;
import com.dokdok.topic.service.TopicAnswerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/gatherings/{gathering_id}/meetings/{meeting_id}/topics/{topic_id}/answers")
public class TopicAnswerController {

    private final TopicAnswerService topicAnswerService;

    @PostMapping
    public ResponseEntity<ApiResponse<TopicAnswerResponse>> createAnswer(
            @PathVariable("gathering_id") Long gatheringId,
            @PathVariable("meeting_id") Long meetingId,
            @PathVariable("topic_id") Long topicId,
            @RequestBody @Valid TopicAnswerRequest request
    ) {
        // TODO: @AuthenticationPrincipal에서 userId 추출
        Long userId = 1L;

        TopicAnswerResponse response = topicAnswerService.createAnswer(
                gatheringId, meetingId, topicId, userId, request
        );

        return ApiResponse.created(response, "답변이 저장되었습니다.");
    }
}

package com.dokdok.topic.controller;

import com.dokdok.global.response.ApiResponse;
import com.dokdok.topic.api.ConfirmTopicApi;
import com.dokdok.topic.dto.response.ConfirmedTopicsResponse;
import com.dokdok.topic.service.TopicService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/gatherings/{gatheringId}/meetings/{meetingId}")
public class ConfirmTopicController implements ConfirmTopicApi {

    private final TopicService topicService;

    @Override
    public ResponseEntity<ApiResponse<ConfirmedTopicsResponse>> getConfirmedTopics(
            Long gatheringId,
            Long meetingId
    ) {
        ConfirmedTopicsResponse response = topicService.getConfirmedTopics(gatheringId, meetingId);
        return ApiResponse.success(response, "확정된 주제 조회를 완료했습니다.");
    }
}

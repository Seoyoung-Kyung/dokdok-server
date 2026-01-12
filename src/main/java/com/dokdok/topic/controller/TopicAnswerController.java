package com.dokdok.topic.controller;

import com.dokdok.global.response.ApiResponse;
import com.dokdok.global.util.SecurityUtil;
import com.dokdok.topic.api.TopicAnswerApi;
import com.dokdok.topic.dto.request.TopicAnswerRequest;
import com.dokdok.topic.dto.response.TopicAnswerDetailResponse;
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
        Long userId = SecurityUtil.getCurrentUserId();

        TopicAnswerResponse response = topicAnswerService.createAnswer(
                gatheringId, meetingId, topicId, userId, request
        );

        return ApiResponse.created(response, "답변이 저장되었습니다.");
    }

    @Override
    public ResponseEntity<ApiResponse<TopicAnswerDetailResponse>> findMyAnswer(
            Long gatheringId,
            Long meetingId,
            Long topicId
    ) {
        Long userId = SecurityUtil.getCurrentUserId();
        TopicAnswerDetailResponse response = topicAnswerService.getMyAnswer(
                gatheringId, meetingId, topicId, userId
        );

        return ApiResponse.success(response, "조회 성공");
    }

    @Override
    public ResponseEntity<ApiResponse<TopicAnswerResponse>> updateMyAnswer(
            Long gatheringId,
            Long meetingId,
            Long topicId,
            TopicAnswerRequest request
    ) {
        Long userId = SecurityUtil.getCurrentUserId();
        TopicAnswerResponse response = topicAnswerService.updateMyAnswer(
                gatheringId, meetingId, topicId, userId, request
        );

        return ApiResponse.updated(response, "답변이 수정되었습니다.");
    }
}

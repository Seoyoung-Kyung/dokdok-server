package com.dokdok.retrospective.controller;

import com.dokdok.global.response.ApiResponse;
import com.dokdok.retrospective.api.MeetingRetrospectiveApi;
import com.dokdok.retrospective.dto.request.MeetingRetrospectiveRequest;
import com.dokdok.retrospective.dto.response.MeetingRetrospectiveResponse;
import com.dokdok.retrospective.entity.MeetingRetrospective;
import com.dokdok.retrospective.service.MeetingRetrospectiveService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/meetings/{meetingId}/retrospectives")
public class MeetingRetrospectiveController implements MeetingRetrospectiveApi {

    private final MeetingRetrospectiveService meetingRetrospectiveService;

    @Override
    @GetMapping
    public ResponseEntity<ApiResponse<MeetingRetrospectiveResponse>> getMeetingRetrospective(@PathVariable Long meetingId) {
        MeetingRetrospectiveResponse response = meetingRetrospectiveService.getMeetingRetrospective(meetingId);

        return ApiResponse.success(response,"공동 회고 조회 성공");
    }

    @PostMapping
    public ResponseEntity<ApiResponse<MeetingRetrospectiveResponse.CommentResponse>> createMeetingRetrospective(
            @PathVariable Long meetingId,
            @Valid @RequestBody MeetingRetrospectiveRequest request
            ) {
        MeetingRetrospectiveResponse.CommentResponse response = meetingRetrospectiveService.createMeetingRetrospective(meetingId, request);

        return ApiResponse.created(response, "공동 회고 작성 완료");
    }
}

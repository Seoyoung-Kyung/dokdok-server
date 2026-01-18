package com.dokdok.retrospective.controller;

import com.dokdok.global.response.ApiResponse;
import com.dokdok.retrospective.api.MeetingRetrospectiveApi;
import com.dokdok.retrospective.dto.response.MeetingRetrospectiveResponse;
import com.dokdok.retrospective.service.MeetingRetrospectiveService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}

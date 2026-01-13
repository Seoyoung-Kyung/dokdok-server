package com.dokdok.meeting.controller;

import com.dokdok.global.response.ApiResponse;
import com.dokdok.meeting.api.MeetingApi;
import com.dokdok.meeting.dto.MeetingCreateRequest;
import com.dokdok.meeting.dto.MeetingResponse;
import com.dokdok.meeting.dto.MeetingStatusResponse;
import com.dokdok.meeting.entity.MeetingStatus;
import com.dokdok.meeting.service.MeetingService;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/meetings")
public class MeetingController implements MeetingApi {

    private final MeetingService meetingService;

    @Override
    @GetMapping(value = "/{meetingId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<MeetingResponse>> findMeeting(
            @PathVariable Long meetingId
    ) {
        return ApiResponse.success(meetingService.findMeeting(meetingId), "약속 상세 조회에 성공했습니다.");
    }

    @Override
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<MeetingResponse>> createMeeting(
            @Valid @RequestBody MeetingCreateRequest request
    ) {
        // todo : 시큐리티 인증 정보에서 userId를 가져오도록 변경
        return ApiResponse.created(meetingService.createMeeting(request, 1L), "약속 생성 요청에 성공했습니다.");
    }

    @Override
    @PatchMapping(value = "/{meetingId}/status", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<MeetingStatusResponse>> changeMeetingStatus(
            @PathVariable Long meetingId,
            @RequestParam MeetingStatus meetingStatus
    ) {
        MeetingStatusResponse response = meetingService.changeMeetingStatus(meetingId, meetingStatus);
        return ApiResponse.updated(response, "약속 상태 변경에 성공했습니다.");
    }

    @Override
    @PostMapping(value = "/{meetingId}/join", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<Long>> joinMeeting(
            @PathVariable Long meetingId
    ) {
        Long response = meetingService.joinMeeting(meetingId);
        return ApiResponse.success(response, "약속 참가 신청에 성공했습니다.");
    }
}

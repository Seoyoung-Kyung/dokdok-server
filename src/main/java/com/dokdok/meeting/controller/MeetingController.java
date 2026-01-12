package com.dokdok.meeting.controller;

import com.dokdok.global.response.ApiResponse;
import com.dokdok.meeting.api.MeetingApi;
import com.dokdok.meeting.dto.MeetingCreateRequest;
import com.dokdok.meeting.dto.MeetingResponse;
import com.dokdok.meeting.service.MeetingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class MeetingController implements MeetingApi {

    private final MeetingService meetingService;

    @Override
    public ResponseEntity<ApiResponse<MeetingResponse>> findMeeting(Long meetingId) {
        return ApiResponse.success(meetingService.findMeeting(meetingId), "약속 상세 조회에 성공했습니다.");
    }

    @Override
    public ResponseEntity<ApiResponse<MeetingResponse>> createMeeting(MeetingCreateRequest request) {
        // todo : 시큐리티 인증 정보에서 userId를 가져오도록 변경
        return ApiResponse.created(meetingService.createMeeting(request, 1L), "약속 생성 요청에 성공했습니다.");
    }
}

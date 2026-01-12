package com.dokdok.meeting.controller;

import com.dokdok.global.response.ApiResponse;
import com.dokdok.meeting.api.MeetingApi;
import com.dokdok.meeting.dto.MeetingResponse;
import com.dokdok.meeting.service.MeetingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/meetings")
public class MeetingController implements MeetingApi {

    private final MeetingService meetingService;

    @Override
    @GetMapping("/{meetingId}")
    public ResponseEntity<ApiResponse<MeetingResponse>> findMeeting(@PathVariable Long meetingId) {
        return ApiResponse.success(meetingService.findMeeting(meetingId));
    }
}

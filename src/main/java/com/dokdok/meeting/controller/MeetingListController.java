package com.dokdok.meeting.controller;

import com.dokdok.global.response.ApiResponse;
import com.dokdok.meeting.api.MeetingListApi;
import com.dokdok.meeting.dto.MeetingListFilter;
import com.dokdok.meeting.dto.MeetingListRequest;
import com.dokdok.meeting.dto.MeetingListResponse;
import com.dokdok.meeting.service.MeetingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/gatherings/{gatheringId}/meetings")
public class MeetingListController implements MeetingListApi {

    private final MeetingService meetingService;

    @Override
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<MeetingListResponse>> getMeetingList(
            @PathVariable Long gatheringId,
            @RequestParam MeetingListFilter filter,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size
    ) {
        MeetingListRequest request = MeetingListRequest.builder()
                .filter(filter)
                .page(page)
                .size(size)
                .build();

        MeetingListResponse response = meetingService.meetingList(gatheringId, request);
        return ApiResponse.success(response, "약속 리스트 조회에 성공했습니다.");
    }
}

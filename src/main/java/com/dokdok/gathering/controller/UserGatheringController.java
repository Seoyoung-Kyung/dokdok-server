package com.dokdok.gathering.controller;

import com.dokdok.gathering.api.UserGatheringApi;
import com.dokdok.gathering.dto.MyGatheringListResponse;
import com.dokdok.gathering.service.GatheringService;
import com.dokdok.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@Slf4j
@RestController
@RequiredArgsConstructor
public class UserGatheringController implements UserGatheringApi {

    private final GatheringService gatheringService;

    @Override
    public ResponseEntity<ApiResponse<MyGatheringListResponse>> getUserGatherings(
            @PathVariable Long userId,
            @PageableDefault(size = 10, sort = "joinedAt", direction = Sort.Direction.DESC)
            Pageable pageable
            ){
        log.info("GET /api/users/{}/gatherings - page = {}, size = {}",
                userId, pageable.getPageNumber(), pageable.getPageSize());

        MyGatheringListResponse response = gatheringService.getMyGatherings(userId, pageable);

        return ApiResponse.success(response,"모임 리스트 조회 성공");
    }
}

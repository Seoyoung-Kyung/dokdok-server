package com.dokdok.gathering.controller;

import com.dokdok.gathering.api.GatheringApi;
import com.dokdok.gathering.dto.GatheringDetailResponse;
import com.dokdok.gathering.service.GatheringService;
import com.dokdok.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class GatheringController implements GatheringApi {

    private final GatheringService gatheringService;

    @Override
    public ResponseEntity<ApiResponse<GatheringDetailResponse>> getGatheringDetail(
            @PathVariable Long gatheringId,
            @RequestParam Long userId   // TODO : 추후 Spring Security 적용 시 @AuthenticationPrincipal로 변경
    ){
        GatheringDetailResponse response = gatheringService.getGatheringDetail(gatheringId,userId);

        return ApiResponse.success(response,"모임 상세정보 조회 성공");
    }
}

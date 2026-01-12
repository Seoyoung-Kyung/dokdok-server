package com.dokdok.gathering.controller;

import com.dokdok.gathering.api.GatheringApi;
import com.dokdok.gathering.dto.GatheringDetailResponse;
import com.dokdok.gathering.dto.GatheringUpdateRequest;
import com.dokdok.gathering.dto.GatheringUpdateResponse;
import com.dokdok.gathering.dto.MyGatheringListResponse;
import com.dokdok.gathering.service.GatheringService;
import com.dokdok.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/gatherings")
@RequiredArgsConstructor
public class GatheringController implements GatheringApi {

    private final GatheringService gatheringService;

    @Override
    public ResponseEntity<ApiResponse<MyGatheringListResponse>> getMyGatherings(Pageable pageable) {
        MyGatheringListResponse response = gatheringService.getMyGatherings(pageable);

        return ApiResponse.success(response, "모임 리스트 조회 성공");
    }

    @Override
    public ResponseEntity<ApiResponse<GatheringDetailResponse>> getGatheringDetail(@PathVariable Long gatheringId){
        GatheringDetailResponse response = gatheringService.getGatheringDetail(gatheringId);

        return ApiResponse.success(response,"모임 상세정보 조회 성공");
    }

    @Override
    public ResponseEntity<ApiResponse<GatheringUpdateResponse>> updateGathering(
            @PathVariable Long gatheringId,
            @Valid @RequestBody GatheringUpdateRequest request
    ) {
        GatheringUpdateResponse response = gatheringService.updateGathering(gatheringId, request);
        return ApiResponse.updated(response, "모임 정보 수정 성공");
    }
}

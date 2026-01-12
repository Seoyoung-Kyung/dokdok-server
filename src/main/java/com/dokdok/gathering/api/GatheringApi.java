package com.dokdok.gathering.api;

import com.dokdok.gathering.dto.GatheringDetailResponse;
import com.dokdok.global.response.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "Gathering", description = "모임 관련 API")
@RequestMapping("/api/gatherings")
public interface GatheringApi {

    @Operation(
        summary = "모임 상세 정보 조회",
        description = "특정 모임의 상세 정보를 조회합니다. 해당 모임의 멤버만 조회할 수 있습니다."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음 - 모임 멤버가 아님"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "모임을 찾을 수 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/{gatheringId}")
    ResponseEntity<ApiResponse<GatheringDetailResponse>> getGatheringDetail(
        @Parameter(description = "조회할 모임의 ID", required = true, example = "1")
        @PathVariable Long gatheringId,

        @Parameter(description = "현재 사용자 ID (추후 Spring Security로 대체 예정)", required = true, example = "1")
        @RequestParam Long userId
    );
}

package com.dokdok.gathering.api;


import com.dokdok.gathering.dto.MyGatheringListResponse;
import com.dokdok.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "User Gathering", description = "사용자의 모임 관련 API")
@RequestMapping("/api/users/{userId}/gatherings")
public interface UserGatheringApi {
    @Operation(
            summary = "내 모임 목록 조회",
            description = "현재 사용자가 속한 모임 목록을 페이징하여 조회합니다. 기본 정렬은 가입일 최신순입니다."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping
    ResponseEntity<ApiResponse<MyGatheringListResponse>> getUserGatherings(
            @Parameter(description = "조회할 사용자의 ID", required = true, example = "1")
            @PathVariable Long userId,

            @Parameter(
                description = "페이징 정보 (page, size, sort)",
                example = "page=0&size=10&sort=joinedAt,desc",
                schema = @Schema(implementation = Pageable.class)
            )
            @PageableDefault(size = 10, sort = "joinedAt", direction = Sort.Direction.DESC)
            Pageable pageable
    );
}

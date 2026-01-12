package com.dokdok.gathering.api;

import com.dokdok.gathering.dto.GatheringDetailResponse;
import com.dokdok.gathering.dto.MyGatheringListResponse;
import com.dokdok.global.response.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "모임", description = "모임 관련 API")
@RequestMapping("/api/gatherings")
public interface GatheringApi {

    @Operation(
        summary = "모임 상세 정보 조회",
        description = """
              현재 로그인한 사용자가 속한 모임 목록을 조회합니다.
              - 가입일 최신순으로 정렬됩니다.
              - 페이징을 지원합니다 (기본 10개씩).
              - 삭제되지 않은 활성 모임만 조회됩니다.
              """
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MyGatheringListResponse.class)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "인증 실패 - 로그인이 필요합니다."
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "서버 오류"
            )
    })
    @GetMapping
    ResponseEntity<ApiResponse<MyGatheringListResponse>> getMyGatherings(
            @ParameterObject
            @Parameter(
                    description = "페이징 정보 (page: 페이지 번호, size: 페이지 크기, sort: 정렬 기준)",
                    example = "page=0&size=10&sort=joinedAt,desc"
            )
            @PageableDefault(size = 10, sort = "joinedAt", direction = org.springframework.data.domain.Sort.Direction.DESC)
            Pageable pageable
    );
    @Operation(
            summary = "내 모임 리스트조회",
            description = """
              내가 가입한 모임의 정보를 조회합니다.
              - 모임에 가입한 모임만 조회할 수 있습니다.
              """
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = GatheringDetailResponse.class)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "인증 실패 - 로그인이 필요합니다."
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "권한 없음 - 모임 멤버만 조회할 수 있습니다."
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "모임을 찾을 수 없음"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "서버 오류"
            )
    })
    @GetMapping("/{gatheringId}")
    ResponseEntity<ApiResponse<GatheringDetailResponse>> getGatheringDetail(
            @Parameter(
                    description = "조회할 모임 ID",
                    required = true,
                    example = "1"
            )
            @PathVariable Long gatheringId
    );
}

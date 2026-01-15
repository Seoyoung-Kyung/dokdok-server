package com.dokdok.meeting.api;

import com.dokdok.global.response.ApiResponse;
import com.dokdok.meeting.dto.MeetingListFilter;
import com.dokdok.meeting.dto.MeetingListResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "약속 관리", description = "약속 관련 API")
public interface MeetingListApi {

    @Operation(
            summary = "모임 약속 리스트 조회",
            description = """
            모임 내 약속 리스트를 조회합니다.
            - 전체: 확정된 약속
            - 다가오는 약속: 3일 이내 시작하는 확정된 약속
            - 완료된 약속: 종료된 약속
            - 내가 참여한 약속: 완료된 약속 중 참여한 약속
            """,
            parameters = {
                    @Parameter(name = "gatheringId", description = "모임 식별자", in = ParameterIn.PATH, required = true),
                    @Parameter(name = "filter", description = "탭 필터 (ALL, UPCOMING, DONE, JOINED)",
                            in = ParameterIn.QUERY, required = true),
                    @Parameter(name = "page", description = "페이지 번호 (0부터 시작)", in = ParameterIn.QUERY),
                    @Parameter(name = "size", description = "페이지 크기", in = ParameterIn.QUERY)
            }
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "약속 리스트 조회 성공",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = MeetingListResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "모임 멤버가 아님"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "모임을 찾을 수 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류")
    })
    ResponseEntity<ApiResponse<MeetingListResponse>> getMeetingList(
            @PathVariable Long gatheringId,
            @RequestParam MeetingListFilter filter,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size
    );
}

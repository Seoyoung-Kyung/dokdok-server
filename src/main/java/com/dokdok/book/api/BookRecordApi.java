package com.dokdok.book.api;

import com.dokdok.global.response.ApiResponse;
import com.dokdok.retrospective.dto.response.RetrospectiveRecordsPageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;

@Tag(name = "독서 기록", description = "책별 독서 기록 관련 API")
public interface BookRecordApi {

    @Operation(
            summary = "책별 개인 회고 목록 조회",
            description = "특정 책에 대해 사용자가 작성한 개인 회고 목록을 커서 기반 페이지네이션으로 조회합니다.",
            parameters = {
                    @Parameter(name = "bookId", description = "책 식별자", in = ParameterIn.PATH, required = true),
                    @Parameter(name = "pageSize", description = "페이지 크기 (기본값: 10)", in = ParameterIn.QUERY, required = false),
                    @Parameter(name = "cursorCreatedAt", description = "커서 - 마지막 항목의 생성 시간 (ISO 8601 형식)", in = ParameterIn.QUERY, required = false),
                    @Parameter(name = "cursorRetrospectiveId", description = "커서 - 마지막 항목의 회고 ID", in = ParameterIn.QUERY, required = false)
            }
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "개인 회고 목록 조회 성공",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = RetrospectiveRecordsPageResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "책을 찾을 수 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping(value = "/retrospectives", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<ApiResponse<RetrospectiveRecordsPageResponse>> getRetrospectiveRecords(
            @PathVariable Long bookId,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime cursorCreatedAt,
            @RequestParam(required = false) Long cursorRetrospectiveId
    );
}

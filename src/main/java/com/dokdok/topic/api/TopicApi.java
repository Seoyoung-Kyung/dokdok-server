package com.dokdok.topic.api;

import com.dokdok.global.response.ApiResponse;
import com.dokdok.topic.dto.request.SuggestTopicRequest;
import com.dokdok.topic.dto.response.SuggestTopicResponse;
import com.dokdok.topic.dto.response.TopicsPageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "주제 관리", description = "주제 관련 API")
public interface TopicApi {

    @Operation(
            summary = "주제 제안",
            description = "약속에 대한 주제를 제안합니다.",
            parameters = {
                    @Parameter(name = "gatheringId", description = "모임 식별자", in = ParameterIn.PATH, required = true),
                    @Parameter(name = "meetingId", description = "약속 식별자", in = ParameterIn.PATH, required = true)
            }
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "주제 제안 성공",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = SuggestTopicResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "모임 또는 약속의 멤버가 아님"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "모임 또는 약속을 찾을 수 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류")
    })
    ResponseEntity<ApiResponse<SuggestTopicResponse>> createTopic(
            @PathVariable Long gatheringId,
            @PathVariable Long meetingId,
            @Valid @RequestBody SuggestTopicRequest request
    );

    @Operation(
            summary = "제안된 주제 조회 (페이지네이션)",
            description = """
                    약속에 제안된 주제 목록을 페이지네이션으로 조회합니다.
                    - 정렬: likeCount(투표 수) 내림차순, topicId 오름차순
                    - page: 페이지 번호 (0부터 시작, 기본값: 0)
                    - size: 페이지 크기 (기본값: 10)
                    """,
            parameters = {
                    @Parameter(name = "gatheringId", description = "모임 식별자", in = ParameterIn.PATH, required = true),
                    @Parameter(name = "meetingId", description = "약속 식별자", in = ParameterIn.PATH, required = true),
                    @Parameter(name = "page", description = "페이지 번호 (0부터 시작)", in = ParameterIn.QUERY)
            }
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "주제 조회 성공",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = TopicsPageResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "모임 또는 약속의 멤버가 아님"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "모임 또는 약속을 찾을 수 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류")
    })
    ResponseEntity<ApiResponse<TopicsPageResponse>> getTopics(
            @PathVariable Long gatheringId,
            @PathVariable Long meetingId,
            @ParameterObject
            @PageableDefault(size = 10) Pageable pageable
    );
}

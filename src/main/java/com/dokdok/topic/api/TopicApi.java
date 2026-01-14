package com.dokdok.topic.api;

import com.dokdok.global.response.ApiResponse;
import com.dokdok.topic.dto.request.ConfirmTopicsRequest;
import com.dokdok.topic.dto.request.SuggestTopicRequest;
import com.dokdok.topic.dto.response.ConfirmTopicsResponse;
import com.dokdok.topic.dto.response.SuggestTopicResponse;
import com.dokdok.topic.dto.response.TopicLikeResponse;
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
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
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

    @Operation(
            summary = "제안된 주제 확정",
            description = "약속에서 제안된 주제를 확정합니다.",
            parameters = {
                    @Parameter(name = "gatheringId", description = "모임 식별자", in = ParameterIn.PATH, required = true),
                    @Parameter(name = "meetingId", description = "약속 식별자", in = ParameterIn.PATH, required = true)
            }
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "주제 확정 성공",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ConfirmTopicsResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "모임 또는 약속의 멤버가 아님"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "모임 또는 약속 또는 주제를 찾을 수 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PatchMapping(value = "/topics/confirm", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<ApiResponse<ConfirmTopicsResponse>> confirmTopics(
            @PathVariable Long gatheringId,
            @PathVariable Long meetingId,
            @Valid @RequestBody ConfirmTopicsRequest request
    );

    @Operation(
            summary = "주제 삭제",
            description = "제안된 주제를 삭제합니다.",
            parameters = {
                    @Parameter(name = "gatheringId", description = "모임 식별자", in = ParameterIn.PATH, required = true),
                    @Parameter(name = "meetingId", description = "약속 식별자", in = ParameterIn.PATH, required = true),
                    @Parameter(name = "topicId", description = "주제 식별자", in = ParameterIn.PATH, required = true)
            }
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "204",
                    description = "주제 삭제 성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "주제 삭제 권한 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "모임, 약속 또는 주제를 찾을 수 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @DeleteMapping(value = "/{topicId}")
    ResponseEntity<ApiResponse<Void>> deleteTopic(
            @PathVariable Long gatheringId,
            @PathVariable Long meetingId,
            @PathVariable Long topicId
    );

    @Operation(
            summary = "주제 좋아요 토글",
            description = "주제에 대한 좋아요를 추가하거나 취소합니다. 이미 좋아요한 주제를 다시 요청하면 좋아요가 취소됩니다.",
            parameters = {
                    @Parameter(name = "gatheringId", description = "모임 식별자", in = ParameterIn.PATH, required = true),
                    @Parameter(name = "meetingId", description = "약속 식별자", in = ParameterIn.PATH, required = true),
                    @Parameter(name = "topicId", description = "주제 식별자", in = ParameterIn.PATH, required = true)
            }
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "좋아요 토글 성공",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = TopicLikeResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "모임 또는 약속의 멤버가 아님"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "모임, 약속 또는 주제를 찾을 수 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PostMapping("/{topicId}/likes")
    ResponseEntity<ApiResponse<TopicLikeResponse>> toggleLike(
            @PathVariable Long gatheringId,
            @PathVariable Long meetingId,
            @PathVariable Long topicId
    );
}

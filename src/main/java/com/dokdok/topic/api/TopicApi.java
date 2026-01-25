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
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = SuggestTopicResponse.class)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "약속 상태로 인해 주제 제안 불가",
                    content = @Content(
                            examples = @ExampleObject(
                                    value = "{\"code\":\"M007\",\"message\":\"약속이 확정된 경우에는 주제를 제안할 수 없습니다.\"}"
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "모임 또는 약속 멤버가 아님",
                    content = @Content(
                            examples = {
                                    @ExampleObject(value = "{\"code\":\"G002\",\"message\":\"모임의 멤버가 아닙니다.\"}"),
                                    @ExampleObject(value = "{\"code\":\"M004\",\"message\":\"약속의 멤버가 아닙니다.\"}")
                            }
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "모임 또는 약속을 찾을 수 없음",
                    content = @Content(
                            examples = {
                                    @ExampleObject(value = "{\"code\":\"G001\",\"message\":\"모임을 찾을 수 없습니다.\"}"),
                                    @ExampleObject(value = "{\"code\":\"M001\",\"message\":\"약속을 찾을 수 없습니다.\"}")
                            }
                    )
            )
    })
    @PostMapping
    ResponseEntity<ApiResponse<SuggestTopicResponse>> createTopic(
            @PathVariable Long gatheringId,
            @PathVariable Long meetingId,
            @Valid @RequestBody SuggestTopicRequest request
    );

    @Operation(
            summary = "제안된 주제 조회 (커서 기반 페이지네이션)",
            description = """
                    약속에 제안된 주제 목록을 커서 기반 페이지네이션으로 조회합니다.
                    - 1차 정렬: 좋아요 수 내림차순
                    - 2차 정렬: topicId 오름차순
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "주제 조회 성공",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = TopicsPageResponse.class)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "약속 멤버가 아님",
                    content = @Content(
                            examples = @ExampleObject(
                                    value = "{\"code\":\"M004\",\"message\":\"약속의 멤버가 아닙니다.\"}"
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "약속을 찾을 수 없음",
                    content = @Content(
                            examples = @ExampleObject(
                                    value = "{\"code\":\"M001\",\"message\":\"약속을 찾을 수 없습니다.\"}"
                            )
                    )
            )
    })
    @GetMapping
    ResponseEntity<ApiResponse<TopicsPageResponse>> getTopics(
            @PathVariable Long gatheringId,
            @PathVariable Long meetingId,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer cursorLikeCount,
            @RequestParam(required = false) Long cursorTopicId
    );

    @Operation(
            summary = "제안된 주제 확정",
            description = "약속에서 제안된 주제를 확정합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "주제 확정 성공",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ConfirmTopicsResponse.class)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "약속장이 아님",
                    content = @Content(
                            examples = @ExampleObject(
                                    value = "{\"code\":\"M006\",\"message\":\"약속장만 수정할 수 있습니다.\"}"
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "주제 또는 약속을 찾을 수 없음",
                    content = @Content(
                            examples = {
                                    @ExampleObject(value = "{\"code\":\"E101\",\"message\":\"주제를 찾을 수 없습니다.\"}"),
                                    @ExampleObject(value = "{\"code\":\"M001\",\"message\":\"약속을 찾을 수 없습니다.\"}")
                            }
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "이미 삭제된 주제",
                    content = @Content(
                            examples = @ExampleObject(
                                    value = "{\"code\":\"E106\",\"message\":\"이미 삭제된 주제입니다.\"}"
                            )
                    )
            )
    })
    @PatchMapping(value = "/topics/confirm", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<ApiResponse<ConfirmTopicsResponse>> confirmTopics(
            @PathVariable Long gatheringId,
            @PathVariable Long meetingId,
            @Valid @RequestBody ConfirmTopicsRequest request
    );

    @Operation(
            summary = "주제 삭제",
            description = "제안된 주제를 삭제합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "204",
                    description = "주제 삭제 성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "주제 삭제 권한 없음",
                    content = @Content(
                            examples = @ExampleObject(
                                    value = "{\"code\":\"E105\",\"message\":\"사용자에게 주제 삭제 권한이 없습니다.\"}"
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "주제를 찾을 수 없음",
                    content = @Content(
                            examples = @ExampleObject(
                                    value = "{\"code\":\"E101\",\"message\":\"주제를 찾을 수 없습니다.\"}"
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "이미 삭제된 주제",
                    content = @Content(
                            examples = @ExampleObject(
                                    value = "{\"code\":\"E106\",\"message\":\"이미 삭제된 주제입니다.\"}"
                            )
                    )
            )
    })
    @DeleteMapping("/{topicId}")
    ResponseEntity<ApiResponse<Void>> deleteTopic(
            @PathVariable Long gatheringId,
            @PathVariable Long meetingId,
            @PathVariable Long topicId
    );

    @Operation(
            summary = "주제 좋아요 토글",
            description = "주제에 대한 좋아요를 추가하거나 취소합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "좋아요 토글 성공",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = TopicLikeResponse.class)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "약속 멤버가 아님",
                    content = @Content(
                            examples = @ExampleObject(
                                    value = "{\"code\":\"M004\",\"message\":\"약속의 멤버가 아닙니다.\"}"
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "주제를 찾을 수 없음",
                    content = @Content(
                            examples = @ExampleObject(
                                    value = "{\"code\":\"E101\",\"message\":\"주제를 찾을 수 없습니다.\"}"
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "이미 삭제된 주제",
                    content = @Content(
                            examples = @ExampleObject(
                                    value = "{\"code\":\"E106\",\"message\":\"이미 삭제된 주제입니다.\"}"
                            )
                    )
            )
    })
    @PostMapping("/{topicId}/likes")
    ResponseEntity<ApiResponse<TopicLikeResponse>> toggleLike(
            @PathVariable Long gatheringId,
            @PathVariable Long meetingId,
            @PathVariable Long topicId
    );
}
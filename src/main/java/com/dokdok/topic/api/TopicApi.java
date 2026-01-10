package com.dokdok.topic.api;

import com.dokdok.global.response.ApiResponse;
import com.dokdok.topic.dto.SuggestTopicRequest;
import com.dokdok.topic.dto.SuggestTopicResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "Topic", description = "주제 제안 API")
public interface TopicApi {

    @Operation(
            summary = "주제 제안",
            description = "모임의 특정 미팅에 새로운 주제를 제안합니다. 제안자는 해당 모임의 멤버여야 합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "주제 제안 성공",
                    content = @Content(schema = @Schema(implementation = SuggestTopicResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 (필수 필드 누락, 유효하지 않은 값)"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "해당 모임의 멤버가 아님"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "모임, 미팅 또는 사용자를 찾을 수 없음"
            )
    })
    ResponseEntity<ApiResponse<SuggestTopicResponse>> createTopic(
            @Parameter(description = "모임 ID", required = true, example = "1")
            @PathVariable @Positive Long gatheringId,

            @Parameter(description = "미팅 ID", required = true, example = "1")
            @PathVariable @Positive Long meetingId,

            @Parameter(description = "사용자 ID (추후 인증 토큰으로 대체 예정)", required = true, example = "1")
            @RequestParam @Positive Long userId,

            @Parameter(description = "주제 제안 요청", required = true)
            @RequestBody @Valid SuggestTopicRequest request
    );
}
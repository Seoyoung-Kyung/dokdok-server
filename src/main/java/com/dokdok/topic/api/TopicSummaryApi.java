package com.dokdok.topic.api;

import com.dokdok.global.response.ApiResponse;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "토픽 요약", description = "토픽 요약 관련 API")
@RequestMapping("/api/gatherings/{gathering_id}/meetings/{meeting_id}/topics/{topic_id}/summaries")
public interface TopicSummaryApi {

    @Operation(
            summary = "토픽 요약 요청 (AI 테스트)",
            description = "AI 서버로 토픽 답변 요약 요청을 전달합니다.",
            parameters = {
                    @Parameter(name = "gathering_id", description = "모임 식별자", in = ParameterIn.PATH, required = true),
                    @Parameter(name = "meeting_id", description = "약속 식별자", in = ParameterIn.PATH, required = true),
                    @Parameter(name = "topic_id", description = "토픽 식별자", in = ParameterIn.PATH, required = true)
            }
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "토픽 요약 요청 성공",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = Object.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "모임 또는 약속의 멤버가 아님"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "모임, 약속 또는 토픽을 찾을 수 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PostMapping(value = "/ai-test", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<ApiResponse<String>> requestTopicSummary(
            @PathVariable("gathering_id") Long gatheringId,
            @PathVariable("meeting_id") Long meetingId,
            @PathVariable("topic_id") Long topicId
    );
}

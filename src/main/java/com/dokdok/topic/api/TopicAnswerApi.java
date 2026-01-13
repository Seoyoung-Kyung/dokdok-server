package com.dokdok.topic.api;

import com.dokdok.global.response.ApiResponse;
import com.dokdok.topic.dto.request.TopicAnswerRequest;
import com.dokdok.topic.dto.response.TopicAnswerDetailResponse;
import com.dokdok.topic.dto.response.TopicAnswerResponse;
import com.dokdok.topic.dto.response.TopicAnswerSubmitResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "토픽 답변", description = "토픽 답변 관련 API")
@RequestMapping("/api/gatherings/{gathering_id}/meetings/{meeting_id}/topics/{topic_id}/answers")
public interface TopicAnswerApi {

    @Operation(
            summary = "토픽 답변 저장",
            description = "토픽 답변을 저장합니다.",
            parameters = {
                    @Parameter(name = "gathering_id", description = "모임 식별자", in = ParameterIn.PATH, required = true),
                    @Parameter(name = "meeting_id", description = "약속 식별자", in = ParameterIn.PATH, required = true),
                    @Parameter(name = "topic_id", description = "토픽 식별자", in = ParameterIn.PATH, required = true)
            }
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "토픽 답변 저장 성공",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = TopicAnswerResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "토픽 또는 사용자를 찾을 수 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<ApiResponse<TopicAnswerResponse>> createAnswer(
            @PathVariable("gathering_id") Long gatheringId,
            @PathVariable("meeting_id") Long meetingId,
            @PathVariable("topic_id") Long topicId,
            @Valid @RequestBody TopicAnswerRequest request
    );

    @Operation(
            summary = "내 토픽 답변 조회",
            description = "현재 로그인 사용자의 토픽 답변을 조회합니다.",
            parameters = {
                    @Parameter(name = "gathering_id", description = "모임 식별자", in = ParameterIn.PATH, required = true),
                    @Parameter(name = "meeting_id", description = "약속 식별자", in = ParameterIn.PATH, required = true),
                    @Parameter(name = "topic_id", description = "토픽 식별자", in = ParameterIn.PATH, required = true)
            }
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "내 토픽 답변 조회 성공",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = TopicAnswerDetailResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "답변을 찾을 수 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping(value = "/me", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<ApiResponse<TopicAnswerDetailResponse>> findMyAnswer(
            @PathVariable("gathering_id") Long gatheringId,
            @PathVariable("meeting_id") Long meetingId,
            @PathVariable("topic_id") Long topicId
    );

    @Operation(
            summary = "내 토픽 답변 수정",
            description = "현재 로그인 사용자의 토픽 답변을 수정합니다.",
            parameters = {
                    @Parameter(name = "gathering_id", description = "모임 식별자", in = ParameterIn.PATH, required = true),
                    @Parameter(name = "meeting_id", description = "약속 식별자", in = ParameterIn.PATH, required = true),
                    @Parameter(name = "topic_id", description = "토픽 식별자", in = ParameterIn.PATH, required = true)
            }
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "내 토픽 답변 수정 성공",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = TopicAnswerResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "답변을 찾을 수 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PatchMapping(value = "/me", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<ApiResponse<TopicAnswerResponse>> updateMyAnswer(
            @PathVariable("gathering_id") Long gatheringId,
            @PathVariable("meeting_id") Long meetingId,
            @PathVariable("topic_id") Long topicId,
            @Valid @RequestBody TopicAnswerRequest request
    );

    @Operation(
            summary = "내 토픽 답변 제출",
            description = "현재 로그인 사용자의 토픽 답변을 제출합니다.",
            parameters = {
                    @Parameter(name = "gathering_id", description = "모임 식별자", in = ParameterIn.PATH, required = true),
                    @Parameter(name = "meeting_id", description = "약속 식별자", in = ParameterIn.PATH, required = true),
                    @Parameter(name = "topic_id", description = "토픽 식별자", in = ParameterIn.PATH, required = true)
            }
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "내 토픽 답변 제출 성공",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = TopicAnswerSubmitResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "답변을 찾을 수 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "이미 제출된 답변"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PatchMapping(value = "/submit", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<ApiResponse<TopicAnswerSubmitResponse>> submitMyAnswer(
            @PathVariable("gathering_id") Long gatheringId,
            @PathVariable("meeting_id") Long meetingId,
            @PathVariable("topic_id") Long topicId
    );
}

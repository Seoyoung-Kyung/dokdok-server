package com.dokdok.topic.api;

import com.dokdok.global.response.ApiResponse;
import com.dokdok.topic.dto.response.ConfirmedTopicsResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Tag(name = "주제 관리", description = "주제 관련 API")
public interface ConfirmTopicApi {

    @Operation(
            summary = "확정된 주제 조회 (developer: 양재웅)",
            description = "약속에서 확정된 주제 목록을 조회합니다.",
            parameters = {
                    @Parameter(name = "gatheringId", description = "모임 식별자", in = ParameterIn.PATH, required = true),
                    @Parameter(name = "meetingId", description = "약속 식별자", in = ParameterIn.PATH, required = true)
            }
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "확정 주제 조회 성공",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ConfirmedTopicsResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(
                                    value = "{\"code\":\"G002\",\"message\":\"입력값이 올바르지 않습니다.\",\"data\":null}"
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "접근 권한 없음",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = {
                                    @ExampleObject(
                                            name = "모임 멤버 아님",
                                            description = "모임의 멤버가 아닌 경우",
                                            value = "{\"code\":\"GA002\",\"message\":\"모임의 멤버가 아닙니다.\",\"data\":null}"
                                    ),
                                    @ExampleObject(
                                            name = "모임에 속한 약속 아님",
                                            description = "약속이 해당 모임에 속하지 않는 경우",
                                            value = "{\"code\":\"M003\",\"message\":\"모임에 속한 약속이 아닙니다.\",\"data\":null}"
                                    )
                            }
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "서버 오류",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(
                                    value = "{\"code\":\"E000\",\"message\":\"서버 내부 오류가 발생했습니다.\",\"data\":null}"
                            )
                    )
            )
    })
    @GetMapping(value = "/confirm-topics", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<ApiResponse<ConfirmedTopicsResponse>> getConfirmedTopics(
            @PathVariable Long gatheringId,
            @PathVariable Long meetingId
    );
}

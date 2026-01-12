package com.dokdok.user.api;

import com.dokdok.global.response.ApiResponse;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "사용자", description = "사용자 관련 API")
@RequestMapping("/api/users")
public interface UserApi {

    @Operation(
            summary = "닉네임 중복 확인",
            description = "닉네임 사용 가능 여부를 확인합니다. 유효성 검사(null, 길이, 형식) 및 중복 여부를 검증합니다.",
            parameters = {
                    @Parameter(
                            name = "nickname",
                            description = "확인할 닉네임 (2~20자, 한글/영문/숫자만 허용)",
                            in = ParameterIn.QUERY,
                            required = true,
                            example = "테스트닉네임"
                    )
            }
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "사용 가능한 닉네임",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(
                                    value = "{\"code\":\"SUCCESS\",\"message\":\"사용 가능한 닉네임입니다.\"}"
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 (유효성 검사 실패)",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = {
                                    @ExampleObject(
                                            name = "닉네임 필수",
                                            description = "닉네임이 null이거나 빈 문자열인 경우",
                                            value = "{\"code\":\"U003\",\"message\":\"닉네임은 필수 입력 항목입니다.\"}"
                                    ),
                                    @ExampleObject(
                                            name = "닉네임 길이 오류",
                                            description = "닉네임이 2자 미만 또는 20자 초과인 경우",
                                            value = "{\"code\":\"U004\",\"message\":\"닉네임은 2자 이상 20자 이하로 입력해주세요.\"}"
                                    ),
                                    @ExampleObject(
                                            name = "닉네임 형식 오류",
                                            description = "특수문자, 공백, 이모지 등이 포함된 경우",
                                            value = "{\"code\":\"U005\",\"message\":\"닉네임은 한글, 영문, 숫자만 사용 가능합니다.\"}"
                                    )
                            }
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "닉네임 중복",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(
                                    value = "{\"code\":\"U002\",\"message\":\"이미 존재하는 사용자 닉네임입니다.\"}"
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "서버 오류"
            )
    })
    @GetMapping(value = "/check-nickname", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<ApiResponse<Void>> checkNickname(
            @RequestParam("nickname") String nickname
    );
}
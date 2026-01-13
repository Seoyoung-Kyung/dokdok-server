package com.dokdok.user.api;

import com.dokdok.global.response.ApiResponse;
import com.dokdok.user.dto.request.OnboardRequest;
import com.dokdok.user.dto.request.UpdateUserInfoRequest;
import com.dokdok.user.dto.response.UserDetailResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "사용자", description = "사용자 관련 API")
@RequestMapping("/api/users")
public interface UserApi {

    @Operation(
            summary = "사용자 온보딩",
            description = "신규 사용자의 닉네임을 설정합니다. 인증된 사용자만 접근 가능하며, 닉네임 유효성 검사 및 중복 확인 후 SecurityContext를 업데이트합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "온보딩 완료",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(
                                    value = "{\"code\":\"SUCCESS\",\"message\":\"온보딩 완료\"}"
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
                    responseCode = "401",
                    description = "인증되지 않은 사용자",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(
                                    value = "{\"code\":\"G001\",\"message\":\"인증되지 않은 사용자입니다.\"}"
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "사용자를 찾을 수 없음",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(
                                    value = "{\"code\":\"U001\",\"message\":\"존재하지 않는 사용자입니디.\"}"
                            )
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
    @PatchMapping(value = "/onboarding", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<ApiResponse<Void>> onboard(
            @Valid @RequestBody OnboardRequest request
    );

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

    @Operation(
            summary = "현재 사용자 정보 조회",
            description = "현재 인증된 사용자의 프로필 정보를 조회합니다. SecurityContext에서 사용자 정보를 가져옵니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "프로필 조회 성공",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = UserDetailResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "code": "SUCCESS",
                                              "message": "프로필 조회 성공",
                                              "data": {
                                                "userId": 1,
                                                "nickname": "테스트닉네임",
                                                "email": "test@example.com",
                                                "profileImageUrl": "https://example.com/profile.jpg",
                                                "createdAt": "2024-01-13T10:30:00"
                                              }
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "인증되지 않은 사용자",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(
                                    value = "{\"code\":\"G001\",\"message\":\"인증되지 않은 사용자입니다.\"}"
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "서버 오류"
            )
    })
    @GetMapping(value = "/me", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<ApiResponse<UserDetailResponse>> getCurrentUser();

    @Operation(
            summary = "사용자 정보 수정",
            description = "현재 사용자의 프로필 정보를 수정합니다. 현재는 닉네임만 수정 가능하며, 유효성 검사 및 중복 확인을 수행합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "프로필 수정 성공",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = UserDetailResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "code": "SUCCESS",
                                              "message": "프로필 수정 성공",
                                              "data": {
                                                "userId": 1,
                                                "nickname": "변경된닉네임",
                                                "email": "test@example.com",
                                                "profileImageUrl": "https://example.com/profile.jpg",
                                                "createdAt": "2024-01-13T10:30:00"
                                              }
                                            }
                                            """
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
                    responseCode = "401",
                    description = "인증되지 않은 사용자",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(
                                    value = "{\"code\":\"G001\",\"message\":\"인증되지 않은 사용자입니다.\"}"
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "사용자를 찾을 수 없음",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(
                                    value = "{\"code\":\"U001\",\"message\":\"존재하지 않는 사용자입니디.\"}"
                            )
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
    @PatchMapping(value = "/me", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<ApiResponse<UserDetailResponse>> updateUserInfo(
            @Valid @RequestBody UpdateUserInfoRequest request
    );

    @Operation(
            summary = "회원 탈퇴",
            description = "현재 인증된 사용자를 소프트 삭제합니다. 처리 후 인증 세션이 해제됩니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "회원 탈퇴 성공",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(
                                    value = "{\"code\":\"DELETED\",\"message\":\"회원 탈퇴가 완료되었습니다.\"}"
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "인증되지 않은 사용자",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(
                                    value = "{\"code\":\"G102\",\"message\":\"인증이 필요합니다.\"}"
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "사용자를 찾을 수 없음",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(
                                    value = "{\"code\":\"U001\",\"message\":\"존재하지 않는 사용자입니디.\"}"
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "서버 오류"
            )
    })
    @DeleteMapping(value = "/me", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<ApiResponse<Void>> deleteCurrentUser(
            @Parameter(hidden = true) HttpServletRequest request
    );
}

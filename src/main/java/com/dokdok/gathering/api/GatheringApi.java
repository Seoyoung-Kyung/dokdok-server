package com.dokdok.gathering.api;


import com.dokdok.gathering.dto.request.GatheringCreateRequest;
import com.dokdok.gathering.dto.request.GatheringUpdateRequest;
import com.dokdok.gathering.dto.response.*;
import com.dokdok.global.response.ApiResponse;
import com.dokdok.global.response.CursorResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import com.dokdok.gathering.dto.request.JoinGatheringMemberRequest;
import io.swagger.v3.oas.annotations.media.ExampleObject;

import java.time.LocalDateTime;

@Tag(name = "모임", description = "모임 관련 API")
@RequestMapping("/api/gatherings")
public interface GatheringApi {

    @Operation(
            summary = "모임 생성",
            description = """
              신규 모임을 생성하고 초대 코드를 발급합니다.
              - 요청한 사용자가 모임장이 되며 초기 멤버로 추가됩니다.
              - 초대 코드는 고유하게 생성됩니다.
              """
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "생성 성공",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = GatheringCreateResponse.class)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "인증 실패 - 로그인이 필요합니다.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                                    {"code": "G102", "message": "인증이 필요합니다.", "data": null}
                                    """)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "초대 코드 중복 등으로 인한 생성 실패",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                                    {"code": "GA007", "message": "초대 코드 생성에 실패했습니다. 다시 시도해주세요.", "data": null}
                                    """)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "서버 오류",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                                    {"code": "E000", "message": "서버 에러가 발생했습니다. 담당자에게 문의 바랍니다.", "data": null}
                                    """)
                    )
            )
    })
    @PostMapping
    ResponseEntity<ApiResponse<GatheringCreateResponse>> createGathering(
            @Parameter(description = "모임 생성 요청", required = true)
            @Valid @RequestBody GatheringCreateRequest request
    );

    @Operation(
            summary = "초대링크로 모임 정보 조회",
            description = """
              초대링크를 통해 모임의 기본 정보를 조회합니다.
              - 모임 가입 전 모임 정보를 미리 확인할 수 있습니다.
              - 로그인 없이도 조회 가능합니다.
              """
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = GatheringCreateResponse.class)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 - 초대링크가 비어있음",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                                    {"code": "GA010", "message": "초대링크는 필수입니다.", "data": null}
                                    """)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "모임을 찾을 수 없음 - 유효하지 않은 초대링크",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                                    {"code": "GA001", "message": "모임을 찾을 수 없습니다.", "data": null}
                                    """)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "서버 오류",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                                    {"code": "E000", "message": "서버 에러가 발생했습니다. 담당자에게 문의 바랍니다.", "data": null}
                                    """)
                    )
            )
    })
    @GetMapping("/join-request/{invitationLink}")
    ResponseEntity<ApiResponse<GatheringCreateResponse>> joinGatheringInfo(
            @Parameter(
                    description = "모임 초대링크",
                    required = true,
                    example = "ABC123XYZ"
            )
            @PathVariable @NotBlank(message = "초대링크는 필수입니다") String invitationLink
    );

    @Operation(
            summary = "모임 가입 요청",
            description = """
              초대링크를 통해 모임에 가입을 요청합니다.
              - 가입 요청 후 모임장의 승인을 기다려야 합니다.
              - 이미 가입된 모임이거나 가입 요청 중인 경우 실패합니다.
              """
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "가입 요청 성공",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = GatheringJoinResponse.class)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 - 초대링크가 비어있음",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                                    {"code": "GA010", "message": "초대링크는 필수입니다.", "data": null}
                                    """)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "인증 실패 - 로그인이 필요합니다.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                                    {"code": "G102", "message": "인증이 필요합니다.", "data": null}
                                    """)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "모임을 찾을 수 없음 - 유효하지 않은 초대링크",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                                    {"code": "GA001", "message": "모임을 찾을 수 없습니다.", "data": null}
                                    """)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "이미 가입된 모임이거나 가입 요청 중",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = {
                                    @ExampleObject(
                                            name = "이미 가입된 모임",
                                            value = """
                                                    {"code": "GA008", "message": "이미 가입된 모임입니다.", "data": null}
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "가입 요청 진행 중",
                                            value = """
                                                    {"code": "GA009", "message": "이미 가입 요청이 진행 중입니다.", "data": null}
                                                    """
                                    )
                            }
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "서버 오류",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                                    {"code": "E000", "message": "서버 에러가 발생했습니다. 담당자에게 문의 바랍니다.", "data": null}
                                    """)
                    )
            )
    })
    @PostMapping("/join-request/{invitationLink}")
    ResponseEntity<ApiResponse<GatheringJoinResponse>> joinGathering(
            @Parameter(
                    description = "모임 초대링크",
                    required = true,
                    example = "ABC123XYZ"
            )
            @PathVariable @NotBlank(message = "초대링크는 필수입니다") String invitationLink
    );
    @Operation(
            summary = "내 모임 전체 목록 조회",
            description = """                                                                                                                                                            
            현재 로그인한 사용자가 속한 모임 전체 목록을 조회합니다.
            - 커서 기반 무한 스크롤을 지원합니다.                                                                                                                                  
            - 가입일 최신순으로 정렬됩니다.                                                                                                                                        
            - 첫 페이지: cursorJoinedAt, cursorId 없이 호출                                                                                                                        
            - 다음 페이지: 응답의 nextCursor 값을 파라미터로 전달                                                                                                                  
            """
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CursorResponse.class)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "인증 실패 - 로그인이 필요합니다.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                                    {"code": "G102", "message": "인증이 필요합니다.", "data": null}
                                    """)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "서버 오류",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                                    {"code": "E000", "message": "서버 에러가 발생했습니다. 담당자에게 문의 바랍니다.", "data": null}
                                    """)
                    )
            )
    })
    @GetMapping
    ResponseEntity<ApiResponse<CursorResponse<GatheringListItemResponse, MyGatheringCursor>>> getMyGatherings(
            @Parameter(description = "페이지 크기", example = "10")
            @RequestParam(defaultValue = "10") int pageSize,

            @Parameter(description = "커서 - 마지막 항목의 가입일시 (ISO 8601)", example = "2026-01-22T10:25:40")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime cursorJoinedAt,

            @Parameter(description = "커서 - 마지막 항목의 ID", example = "127")
            @RequestParam(required = false) Long cursorId
    );

    @Operation(
        summary = "즐겨찾기 모임 목록 조회",
        description = """
              현재 로그인한 사용자가 즐겨찾기 한 모임 목록을 조회합니다.
              - 가입일 최신순으로 정렬됩니다.
              - 최대 4개까지 조회됩니다.
              """
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = FavoriteGatheringListResponse.class)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "인증 실패 - 로그인이 필요합니다.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                                    {"code": "G102", "message": "인증이 필요합니다.", "data": null}
                                    """)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "서버 오류",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                                    {"code": "E000", "message": "서버 에러가 발생했습니다. 담당자에게 문의 바랍니다.", "data": null}
                                    """)
                    )
            )
    })
    @GetMapping("/favorites")
    ResponseEntity<ApiResponse<FavoriteGatheringListResponse>> getFavoriteGatherings();

    @Operation(
            summary = "특정 모임 상세 조회",
            description = """
              내가 가입한 모임의 상세 정보를 조회합니다.
              - 모임에 가입한 모임만 조회할 수 있습니다.
              """
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = GatheringDetailResponse.class)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "인증 실패 - 로그인이 필요합니다.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                                    {"code": "G102", "message": "인증이 필요합니다.", "data": null}
                                    """)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "권한 없음 - 모임 멤버만 조회할 수 있습니다.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                                    {"code": "GA002", "message": "모임의 멤버가 아닙니다.", "data": null}
                                    """)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "모임을 찾을 수 없음",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                                    {"code": "GA001", "message": "모임을 찾을 수 없습니다.", "data": null}
                                    """)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "서버 오류",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                                    {"code": "E000", "message": "서버 에러가 발생했습니다. 담당자에게 문의 바랍니다.", "data": null}
                                    """)
                    )
            )
    })
    @GetMapping("/{gatheringId}")
    ResponseEntity<ApiResponse<GatheringDetailResponse>> getGatheringDetail(
            @Parameter(
                    description = "조회할 모임 ID",
                    required = true,
                    example = "1"
            )
            @PathVariable Long gatheringId
    );

    @Operation(
            summary = "모임 정보 수정",
            description = """
              모임의 기본 정보(모임명, 설명)를 수정합니다.
              - 모임의 리더만 수정할 수 있습니다.
              - 모임명은 필수이며, 공백만 포함할 수 없고 최대 12자까지 가능합니다.
              - 설명은 선택사항이며, 최대 150자까지 가능합니다.
              """
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "수정 성공",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = GatheringUpdateResponse.class)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 - 유효성 검증 실패 (모임명이 필수이거나 공백만 포함, 또는 12자 초과)",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                                    {"code": "G002", "message": "입력값이 올바르지 않습니다.", "data": null}
                                    """)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "인증 실패 - 로그인이 필요합니다.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                                    {"code": "G102", "message": "인증이 필요합니다.", "data": null}
                                    """)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "권한 없음 - 모임의 리더만 수정할 수 있습니다.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                                    {"code": "GA003", "message": "리더만 가능한 작업입니다.", "data": null}
                                    """)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "모임을 찾을 수 없음",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                                    {"code": "GA001", "message": "모임을 찾을 수 없습니다.", "data": null}
                                    """)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "서버 오류",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                                    {"code": "E000", "message": "서버 에러가 발생했습니다. 담당자에게 문의 바랍니다.", "data": null}
                                    """)
                    )
            )
    })
    @PatchMapping("/{gatheringId}")
    ResponseEntity<ApiResponse<GatheringUpdateResponse>> updateGathering(
            @Parameter(
                    description = "수정할 모임 ID",
                    required = true,
                    example = "123"
            )
            @PathVariable Long gatheringId,

            @Parameter(
                    description = "수정할 모임 정보",
                    required = true
            )
            @Valid @RequestBody GatheringUpdateRequest request
    );

    @Operation(
            summary = "모임 삭제",
            description = """
              모임을 삭제(Soft Delete)합니다.
              - 모임의 리더만 삭제할 수 있습니다.
              - 삭제된 모임은 조회되지 않습니다.
              """
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "삭제 성공",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "인증 실패 - 로그인이 필요합니다.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                                    {"code": "G102", "message": "인증이 필요합니다.", "data": null}
                                    """)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "권한 없음 - 모임의 리더만 삭제할 수 있습니다.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                                    {"code": "GA003", "message": "리더만 가능한 작업입니다.", "data": null}
                                    """)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "모임을 찾을 수 없음",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                                    {"code": "GA001", "message": "모임을 찾을 수 없습니다.", "data": null}
                                    """)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "서버 오류",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                                    {"code": "E000", "message": "서버 에러가 발생했습니다. 담당자에게 문의 바랍니다.", "data": null}
                                    """)
                    )
            )
    })
    @DeleteMapping("/{gatheringId}")
    ResponseEntity<ApiResponse<Void>> deleteGathering(
            @Parameter(
                    description = "삭제할 모임 ID",
                    required = true,
                    example = "123"
            )
            @PathVariable Long gatheringId
    );

    @Operation(
            summary = "모임원 강퇴",
            description = """
            모임원을 강퇴합니다.
            - 모임의 리더만 강퇴할 수 있습니다.
            - 리더는 강퇴할 수 없습니다.
            """
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "강퇴 성공",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "인증 실패 - 로그인이 필요합니다.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                                    {"code": "G102", "message": "인증이 필요합니다.", "data": null}
                                    """)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "권한 없음 - 모임의 리더만 강퇴할 수 있습니다.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = {
                                    @ExampleObject(
                                            name = "리더 권한 없음",
                                            value = """
                                                    {"code": "GA003", "message": "리더만 가능한 작업입니다.", "data": null}
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "리더 강퇴 불가",
                                            value = """
                                                    {"code": "GA005", "message": "유일한 리더는 강퇴할 수 없습니다.", "data": null}
                                                    """
                                    )
                            }
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "모임 또는 멤버를 찾을 수 없음",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                                    {"code": "GA001", "message": "모임을 찾을 수 없습니다.", "data": null}
                                    """)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "서버 오류",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                                    {"code": "E000", "message": "서버 에러가 발생했습니다. 담당자에게 문의 바랍니다.", "data": null}
                                    """)
                    )
            )
    })
    @DeleteMapping("/{gatheringId}/members/{userId}")
    ResponseEntity<ApiResponse<Void>> removeMember(
            @Parameter(description = "모임 ID", required = true, example = "123")
            @PathVariable Long gatheringId,
            @Parameter(description = "강퇴할 유저 ID", required = true, example = "456")
            @PathVariable Long userId
    );

    @Operation(
            summary = "모임 즐겨찾기 토글",
            description = """
              모임의 즐겨찾기 상태를 변경합니다.
              - 모임 멤버만 설정할 수 있습니다.
              """
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "즐겨찾기 상태 변경 성공",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "즐겨찾기 제한 초과",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                                    {"code": "GA013", "message": "즐겨찾기는 최대 4개까지만 등록할 수 있습니다.", "data": null}
                                    """)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "인증 실패 - 로그인이 필요합니다.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                                    {"code": "G102", "message": "인증이 필요합니다.", "data": null}
                                    """)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "권한 없음 - 모임 멤버만 설정할 수 있습니다.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                                    {"code": "GA002", "message": "모임의 멤버가 아닙니다.", "data": null}
                                    """)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "모임을 찾을 수 없음",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                                    {"code": "GA001", "message": "모임을 찾을 수 없습니다.", "data": null}
                                    """)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "서버 오류",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                                    {"code": "E000", "message": "서버 에러가 발생했습니다. 담당자에게 문의 바랍니다.", "data": null}
                                    """)
                    )
            )
    })
    @PatchMapping("/{gatheringId}/favorites")
    ResponseEntity<ApiResponse<Void>> updateFavorite(
            @Parameter(description = "모임 ID", required = true, example = "123")
            @PathVariable Long gatheringId
    );

    @Operation(
            summary = "가입 요청 승인/거절",
            description = """
                모임장이 가입 요청한 멤버를 승인하거나 거절합니다.
                - 모임장만 처리할 수 있습니다.
                - PENDING 상태의 멤버만 처리할 수 있습니다.
                - approve_type은 ACTIVE(승인) 또는 REJECTED(거절)만 허용됩니다.
                - 승인 시 joinedAt이 현재 시간으로 설정됩니다.
                """
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "처리 성공",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 - approve_type이 PENDING이거나, 대상 멤버가 PENDING 상태가 아님",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = {
                                    @ExampleObject(
                                            name = "잘못된 approve_type",
                                            value = """
                                                    {"code": "GA012", "message": "승인 상태는 ACTIVE 또는 REJECTED만 가능합니다.", "data": null}
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "PENDING 상태 아님",
                                            value = """
                                                    {"code": "GA011", "message": "대기 중인 가입 요청만 처리할 수 있습니다.", "data": null}
                                                    """
                                    )
                            }
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "인증 실패 - 로그인이 필요합니다.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                                    {"code": "G102", "message": "인증이 필요합니다.", "data": null}
                                    """)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "권한 없음 - 모임장만 가입 요청을 처리할 수 있습니다.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                                    {"code": "GA003", "message": "리더만 가능한 작업입니다.", "data": null}
                                    """)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "모임 또는 멤버를 찾을 수 없음",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                                    {"code": "GA001", "message": "모임을 찾을 수 없습니다.", "data": null}
                                    """)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "서버 오류",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                                    {"code": "E000", "message": "서버 에러가 발생했습니다. 담당자에게 문의 바랍니다.", "data": null}
                                    """)
                    )
            )
    })
    @PatchMapping("/{gatheringId}/join-requests/{memberId}")
    ResponseEntity<ApiResponse<Void>> handleJoinRequest(
            @Parameter(description = "모임 ID", required = true, example = "1")
            @PathVariable("gatheringId") Long gatheringId,
            @Parameter(description = "처리할 멤버의 유저 ID", required = true, example = "5")
            @PathVariable("memberId") Long memberId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "승인/거절 요청",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = JoinGatheringMemberRequest.class),
                            examples = {
                                    @ExampleObject(
                                            name = "승인 요청",
                                            value = """
                                                {
                                                    "approve_type": "ACTIVE"
                                                }
                                                """
                                    ),
                                    @ExampleObject(
                                            name = "거절 요청",
                                            value = """
                                                {
                                                    "approve_type": "REJECTED"
                                                }
                                                """
                                    )
                            }
                    )
            )
            @Valid @RequestBody JoinGatheringMemberRequest request
    );
}

package com.dokdok.book.api;

import com.dokdok.book.dto.request.PersonalReadingRecordCreateRequest;
import com.dokdok.book.dto.request.PersonalReadingRecordUpdateRequest;
import com.dokdok.book.dto.response.*;
import com.dokdok.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;

@Tag(name = "독서 기록", description = "책별 독서 기록 관련 API")
@RequestMapping("/api/book")
public interface PersonalBookRecordApi {


    @Operation(
            summary = "독서 기록 등록",
            description = """
                    내 책장에 있는 책의 독서 기록을 등록합니다.
                    - 경로의 personalBookId로 책을 지정합니다.
                    - 요청 본문: recordType(MEMO/QUOTE), recordContent, recordType이 QUOTE일 경우 meta에 page, excerpt 필수.
                    - recordType이 MEMO이면 meta는 null로 저장됩니다.
                    - 로그인한 사용자 기준으로 본인 책에만 기록을 남길 수 있습니다.
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "독서 기록 등록 성공",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = PersonalReadingRecordApiResponse.class),
                            examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                                    value = """
                                            {
                                              "code": "CREATED",
                                              "message": "기록 등록 성공",
                                              "data": {
                                                "recordId": 5,
                                                "recordType": "QUOTE",
                                                "recordContent": "오늘 기억하고 싶은 문장을 기록합니다.",
                                                "meta": {
                                                  "page": 23,
                                                  "excerpt": "이 문장이 좋았다."
                                                },
                                                "bookId": 10
                                              }
                                            }
                                            """
                            ))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 (recordType 혹은 meta 오류)",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = {
                                    @io.swagger.v3.oas.annotations.media.ExampleObject(
                                            name = "기록 타입 누락",
                                            value = """
                                                    {
                                                      "code": "R001",
                                                      "message": "기록 타입에 필요한 입력값이 누락되었습니다.",
                                                      "data": null
                                                    }
                                                    """
                                    ),
                                    @io.swagger.v3.oas.annotations.media.ExampleObject(
                                            name = "기록 타입 오류",
                                            value = """
                                                    {
                                                      "code": "R002",
                                                      "message": "존재하지 않는 타입입니다.",
                                                      "data": null
                                                    }
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
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                                    value = """
                                            {
                                              "code": "G102",
                                              "message": "인증이 필요합니다.",
                                              "data": null
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "책을 찾을 수 없음",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                                    value = """
                                            {
                                              "code": "B003",
                                              "message": "책장에 해당 책이 존재하지 않습니다.",
                                              "data": null
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "서버 오류",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                                    value = """
                                            {
                                              "code": "E000",
                                              "message": "서버 에러가 발생했습니다. 담당자에게 문의 바랍니다.",
                                              "data": null
                                            }
                                            """
                            )
                    )
            )
    })
    @PostMapping("/{personalBookId}/records")
    ResponseEntity<ApiResponse<PersonalReadingRecordCreateResponse>> createMyReadingRecord(
            @Parameter(description = "독서 기록을 남길 개인 책장 ID (personal_book 테이블 PK)", required = true, example = "10")
            @PathVariable Long personalBookId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "등록할 독서 기록 내용 및 유형",
                    required = true,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = PersonalReadingRecordCreateRequest.class),
                            examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                                    value = """
                                            {
                                              "recordType": "QUOTE",
                                              "recordContent": "오늘 기억하고 싶은 문장을 기록합니다.",
                                              "meta": {
                                                "page": 23,
                                                "excerpt": "이 문장이 좋았다."
                                              }
                                            }
                                            """
                            )
                    )
            )
            @RequestBody PersonalReadingRecordCreateRequest request
    );

    @Operation(
            summary = "독서 기록 수정",
            description = """
                    내 책장에 있는 책의 독서 기록을 수정합니다.
                    - 경로의 personalBookId와 recordId로 대상을 지정합니다.
                    - 요청 본문: recordType(MEMO/QUOTE), recordContent, recordType이 QUOTE일 경우 meta에 page, excerpt 필수.
                    - recordType이 MEMO이면 meta는 null로 저장됩니다.
                    - 로그인한 사용자 기준으로 본인 기록만 수정할 수 있습니다.
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "독서 기록 수정 성공",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = PersonalReadingRecordApiResponse.class),
                            examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                                    value = """
                                            {
                                              "code": "SUCCESS",
                                              "message": "기록 수정 성공",
                                              "data": {
                                                "recordId": 5,
                                                "recordType": "QUOTE",
                                                "recordContent": "문장을 다시 손봤습니다.",
                                                "meta": {
                                                  "page": 30,
                                                  "excerpt": "수정된 인용문"
                                                },
                                                "bookId": 10
                                              }
                                            }
                                            """
                            ))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 (recordType 혹은 meta 오류)",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = {
                                    @io.swagger.v3.oas.annotations.media.ExampleObject(
                                            name = "기록 타입 누락",
                                            value = """
                                                    {
                                                      "code": "R001",
                                                      "message": "기록 타입에 필요한 입력값이 누락되었습니다.",
                                                      "data": null
                                                    }
                                                    """
                                    ),
                                    @io.swagger.v3.oas.annotations.media.ExampleObject(
                                            name = "기록 타입 오류",
                                            value = """
                                                    {
                                                      "code": "R002",
                                                      "message": "존재하지 않는 타입입니다.",
                                                      "data": null
                                                    }
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
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                                    value = """
                                            {
                                              "code": "G102",
                                              "message": "인증이 필요합니다.",
                                              "data": null
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "책 또는 기록을 찾을 수 없음",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = {
                                    @io.swagger.v3.oas.annotations.media.ExampleObject(
                                            name = "책 없음",
                                            value = """
                                                    {
                                                      "code": "B003",
                                                      "message": "책장에 해당 책이 존재하지 않습니다.",
                                                      "data": null
                                                    }
                                                    """
                                    ),
                                    @io.swagger.v3.oas.annotations.media.ExampleObject(
                                            name = "기록 없음",
                                            value = """
                                                    {
                                                      "code": "R003",
                                                      "message": "기록을 찾을 수 없습니다.",
                                                      "data": null
                                                    }
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
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                                    value = """
                                            {
                                              "code": "E000",
                                              "message": "서버 에러가 발생했습니다. 담당자에게 문의 바랍니다.",
                                              "data": null
                                            }
                                            """
                            )
                    )
            )
    })
    @PatchMapping("/{personalBookId}/records/{recordId}")
    ResponseEntity<ApiResponse<PersonalReadingRecordCreateResponse>> updateMyReadingRecord(
            @Parameter(description = "수정할 개인 책장 ID (personal_book 테이블 PK)", required = true, example = "10")
            @PathVariable Long personalBookId,
            @Parameter(description = "수정할 기록 ID", required = true, example = "5")
            @PathVariable Long recordId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "수정할 독서 기록 내용 및 유형",
                    required = true,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = PersonalReadingRecordUpdateRequest.class),
                            examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                                    value = """
                                            {
                                              "recordType": "QUOTE",
                                              "recordContent": "문장을 다시 손봤습니다.",
                                              "meta": {
                                                "page": 30,
                                                "excerpt": "수정된 인용문"
                                              }
                                            }
                                            """
                            )
                    )
            )
            @RequestBody PersonalReadingRecordUpdateRequest request
    );

    @Operation(
            summary = "독서 기록 삭제",
            description = """
                    내 책장에 있는 책의 독서 기록을 삭제합니다.
                    - 경로의 personalBookId와 recordId로 대상을 지정합니다.
                    - Soft Delete로 처리되어 이후 조회에서 노출되지 않습니다.
                    - 로그인한 사용자 기준으로 본인 기록만 삭제할 수 있습니다.
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "독서 기록 삭제 성공",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = PersonalReadingRecordDeleteApiResponse.class),
                            examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                                    value = """
                                            {
                                              "code": "DELETED",
                                              "message": "기록 삭제 성공",
                                              "data": null
                                            }
                                            """
                            ))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "인증 실패 - 로그인이 필요합니다.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                                    value = """
                                            {
                                              "code": "G102",
                                              "message": "인증이 필요합니다.",
                                              "data": null
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "책 또는 기록을 찾을 수 없음",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = {
                                    @io.swagger.v3.oas.annotations.media.ExampleObject(
                                            name = "책 없음",
                                            value = """
                                                    {
                                                      "code": "B003",
                                                      "message": "책장에 해당 책이 존재하지 않습니다.",
                                                      "data": null
                                                    }
                                                    """
                                    ),
                                    @io.swagger.v3.oas.annotations.media.ExampleObject(
                                            name = "기록 없음",
                                            value = """
                                                    {
                                                      "code": "R003",
                                                      "message": "기록을 찾을 수 없습니다.",
                                                      "data": null
                                                    }
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
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                                    value = """
                                            {
                                              "code": "E000",
                                              "message": "서버 에러가 발생했습니다. 담당자에게 문의 바랍니다.",
                                              "data": null
                                            }
                                            """
                            )
                    )
            )
    })
    @DeleteMapping("/{personalBookId}/records/{recordId}")
    ResponseEntity<ApiResponse<Void>> deleteMyReadingRecord(
            @Parameter(description = "삭제할 개인 책장 ID (personal_book 테이블 PK)", required = true, example = "10")
            @PathVariable Long personalBookId,
            @Parameter(description = "삭제할 기록 ID", required = true, example = "5")
            @PathVariable Long recordId
    );


    @Operation(
            summary = "독서 기록 목록 조회",
            description = """
                    내 책장에 있는 책의 독서 기록을 조회합니다.
                    - 경로의 personalBookId로 책을 지정합니다.
                    - 로그인한 사용자 기준으로 본인 책의 기록만 조회됩니다.
                    - cursorCreatedAt/cursorRecordId/size 파라미터로 다음 페이지를 조회합니다.
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "독서 기록 조회 성공",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = PersonalReadingRecordListApiResponse.class),
                            examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                                    value = """
                                            {
                                              "code": "SUCCESS",
                                              "message": "기록 조회 성공",
                                              "data": {
                                                "items": [
                                                  {
                                                    "recordId": 5,
                                                    "recordType": "QUOTE",
                                                    "recordContent": "오늘 기억하고 싶은 문장을 기록합니다.",
                                                    "meta": {
                                                      "page": 23,
                                                      "excerpt": "이 문장이 좋았다."
                                                    },
                                                    "bookId": 10
                                                  }
                                                ],
                                                "pageSize": 10,
                                                "hasNext": true,
                                                "nextCursor": {
                                                  "createdAt": "2026-01-22T10:25:40Z",
                                                  "recordId": 5
                                                }
                                              }
                                            }
                                            """
                            ))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "인증 실패 - 로그인이 필요합니다.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                                    value = """
                                            {
                                              "code": "G102",
                                              "message": "인증이 필요합니다.",
                                              "data": null
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "책을 찾을 수 없음",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                                    value = """
                                            {
                                              "code": "B003",
                                              "message": "책장에 해당 책이 존재하지 않습니다.",
                                              "data": null
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "서버 오류",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                                    value = """
                                            {
                                              "code": "E000",
                                              "message": "서버 에러가 발생했습니다. 담당자에게 문의 바랍니다.",
                                              "data": null
                                            }
                                            """
                            )
                    )
            )
    })
    @GetMapping("/{personalBookId}/records")
    ResponseEntity<ApiResponse<CursorPageResponse<PersonalReadingRecordListResponse, ReadingRecordCursor>>> getMyReadingRecords(
            @Parameter(description = "개인 책장 ID (personal_book 테이블 PK)", required = true, example = "10")
            @PathVariable Long personalBookId,
            @Parameter(
                    description = "커서 - 마지막 아이템 createdAt (ISO 8601, cursorRecordId와 함께 전달)",
                    example = ""
            )
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            OffsetDateTime cursorCreatedAt,
            @Parameter(description = "커서 - 마지막 아이템 recordId (cursorCreatedAt과 함께 전달)", example = "5")
            @RequestParam(required = false) Long cursorRecordId,
            @Parameter(description = "한 페이지당 아이템 수", example = "10")
            @RequestParam(required = false) Integer size
    );

    @Schema(name = "PersonalReadingRecordApiResponse")
    record PersonalReadingRecordApiResponse(
            String code,
            String message,
            PersonalReadingRecordCreateResponse data
    ) {
    }

    @Schema(name = "PersonalReadingRecordListApiResponse")
    record PersonalReadingRecordListApiResponse(
            String code,
            String message,
            CursorPageResponse<PersonalReadingRecordListResponse, ReadingRecordCursor> data
    ) {
    }

    @Schema(name = "PersonalReadingRecordDeleteApiResponse")
    record PersonalReadingRecordDeleteApiResponse(
            String code,
            String message,
            Void data
    ) {
    }
}

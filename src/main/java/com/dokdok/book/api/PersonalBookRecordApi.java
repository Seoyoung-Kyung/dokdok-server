package com.dokdok.book.api;

import com.dokdok.book.dto.request.BookCreateRequest;
import com.dokdok.book.dto.request.PersonalReadingRecordCreateRequest;
import com.dokdok.book.dto.request.PersonalReadingRecordUpdateRequest;
import com.dokdok.book.dto.response.*;
import com.dokdok.book.entity.BookReadingStatus;
import com.dokdok.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "책 관리", description = "책 검색 및 내 책장 관리 API")
@RequestMapping("/api/book")
public interface PersonalBookRecordApi {


    @Operation(
            summary = "독서 기록 등록",
            description = """
                    내 책장에 있는 책의 독서 기록을 등록합니다.
                    - 경로의 bookId로 책을 지정합니다.
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
                            schema = @Schema(implementation = ApiResponse.class),
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
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청 (recordType 혹은 meta 오류)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패 - 로그인이 필요합니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "책을 찾을 수 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PostMapping("/{bookId}")
    ResponseEntity<ApiResponse<PersonalReadingRecordCreateResponse>> createMyReadingRecord(
            @Parameter(description = "독서 기록을 남길 책 ID (book 테이블 PK)", required = true, example = "10")
            @PathVariable Long bookId,
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
                    - 경로의 bookId와 recordId로 대상을 지정합니다.
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
                            schema = @Schema(implementation = ApiResponse.class),
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
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청 (recordType 혹은 meta 오류)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패 - 로그인이 필요합니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "책 또는 기록을 찾을 수 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PatchMapping("/{bookId}/records/{recordId}")
    ResponseEntity<ApiResponse<PersonalReadingRecordCreateResponse>> updateMyReadingRecord(
            @Parameter(description = "수정할 책 ID (book 테이블 PK)", required = true, example = "10")
            @PathVariable Long bookId,
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
                    - 경로의 bookId와 recordId로 대상을 지정합니다.
                    - Soft Delete로 처리되어 이후 조회에서 노출되지 않습니다.
                    - 로그인한 사용자 기준으로 본인 기록만 삭제할 수 있습니다.
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "독서 기록 삭제 성공",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                                    value = """
                                            {
                                              "code": "DELETED",
                                              "message": "기록 삭제 성공"
                                            }
                                            """
                            ))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패 - 로그인이 필요합니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "책 또는 기록을 찾을 수 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @DeleteMapping("/{bookId}/records/{recordId}")
    ResponseEntity<ApiResponse<Void>> deleteMyReadingRecord(
            @Parameter(description = "삭제할 책 ID (book 테이블 PK)", required = true, example = "10")
            @PathVariable Long bookId,
            @Parameter(description = "삭제할 기록 ID", required = true, example = "5")
            @PathVariable Long recordId
    );


    @Operation(
            summary = "독서 기록 목록 조회",
            description = """
                    내 책장에 있는 책의 독서 기록을 조회합니다.
                    - 경로의 bookId로 책을 지정합니다.
                    - 로그인한 사용자 기준으로 본인 책의 기록만 조회됩니다.
                    - page/size/sort 파라미터로 페이징과 정렬을 제어할 수 있습니다. (기본 정렬: createdAt DESC)
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "독서 기록 조회 성공",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiResponse.class),
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
                                                "totalCount": 1,
                                                "currentPage": 0,
                                                "pageSize": 10,
                                                "totalPages": 1
                                              }
                                            }
                                            """
                            ))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패 - 로그인이 필요합니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "책을 찾을 수 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/{bookId}/records")
    ResponseEntity<ApiResponse<PageResponse<PersonalReadingRecordListResponse>>> getMyReadingRecords(
            @Parameter(description = "책 ID (book 테이블 PK)", required = true, example = "10")
            @PathVariable Long bookId,
            @ParameterObject
            @Parameter(
                    description = "페이징 정보 (page: 페이지 번호, size: 페이지 크기, sort: 정렬 기준)",
                    example = "page=0&size=10&sort=createdAt,desc"
            )
            @PageableDefault(
                    size = 10,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            ) Pageable pageable
    );
}

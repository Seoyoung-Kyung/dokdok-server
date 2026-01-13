package com.dokdok.book.api;

import com.dokdok.book.dto.request.BookCreateRequest;
import com.dokdok.book.dto.response.KakaoBookResponse;
import com.dokdok.book.dto.response.PersonalBookCreateResponse;
import com.dokdok.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "책 관리", description = "책 검색 및 내 책장 관리 API")
@RequestMapping("/api/book")
public interface BookApi {

    @Operation(
            summary = "외부 책 API 조회",
            description = "검색어로 책 정보를 조회합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "책 조회 성공",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                                    value = """
                                            {
                                              "code": "SUCCESS",
                                              "message": "책 정보 조회 성공",
                                              "data": {
                                                "documents": [
                                                  {
                                                    "title": "예제 도서명",
                                                    "contents": "책 소개",
                                                    "authors": ["저자A", "저자B"],
                                                    "publisher": "출판사",
                                                    "isbn": "9788994757254",
                                                    "thumbnail": "https://example.com/thumb.jpg"
                                                  }
                                                ],
                                                "meta": {
                                                  "is_end": true,
                                                  "pageable_count": 1,
                                                  "total_count": 1
                                                }
                                              }
                                            }
                                            """
                            ))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "책을 찾을 수 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/search")
    ResponseEntity<ApiResponse<KakaoBookResponse>> searchBook(
            @Parameter(description = "책 제목, 내용 등에 사용할 검색어", required = true)
            @RequestParam String query
    );


    @Operation(
            summary = "내 책장에 책 등록",
            description = "조회한 책을 내 책장에 등록합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "책장에 책 등록 성공",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                                    value = """
                                            {
                                              "code": "CREATED",
                                              "message": "내 책장에 책 등록 성공",
                                              "data": {
                                                "isbn": "9788994757254",
                                                "readingStatus": "READING",
                                                "addedAt": "2026-01-13T08:36:03.043Z"
                                              }
                                            }
                                            """
                            ))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "책을 찾을 수 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PostMapping
    ResponseEntity<ApiResponse<PersonalBookCreateResponse>> createBook(@Valid @RequestBody BookCreateRequest bookCreateRequest);
}

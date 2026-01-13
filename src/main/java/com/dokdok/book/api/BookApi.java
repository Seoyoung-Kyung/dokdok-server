package com.dokdok.book.api;

import com.dokdok.book.dto.request.BookCreateRequest;
import com.dokdok.book.dto.response.KakaoBookResponse;
import com.dokdok.book.dto.response.PersonalBookCreateResponse;
import com.dokdok.global.response.ApiResponse;
import com.dokdok.meeting.dto.MeetingResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "책 관리", description = " 관련 API")
@RequestMapping("/api/book")
public interface BookApi {

    @Operation(
            summary = "외부 책 API 조회",
            description = "검색할 책을 조회합니다.",
            parameters = {
                    @Parameter(name = "title", description = "책 이름, 책 내용 등을 검색할 수 있습니다.", in = ParameterIn.PATH, required = true)
            }
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "책 조회 성공",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = MeetingResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "책을 찾을 수 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/search")
    ResponseEntity<ApiResponse<KakaoBookResponse>> searchBook(@RequestParam String query);


    @Operation(
            summary = "내 책장에 책 등록",
            description = "조회한 책을 내 책장에 등록합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "책장에 책 등록 성공",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = MeetingResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "책을 찾을 수 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PostMapping
    ResponseEntity<ApiResponse<PersonalBookCreateResponse>> createBook(@RequestBody BookCreateRequest bookCreateRequest);
}

package com.dokdok.book.api;

import com.dokdok.book.dto.request.BookReviewRequest;
import com.dokdok.book.dto.response.BookReviewResponse;
import com.dokdok.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "책 리뷰", description = "책 리뷰 관련 API")
public interface BookReviewApi {

    @Operation(
            summary = "책 리뷰 생성 (developer: 양재웅)",
            description = """
            책 리뷰를 생성합니다.
            - 입력: 별점(0.5 단위, 0.5~5.0), 키워드 ID 리스트
            - 권한: 로그인 사용자
            """,
            parameters = {
                    @Parameter(name = "bookId", description = "책 식별자", in = ParameterIn.PATH, required = true)
            }
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "책 리뷰 생성 성공",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = BookReviewResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "reviewId": 101,
                                      "bookId": 12,
                                      "userId": 5,
                                      "rating": 4.5,
                                      "keywords": [
                                        { "id": 3, "name": "감동" },
                                        { "id": 7, "name": "몰입" }
                                      ]
                                    }
                                    """))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = {
                                    @ExampleObject(
                                            name = "별점 유효성 오류",
                                            value = """
                                                    {"code": "B008", "message": "별점은 0.5 단위의 5점 만점 값이어야 합니다.", "data": null}
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "선택 불가 키워드",
                                            value = """
                                                    {"code": "B007", "message": "선택할 수 없는 키워드입니다.", "data": null}
                                                    """
                                    )
                            })),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "책 또는 키워드를 찾을 수 없음",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = {
                                    @ExampleObject(
                                            name = "책 없음",
                                            value = """
                                                    {"code": "B001", "message": "책을 찾을 수 없습니다.", "data": null}
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "키워드 없음",
                                            value = """
                                                    {"code": "B006", "message": "키워드를 찾을 수 없습니다.", "data": null}
                                                    """
                                    )
                            })),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "이미 책 리뷰가 존재함",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                                    {"code": "B005", "message": "이미 책 리뷰가 존재합니다.", "data": null}
                                    """))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                                    {"code": "E000", "message": "서버 에러가 발생했습니다. 담당자에게 문의 바랍니다.", "data": null}
                                    """)))
    })
    @PostMapping(value = "/{bookId}/reviews", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<ApiResponse<BookReviewResponse>> createReview(
            @PathVariable Long bookId,
            @Valid @RequestBody BookReviewRequest request
    );

    @Operation(
            summary = "내 책 리뷰 조회 (developer: 양재웅)",
            description = """
            로그인한 사용자의 책 리뷰를 조회합니다.
            - 권한: 로그인 사용자
            """,
            parameters = {
                    @Parameter(name = "bookId", description = "책 식별자", in = ParameterIn.PATH, required = true)
            }
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "책 리뷰 조회 성공",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = BookReviewResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "reviewId": 101,
                                      "bookId": 12,
                                      "userId": 5,
                                      "rating": 4.5,
                                      "keywords": [
                                        { "id": 3, "name": "감동" },
                                        { "id": 7, "name": "몰입" }
                                      ]
                                    }
                                    """))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "책 리뷰를 찾을 수 없음",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                                    {"code": "B004", "message": "책 리뷰를 찾을 수 없습니다.", "data": null}
                                    """))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                                    {"code": "E000", "message": "서버 에러가 발생했습니다. 담당자에게 문의 바랍니다.", "data": null}
                                    """)))
    })
    @GetMapping(value = "/{bookId}/reviews/me", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<ApiResponse<BookReviewResponse>> getMyReview(
            @PathVariable Long bookId
    );

    @Operation(
            summary = "내 책 리뷰 수정 (developer: 양재웅)",
            description = """
            로그인한 사용자의 책 리뷰를 수정합니다.
            - 입력: 별점(0.5 단위, 0.5~5.0), 키워드 ID 리스트
            - 권한: 로그인 사용자
            """,
            parameters = {
                    @Parameter(name = "bookId", description = "책 식별자", in = ParameterIn.PATH, required = true)
            }
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "책 리뷰 수정 성공",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = BookReviewResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "reviewId": 101,
                                      "bookId": 12,
                                      "userId": 5,
                                      "rating": 3.5,
                                      "keywords": [
                                        { "id": 5, "name": "희망" },
                                        { "id": 8, "name": "위로" }
                                      ]
                                    }
                                    """))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = {
                                    @ExampleObject(
                                            name = "별점 유효성 오류",
                                            value = """
                                                    {"code": "B008", "message": "별점은 0.5 단위의 5점 만점 값이어야 합니다.", "data": null}
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "선택 불가 키워드",
                                            value = """
                                                    {"code": "B007", "message": "선택할 수 없는 키워드입니다.", "data": null}
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "삭제된 리뷰",
                                            value = """
                                                    {"code": "B009", "message": "삭제된 책 리뷰입니다.", "data": null}
                                                    """
                                    )
                            })),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "책 리뷰 또는 키워드를 찾을 수 없음",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = {
                                    @ExampleObject(
                                            name = "책 리뷰 없음",
                                            value = """
                                                    {"code": "B004", "message": "책 리뷰를 찾을 수 없습니다.", "data": null}
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "키워드 없음",
                                            value = """
                                                    {"code": "B006", "message": "키워드를 찾을 수 없습니다.", "data": null}
                                                    """
                                    )
                            })),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                                    {"code": "E000", "message": "서버 에러가 발생했습니다. 담당자에게 문의 바랍니다.", "data": null}
                                    """)))
    })
    @PatchMapping(value = "/{bookId}/reviews/me", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<ApiResponse<BookReviewResponse>> updateMyReview(
            @PathVariable Long bookId,
            @Valid @RequestBody BookReviewRequest request
    );

    @Operation(
            summary = "내 책 리뷰 삭제 (developer: 양재웅)",
            description = """
            로그인한 사용자의 책 리뷰를 삭제합니다.
            - 권한: 로그인 사용자
            """,
            parameters = {
                    @Parameter(name = "bookId", description = "책 식별자", in = ParameterIn.PATH, required = true)
            }
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "책 리뷰 삭제 성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "책 리뷰를 찾을 수 없음",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                                    {"code": "B004", "message": "책 리뷰를 찾을 수 없습니다.", "data": null}
                                    """))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                                    {"code": "E000", "message": "서버 에러가 발생했습니다. 담당자에게 문의 바랍니다.", "data": null}
                                    """)))
    })
    @DeleteMapping(value = "/{bookId}/reviews/me")
    ResponseEntity<ApiResponse<Void>> deleteMyReview(
            @PathVariable Long bookId
    );
}

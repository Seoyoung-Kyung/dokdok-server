package com.dokdok.retrospective.api;

import com.dokdok.global.response.ApiResponse;
import com.dokdok.retrospective.dto.request.MeetingRetrospectiveRequest;
import com.dokdok.retrospective.dto.response.MeetingRetrospectiveResponse;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "공동 회고", description = "공동 회고 관련 API")
public interface MeetingRetrospectiveApi {

    @Operation(
            summary = "공동 회고 조회 (developer: 오주현)",
            description = """
            약속에 대한 공동 회고를 조회합니다.
            - 권한: 모임장, 약속장, 약속 참여자
            - 제약: 약속에 참여한 사용자만 조회 가능
            """,
            parameters = {
                    @Parameter(name = "meetingId", description = "약속 식별자", in = ParameterIn.PATH, required = true)
            }
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "공동 회고 조회 성공",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = MeetingRetrospectiveResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "code": "SUCCESS",
                                      "message": "공동 회고 조회 성공",
                                      "data": {
                                        "meetingId": 1,
                                        "meetingName": "데미안을 읽어보아요 데미안을 읽어보아요 데미",
                                        "meetingDate": "2026-01-15",
                                        "meetingTime": "19:00-20:00",
                                        "topics": [
                                          {
                                            "topicId": 1,
                                            "topicName": "가볍 목업, 유사 목업",
                                            "summarizedOpinions": [
                                              "실제적인 것에 찾기 과정이 없어 학습해야 고려할 때마다 알도다",
                                              "데미안이라는 인물이 실존하는지, 아니면 내면의 동일시인 때문에 찾아 온미팅다",
                                              "아브락시스 과잉이 실제 역에 아브발달 남아되는 결과적 때시대를 안정한다"
                                            ],
                                            "comments": [
                                              {
                                                "meetingRetrospectiveId": 1,
                                                "userId": 1,
                                                "nickname": "사용자1",
                                                "profileImageUrl": "https://...",
                                                "comment": "모임 하기전엔 이랬는데, 누구의 이런 발을 듣고 이렇게 생각이 바뀌었다.",
                                                "createdAt": "2026-01-15T15:30:00"
                                              },
                                              {
                                                "meetingRetrospectiveId": 2,
                                                "userId": 2,
                                                "nickname": "사용자2",
                                                "profileImageUrl": "https://...",
                                                "comment": "모임 하기전엔 이랬는데, 누구의 이런 발을 듣고 이렇게 생각이 바뀌었다.",
                                                "createdAt": "2026-01-15T15:35:00"
                                              }
                                            ]
                                          },
                                          {
                                            "topicId": 2,
                                            "topicName": "신과 악",
                                            "summarizedOpinions": [
                                              "세가 앞에서 나으는 일만은 자연의 언성을 실질적는 감정전 아이라",
                                              "발과 어둠의 세계가 대조적으로 묘사되며 갈등을 고조한다다"
                                            ],
                                            "comments": [
                                              {
                                                "meetingRetrospectiveId": 3,
                                                "userId": 1,
                                                "nickname": "사용자1",
                                                "profileImageUrl": "https://...",
                                                "comment": "또 다른 의견 내용",
                                                "createdAt": "2026-01-15T15:40:00"
                                              }
                                            ]
                                          }
                                        ]
                                      }
                                    }
                                    """)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "접근 권한 없음",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                                    {"code": "R105", "message": "회고에 접근할 권한이 없습니다.", "data": null}
                                    """))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "약속을 찾을 수 없음",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                                    {"code": "M001", "message": "약속을 찾을 수 없습니다.", "data": null}
                                    """))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                                    {"code": "E000", "message": "서버 에러가 발생했습니다. 담당자에게 문의 바랍니다.", "data": null}
                                    """)))
    })
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<ApiResponse<MeetingRetrospectiveResponse>> getMeetingRetrospective(
            @PathVariable Long meetingId
    );

    @Operation(
            summary = "공동 회고 작성 (developer: 오주현)",
            description = """
            약속에 대한 공동 회고를 작성합니다.
            - 제약: 약속에 참여한 사용자만 작성 가능
            """,
            parameters = {
                    @Parameter(name = "meetingId", description = "약속 식별자", in = ParameterIn.PATH, required = true)
            }
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "공동 회고 작성 완료",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = MeetingRetrospectiveResponse.CommentResponse.class),
                            examples = @ExampleObject(value = """
                                    {"code": "CREATED", "message": "공동 회고 작성 완료", "data": {"meetingRetrospectiveId": 1, "userId": 1, "nickname": "독서왕", "profileImageUrl": "https://example.com/profile.jpg", "comment": "좋았습니다.", "createdAt": "2025-02-01T16:30:00"}}
                                    """)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                                    {"code": "G002", "message": "입력값이 올바르지 않습니다.", "data": null}
                                    """))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "접근 권한 없음",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                                    {"code": "R105", "message": "회고에 접근할 권한이 없습니다.", "data": null}
                                    """))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "약속 또는 토픽을 찾을 수 없음",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                                    {"code": "M001", "message": "약속을 찾을 수 없습니다.", "data": null}
                                    """))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "이미 삭제된 주제",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                                    {"code": "E106", "message": "이미 삭제된 주제입니다.", "data": null}
                                    """))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                                    {"code": "E000", "message": "서버 에러가 발생했습니다. 담당자에게 문의 바랍니다.", "data": null}
                                    """)))
    })
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<ApiResponse<MeetingRetrospectiveResponse.CommentResponse>> createMeetingRetrospective(
            @PathVariable Long meetingId,
            @Valid @RequestBody MeetingRetrospectiveRequest request
    );

    @Operation(
            summary = "공동 회고 코멘트 삭제 (developer: 오주현)",
            description = """
            약속에 대한 공동 회고 코멘트를 삭제합니다.
            - 권한: 작성자 또는 모임장
            """,
            parameters = {
                    @Parameter(name = "meetingId", description = "약속 식별자", in = ParameterIn.PATH, required = true),
                    @Parameter(name = "meetingRetrospectiveId", description = "공동 회고 식별자", in = ParameterIn.PATH, required = true)
            }
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "공동 회고 코멘트 삭제 완료",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(value = """
                                    {"code": "DELETED", "message": "공동 회고 코멘트 삭제 완료", "data": null}
                                    """))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "접근 권한 없음",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                                    {"code": "GA003", "message": "리더만 가능한 작업입니다.", "data": null}
                                    """))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "공동 회고를 찾을 수 없음",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                                    {"code": "R103", "message": "공동 회고 내용을 찾을 수 없습니다.", "data": null}
                                    """))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                                    {"code": "E000", "message": "서버 에러가 발생했습니다. 담당자에게 문의 바랍니다.", "data": null}
                                    """)))
    })
    @DeleteMapping(path = "/{meetingRetrospectiveId}", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<ApiResponse<Void>> deleteMeetingRetrospective(
            @PathVariable Long meetingId,
            @PathVariable Long meetingRetrospectiveId
    );
}

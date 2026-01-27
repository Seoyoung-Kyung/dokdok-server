package com.dokdok.retrospective.api;

import com.dokdok.global.response.ApiResponse;
import com.dokdok.retrospective.dto.request.RetrospectiveSummaryUpdateRequest;
import com.dokdok.retrospective.dto.response.RetrospectiveSummaryResponse;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "AI 요약", description = "AI 요약 관련 API")
public interface RetrospectiveSummaryApi {

    @Operation(
            summary = "AI 요약 조회 (developer: 오주현)",
            description = """                                                                                                                                                    
                      약속에 대한 AI 요약을 조회합니다.
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
                    description = "AI 요약 조회 성공",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = RetrospectiveSummaryResponse.class),
                            examples = @ExampleObject(value = """                                                                                                                
                                      {
                                        "code": "SUCCESS",
                                        "message": "AI 요약 조회 성공",
                                        "data": {
                                          "meetingId": 1,
                                          "topics": [
                                            {
                                              "topicId": 1,
                                              "topicTitle": "가짜 욕망, 유사 욕망",
                                              "topicDescription": "가짜욕망, 유사욕망에 대해 이야기해봅시다.",
                                              "summary": "참여자들은 『데미안』 속 싱클레어가 느꼈던 혼란을 자신들의 경험과 연결하며...",
                                              "keyPoint": "1) 사회가 만든 욕망의 구조..."
                                            },
                                            {
                                              "topicId": 2,
                                              "topicTitle": "선과 악",
                                              "topicDescription": "인간의 세계에서 선과 악 어느 것이 힘이 더 셀까",
                                              "summary": "선과 악 중 어느 쪽이 더 강한지를 묻기보다...",
                                              "keyPoint": "악이 더 강해보이는 이유"
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
    ResponseEntity<ApiResponse<RetrospectiveSummaryResponse>> getRetrospectiveSummary(
            @PathVariable Long meetingId
    );

    @Operation(
            summary = "AI 요약 수정 (developer: 오주현)",
            description = """                                                                                                                                                    
                      약속에 대한 AI 요약을 수정합니다.
                      - 권한: 모임장, 약속장
                      - 제약: 모임장 또는 약속장만 수정 가능
                      """,
            parameters = {
                    @Parameter(name = "meetingId", description = "약속 식별자", in = ParameterIn.PATH, required = true)
            }
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "AI 요약 수정 성공",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = RetrospectiveSummaryResponse.class),
                            examples = @ExampleObject(value = """                                                                                                                
                                      {
                                        "code": "SUCCESS",
                                        "message": "AI 요약 수정 성공",
                                        "data": {
                                          "meetingId": 1,                                                                                                                          
                                          "topics": [                                                                                                                              
                                            {                                                                                                                                      
                                              "topicId": 1,                                                                                                                        
                                              "topicTitle": "가짜 욕망, 유사 욕망",                                                                                                
                                              "topicDescription": "가짜욕망, 유사욕망에 대해 이야기해봅시다.",                                                                     
                                              "summary": "수정된 핵심 요약...",                                                                                                    
                                              "keyPoint": "수정된 주요 포인트..."                                                                                                  
                                            }                                                                                                                                      
                                          ]                                                                                                                                        
                                        }                                                                                                                                          
                                      }                                                                                                                                            
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
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "약속 또는 AI 요약을 찾을 수 없음",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """                                                                                                                
                                      {"code": "R106", "message": "AI 요약을 찾을 수 없습니다.", "data": null}
                                      """))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """                                                                                                                
                                      {"code": "E000", "message": "서버 에러가 발생했습니다. 담당자에게 문의 바랍니다.", "data": null}
                                      """)))
    })
    @PatchMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<ApiResponse<RetrospectiveSummaryResponse>> updateRetrospectiveSummary(
            @PathVariable Long meetingId,
            @Valid @RequestBody RetrospectiveSummaryUpdateRequest request
    );
}

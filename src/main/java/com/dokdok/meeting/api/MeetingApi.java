package com.dokdok.meeting.api;

import com.dokdok.global.response.ApiResponse;
import com.dokdok.meeting.dto.MeetingCreateRequest;
import com.dokdok.meeting.dto.MeetingResponse;
import com.dokdok.meeting.dto.MeetingStatusResponse;
import com.dokdok.meeting.dto.MeetingUpdateRequest;
import com.dokdok.meeting.dto.MeetingUpdateResponse;
import com.dokdok.meeting.entity.MeetingStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

@Tag(name = "약속 관리", description = "약속 관련 API")
public interface MeetingApi {

    @Operation(
            summary = "약속 상세 조회",
            description = """
            약속 상세 정보를 조회합니다.
            - 약속 이름, 책 이름, 약속 일시, 주제 타입 확인
            - 정렬 필터: 최신순/이름순
            - 권한: 모임장, 모임원, 약속장
            - 제약: 모임에 속한 사용자만 조회 가능
            """,
            parameters = {
                    @Parameter(name = "meetingId", description = "약속 식별자", in = ParameterIn.PATH, required = true)
            }
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "약속 상세 조회 성공",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = MeetingResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "약속을 찾을 수 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류")
    })
    ResponseEntity<ApiResponse<MeetingResponse>> findMeeting(
            Long meetingId
    );

    @Operation(
            summary = "약속 생성 신청",
            description = """
            모임 구성원이 약속 생성을 신청합니다.
            - 입력: 약속 제목(미입력 시 책 제목), 책 제목*, 약속 일시*, 최대 인원 수(null 허용), 장소(null 허용)
            - 권한: 해당 모임의 구성원
            """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "약속 생성 요청 성공",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = MeetingResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "모임 또는 책 또는 사용자를 찾을 수 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류")
    })
    ResponseEntity<ApiResponse<MeetingResponse>> createMeeting(
            MeetingCreateRequest request
    );

    @Operation(
            summary = "약속 생성 확정",
            description = """
            신청된 약속 중 하나를 확정합니다.
            - 상태: PENDING(신청), CONFIRMED(모임장 확정), DONE(종료)
            - 확정 시 신청자가 약속장으로 변경된다.
            - 약속장은 자동으로 약속에 포함된다 (MeetingMember).
            - 확정된 약속은 되돌릴 수 없다.
            - 모임장은 만든 약속을 바로 확정할 수 있다.
            - 동일 시간에 약속 하나만 확정 가능
            - 신청된 약속이 없으면 모임장이 약속을 바로 생성/확정 가능
            """,
            parameters = {
                    @Parameter(name = "meetingId", description = "약속 식별자", in = ParameterIn.PATH, required = true),
                    @Parameter(name = "meetingStatus", description = "변경할 약속 상태 (PENDING, CONFIRMED, DONE)",
                            in = ParameterIn.QUERY, required = true)
            }
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "약속 상태 변경 성공",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = MeetingStatusResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "약속을 찾을 수 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류")
    })
    ResponseEntity<ApiResponse<MeetingStatusResponse>> changeMeetingStatus(
            Long meetingId,
            MeetingStatus meetingStatus
    );

    @Operation(
            summary = "약속 참가 신청",
            description = """
            약속에 참가 신청합니다.
            - 권한: 모임원 전원
            - 제약: 모집 정원 마감 전
            """,
            parameters = {
                    @Parameter(name = "meetingId", description = "약속 식별자", in = ParameterIn.PATH, required = true)
            }
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "약속 참가 신청 성공",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = Long.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "모임 멤버가 아님"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "약속 또는 사용자를 찾을 수 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류")
    })
    ResponseEntity<ApiResponse<Long>> joinMeeting(
            Long meetingId
    );

    @Operation(
            summary = "약속 참가 취소",
            description = """
            약속 참가 신청을 취소합니다.
            - 권한: 약속에 참여한 모임원
            - 제약: 약속 시작 24시간 이내 취소 불가
            - 주제를 등록한 참가자가 취소하면 주제도 삭제 처리
            """,
            parameters = {
                    @Parameter(name = "meetingId", description = "약속 식별자", in = ParameterIn.PATH, required = true)
            }
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "약속 참가 취소 성공",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = Long.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "약속 멤버가 아님"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "약속을 찾을 수 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류")
    })
    ResponseEntity<ApiResponse<Long>> cancelMeeting(
            Long meetingId
    );

    @Operation(
            summary = "약속 수정",
            description = """
            약속 정보를 수정합니다.
            - 권한: 약속장
            - 제약: 종료된 약속은 수정 불가
            - 제약: 종료 일시는 시작 일시보다 이전일 수 없음
            - 제약: 최대 참여 인원은 현재 참여 인원보다 작을 수 없음
            """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "약속 수정 성공",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = MeetingUpdateResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "약속장 아님"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "약속을 찾을 수 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류")
    })
    ResponseEntity<ApiResponse<MeetingUpdateResponse>> updateMeeting(
            @Parameter(name = "meetingId", description = "약속 식별자", in = ParameterIn.PATH, required = true)
            Long meetingId,
            MeetingUpdateRequest request
    );

}

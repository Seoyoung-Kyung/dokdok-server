package com.dokdok.meeting.dto;

import com.dokdok.meeting.entity.MeetingLocation;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.time.LocalDateTime;

@Schema(description = "약속 생성 요청")
@Builder
public record MeetingCreateRequest(
        @Schema(description = "모임 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        Long gatheringId,

        @Schema(description = "책 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        Long bookId,

        @Schema(description = "약속 이름 (미입력 시 책 제목 사용)", example = "1월 독서 모임", minLength = 1, maxLength = 24)
        @Size(min = 1, max = 24)
        String meetingName,

        @Schema(description = "약속 시작 일시", example = "2025-02-01T14:00:00", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull
        LocalDateTime meetingStartDate,

        @Schema(description = "약속 종료 일시", example = "2025-02-01T16:00:00", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull
        LocalDateTime meetingEndDate,

        @Schema(description = "최대 참가 인원 (null 허용)", example = "10")
        Integer maxParticipants,

        @Schema(description = "장소 정보 (null 허용)")
        MeetingLocationDto location
) {
    public MeetingLocation toLocationEntity() {
        if (location == null) {
            return null;
        }
        return location.toEntity();
    }
}

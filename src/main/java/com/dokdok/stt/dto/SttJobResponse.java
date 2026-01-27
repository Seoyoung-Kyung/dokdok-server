package com.dokdok.stt.dto;

import com.dokdok.stt.entity.SttJob;
import com.dokdok.stt.entity.SttJobStatus;

import java.time.LocalDateTime;

public record SttJobResponse(
        Long jobId,
        Long meetingId,
        Long userId,
        SttJobStatus status,
        String resultText,
        String errorMessage,
        LocalDateTime createdAt
) {
    public static SttJobResponse from(SttJob job) {
        return new SttJobResponse(
                job.getId(),
                job.getMeeting().getId(),
                job.getUser().getId(),
                job.getStatus(),
                job.getResultText(),
                job.getErrorMessage(),
                job.getCreatedAt()
        );
    }
}

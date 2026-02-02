package com.dokdok.stt.dto;

import com.dokdok.stt.entity.SttJob;
import com.dokdok.stt.entity.SttJobStatus;
import com.dokdok.stt.entity.SttSummary;

import java.time.LocalDateTime;
import java.util.List;

public record SttJobResponse(
        Long jobId,
        Long meetingId,
        Long userId,
        SttJobStatus status,
        String resultText,
        String summary,
        List<String> highlights,
        List<String> keywords,
        String errorMessage,
        LocalDateTime createdAt
) {
    public static SttJobResponse from(SttJob job, SttSummary summary) {
        return new SttJobResponse(
                job.getId(),
                job.getMeeting().getId(),
                job.getUser().getId(),
                job.getStatus(),
                job.getResultText(),
                summary != null ? summary.getSummary() : null,
                summary != null ? summary.getHighlights() : null,
                summary != null ? summary.getKeywords() : null,
                job.getErrorMessage(),
                job.getCreatedAt()
        );
    }
}

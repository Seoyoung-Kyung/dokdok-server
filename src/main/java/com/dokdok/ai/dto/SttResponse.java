package com.dokdok.ai.dto;

public record SttResponse(
        Long jobId,
        String status,
        String text,
        String errorMessage
) {
}

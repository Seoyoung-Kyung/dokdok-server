package com.dokdok.ai.dto;

public record SttRequest(
        Long jobId,
        String filePath,
        String language
) {
}

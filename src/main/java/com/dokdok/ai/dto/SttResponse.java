package com.dokdok.ai.dto;

import java.util.List;

public record SttResponse(
        Long jobId,
        String status,
        String text,
        String summary,
        List<String> mainPoints,
        List<String> highlights,
        List<String> keywords,
        String errorMessage
) {
}

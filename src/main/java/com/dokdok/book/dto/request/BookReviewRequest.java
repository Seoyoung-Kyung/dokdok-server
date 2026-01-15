package com.dokdok.book.dto.request;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record BookReviewRequest(
        BigDecimal rating,

        @NotNull(message = "keywordId는 필수입니다")
        Long keywordId
) {
}

package com.dokdok.book.dto.request;

import jakarta.validation.constraints.NotEmpty;

import java.math.BigDecimal;
import java.util.List;

public record BookReviewRequest(
        BigDecimal rating,

        @NotEmpty(message = "keywordIds는 필수입니다")
        List<Long> keywordIds
) {
}

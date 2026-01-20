package com.dokdok.book.dto.response;

import com.dokdok.book.entity.BookReview;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public record BookReviewResponse(
        Long reviewId,
        Long bookId,
        Long userId,
        BigDecimal rating,
        List<Long> keywordIds,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static BookReviewResponse from(BookReview review) {
        return new BookReviewResponse(
                review.getId(),
                review.getBook().getId(),
                review.getUser().getId(),
                review.getRating(),
                review.getKeywords().stream()
                        .map(reviewKeyword -> reviewKeyword.getKeyword().getId())
                        .collect(Collectors.toList()),
                review.getCreatedAt(),
                review.getUpdatedAt()
        );
    }
}

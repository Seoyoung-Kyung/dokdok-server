package com.dokdok.book.dto.response;

import com.dokdok.book.entity.BookReview;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record BookReviewResponse(
        Long reviewId,
        Long bookId,
        Long userId,
        BigDecimal rating,
        Long keywordId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static BookReviewResponse from(BookReview review) {
        return new BookReviewResponse(
                review.getId(),
                review.getBook().getId(),
                review.getUser().getId(),
                review.getRating(),
                review.getKeyword().getId(),
                review.getCreatedAt(),
                review.getUpdatedAt()
        );
    }
}

package com.dokdok.book.dto.response;

import com.dokdok.book.entity.BookReview;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

public record BookReviewResponse(
        Long reviewId,
        Long bookId,
        Long userId,
        BigDecimal rating,
        List<KeywordInfo> keywords
) {
    public static BookReviewResponse from(BookReview review) {
        List<KeywordInfo> keywordInfos = review.getKeywords().stream()
                .map(reviewKeyword -> new KeywordInfo(
                        reviewKeyword.getKeyword().getId(),
                        reviewKeyword.getKeyword().getKeywordName()
                ))
                .collect(Collectors.toList());

        return new BookReviewResponse(
                review.getId(),
                review.getBook().getId(),
                review.getUser().getId(),
                review.getRating(),
                keywordInfos
        );
    }

    public record KeywordInfo(
            Long id,
            String name
    ) {
    }
}

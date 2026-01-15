package com.dokdok.book.service;

import com.dokdok.book.dto.request.BookReviewRequest;
import com.dokdok.book.dto.response.BookReviewResponse;
import com.dokdok.book.entity.Book;
import com.dokdok.book.entity.BookReview;
import com.dokdok.book.exception.BookErrorCode;
import com.dokdok.book.exception.BookException;
import com.dokdok.book.repository.BookReviewRepository;
import com.dokdok.global.util.SecurityUtil;
import com.dokdok.keyword.entity.Keyword;
import com.dokdok.keyword.service.KeywordValidator;
import com.dokdok.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class BookReviewService {

    private final BookReviewRepository bookReviewRepository;
    private final BookValidator bookValidator;
    private final KeywordValidator keywordValidator;

    @Transactional
    public BookReviewResponse createReview(Long bookId, BookReviewRequest request) {
        Long userId = SecurityUtil.getCurrentUserId();
        validateRating(request.rating());

        Book book = bookValidator.validateAndGetBook(bookId);

        Keyword keyword = keywordValidator.validateAndGetSelectableKeyword(
                request.keywordId()
        );

        bookValidator.validateReviewNotExists(bookId, userId);

        User user = SecurityUtil.getCurrentUserEntity();

        BookReview review = BookReview.create(book, user, request.rating(), keyword);

        return BookReviewResponse.from(bookReviewRepository.save(review));
    }

    @Transactional(readOnly = true)
    public BookReviewResponse getMyReview(Long bookId) {
        Long userId = SecurityUtil.getCurrentUserId();

        BookReview review = bookValidator.validateAndGetReview(bookId, userId);

        return BookReviewResponse.from(review);
    }

    @Transactional
    public BookReviewResponse updateMyReview(Long bookId, BookReviewRequest request) {
        Long userId = SecurityUtil.getCurrentUserId();
        validateRating(request.rating());

        BookReview review = bookValidator.validateAndGetReview(bookId, userId);

        Keyword keyword = keywordValidator.validateAndGetSelectableKeyword(
                request.keywordId()
        );

        review.updateReview(request.rating(), keyword);

        return BookReviewResponse.from(review);
    }

    private void validateRating(BigDecimal rating) {
        if (rating == null) {
            return;
        }
        BigDecimal min = new BigDecimal("0.5");
        BigDecimal max = new BigDecimal("5.0");
        if (rating.compareTo(min) < 0 || rating.compareTo(max) > 0) {
            throw new BookException(BookErrorCode.BOOK_REVIEW_INVALID_RATING);
        }
        BigDecimal scaled = rating.multiply(BigDecimal.TEN);
        if (scaled.remainder(new BigDecimal("5")).compareTo(BigDecimal.ZERO) != 0) {
            throw new BookException(BookErrorCode.BOOK_REVIEW_INVALID_RATING);
        }
    }
}

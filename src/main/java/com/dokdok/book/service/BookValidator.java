package com.dokdok.book.service;

import com.dokdok.book.entity.Book;
import com.dokdok.book.entity.BookReview;
import com.dokdok.book.exception.BookErrorCode;
import com.dokdok.book.exception.BookException;
import com.dokdok.book.repository.BookRepository;
import com.dokdok.book.repository.BookReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BookValidator {

    private final BookRepository bookRepository;
    private final BookReviewRepository bookReviewRepository;

    // 책 존재 여부를 검증하고 엔티티를 반환합니다.
    public Book validateAndGetBook(Long bookId) {
        return bookRepository.findById(bookId)
                .orElseThrow(() -> new BookException(BookErrorCode.BOOK_NOT_FOUND));
    }

    // 동일 사용자/책 리뷰 중복 생성을 막습니다.
    public void validateReviewNotExists(Long bookId, Long userId) {
        if (bookReviewRepository.existsByBookIdAndUserId(bookId, userId)) {
            throw new BookException(BookErrorCode.BOOK_REVIEW_ALREADY_EXISTS);
        }
    }

    // 사용자의 책 리뷰 존재 여부를 검증하고 반환합니다.
    public BookReview validateAndGetReview(Long bookId, Long userId) {
        return bookReviewRepository.findByBookIdAndUserId(bookId, userId)
                .orElseThrow(() -> new BookException(BookErrorCode.BOOK_REVIEW_NOT_FOUND));
    }
}

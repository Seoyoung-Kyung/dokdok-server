package com.dokdok.book.service;

import com.dokdok.book.dto.request.BookReviewRequest;
import com.dokdok.book.dto.response.BookReviewResponse;
import com.dokdok.book.entity.Book;
import com.dokdok.book.entity.BookReview;
import com.dokdok.book.exception.BookErrorCode;
import com.dokdok.book.exception.BookException;
import com.dokdok.book.repository.BookRepository;
import com.dokdok.book.repository.BookReviewRepository;
import com.dokdok.global.util.SecurityUtil;
import com.dokdok.keyword.entity.Keyword;
import com.dokdok.keyword.repository.KeywordRepository;
import com.dokdok.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BookReviewService {

    private final BookRepository bookRepository;
    private final BookReviewRepository bookReviewRepository;
    private final KeywordRepository keywordRepository;

    @Transactional
    public BookReviewResponse createReview(Long bookId, BookReviewRequest request) {
        Long userId = SecurityUtil.getCurrentUserId();

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new BookException(BookErrorCode.BOOK_NOT_FOUND));

        Keyword keyword = keywordRepository.findById(request.keywordId())
                .orElseThrow(() -> new BookException(BookErrorCode.KEYWORD_NOT_FOUND));

        if (bookReviewRepository.existsByBookIdAndUserId(bookId, userId)) {
            throw new BookException(BookErrorCode.BOOK_REVIEW_ALREADY_EXISTS);
        }

        User user = SecurityUtil.getCurrentUserEntity();

        BookReview review = BookReview.builder()
                .book(book)
                .user(user)
                .rating(request.rating())
                .keyword(keyword)
                .build();

        return BookReviewResponse.from(bookReviewRepository.save(review));
    }

}

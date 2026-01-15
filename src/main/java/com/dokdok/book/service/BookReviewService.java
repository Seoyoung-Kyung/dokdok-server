package com.dokdok.book.service;

import com.dokdok.book.dto.request.BookReviewRequest;
import com.dokdok.book.dto.response.BookReviewResponse;
import com.dokdok.book.entity.Book;
import com.dokdok.book.entity.BookReview;
import com.dokdok.book.repository.BookReviewRepository;
import com.dokdok.global.util.SecurityUtil;
import com.dokdok.keyword.entity.Keyword;
import com.dokdok.keyword.service.KeywordValidator;
import com.dokdok.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BookReviewService {

    private final BookReviewRepository bookReviewRepository;
    private final BookValidator bookValidator;
    private final KeywordValidator keywordValidator;

    @Transactional
    public BookReviewResponse createReview(Long bookId, BookReviewRequest request) {
        Long userId = SecurityUtil.getCurrentUserId();

        Book book = bookValidator.validateAndGetBook(bookId);

        Keyword keyword = keywordValidator.validateAndGetSelectableKeyword(
                request.keywordId()
        );

        bookValidator.validateReviewNotExists(bookId, userId);

        User user = SecurityUtil.getCurrentUserEntity();

        BookReview review = BookReview.create(book, user, request.rating(), keyword);

        return BookReviewResponse.from(bookReviewRepository.save(review));
    }

}

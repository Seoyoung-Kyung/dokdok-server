package com.dokdok.book.service;

import com.dokdok.book.dto.request.BookReviewRequest;
import com.dokdok.book.dto.response.BookReviewResponse;
import com.dokdok.book.entity.Book;
import com.dokdok.book.entity.BookReview;
import com.dokdok.book.exception.BookErrorCode;
import com.dokdok.book.exception.BookException;
import com.dokdok.book.repository.BookReviewRepository;
import com.dokdok.global.exception.GlobalErrorCode;
import com.dokdok.global.exception.GlobalException;
import com.dokdok.global.util.SecurityUtil;
import com.dokdok.keyword.entity.Keyword;
import com.dokdok.keyword.service.KeywordValidator;
import com.dokdok.user.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookReviewServiceTest {

    @Mock
    private BookReviewRepository bookReviewRepository;

    @Mock
    private BookValidator bookValidator;

    @Mock
    private KeywordValidator keywordValidator;

    @InjectMocks
    private BookReviewService bookReviewService;

    @Test
    @DisplayName("책 리뷰를 정상적으로 생성한다")
    void createReview_success() {
        Book book = Book.builder().id(1L).build();
        Keyword keyword = Keyword.builder().id(2L).build();
        User user = User.builder().id(3L).build();
        BookReviewRequest request = new BookReviewRequest(new BigDecimal("4.5"), 2L);

        BookReview saved = BookReview.builder()
                .id(10L)
                .book(book)
                .user(user)
                .rating(new BigDecimal("4.5"))
                .keyword(keyword)
                .build();

        try (MockedStatic<SecurityUtil> securityUtilMock = org.mockito.Mockito.mockStatic(SecurityUtil.class)) {
            securityUtilMock.when(SecurityUtil::getCurrentUserId).thenReturn(3L);
            securityUtilMock.when(SecurityUtil::getCurrentUserEntity).thenReturn(user);

            doNothing().when(bookValidator).validateReviewNotExists(1L, 3L);
            when(bookValidator.validateAndGetBook(1L)).thenReturn(book);
            when(keywordValidator.validateAndGetSelectableKeyword(2L)).thenReturn(keyword);
            when(bookReviewRepository.save(any(BookReview.class))).thenReturn(saved);

            BookReviewResponse response = bookReviewService.createReview(1L, request);

            assertThat(response.reviewId()).isEqualTo(10L);
            assertThat(response.bookId()).isEqualTo(1L);
            assertThat(response.userId()).isEqualTo(3L);
            assertThat(response.rating()).isEqualTo(new BigDecimal("4.5"));
            assertThat(response.keywordId()).isEqualTo(2L);

            verify(bookValidator).validateReviewNotExists(1L, 3L);
            verify(bookValidator).validateAndGetBook(1L);
            verify(keywordValidator).validateAndGetSelectableKeyword(2L);
            verify(bookReviewRepository).save(any(BookReview.class));
        }
    }

    @Test
    @DisplayName("리뷰가 이미 존재하면 예외가 발생한다")
    void createReview_throwsWhenAlreadyExists() {
        BookReviewRequest request = new BookReviewRequest(new BigDecimal("4.5"), 2L);

        try (MockedStatic<SecurityUtil> securityUtilMock = org.mockito.Mockito.mockStatic(SecurityUtil.class)) {
            securityUtilMock.when(SecurityUtil::getCurrentUserId).thenReturn(3L);

            doThrow(new BookException(BookErrorCode.BOOK_REVIEW_ALREADY_EXISTS))
                    .when(bookValidator).validateReviewNotExists(1L, 3L);

            assertThatThrownBy(() -> bookReviewService.createReview(1L, request))
                    .isInstanceOf(BookException.class)
                    .hasFieldOrPropertyWithValue("errorCode", BookErrorCode.BOOK_REVIEW_ALREADY_EXISTS);

            verify(bookValidator).validateAndGetBook(1L);
            verify(keywordValidator).validateAndGetSelectableKeyword(2L);
            verify(bookReviewRepository, never()).save(any(BookReview.class));
        }
    }

    @Test
    @DisplayName("책이 없으면 예외가 발생한다")
    void createReview_throwsWhenBookMissing() {
        BookReviewRequest request = new BookReviewRequest(new BigDecimal("4.5"), 2L);

        try (MockedStatic<SecurityUtil> securityUtilMock = org.mockito.Mockito.mockStatic(SecurityUtil.class)) {
            securityUtilMock.when(SecurityUtil::getCurrentUserId).thenReturn(3L);

            doThrow(new BookException(BookErrorCode.BOOK_NOT_FOUND))
                    .when(bookValidator).validateAndGetBook(1L);

            assertThatThrownBy(() -> bookReviewService.createReview(1L, request))
                    .isInstanceOf(BookException.class)
                    .hasFieldOrPropertyWithValue("errorCode", BookErrorCode.BOOK_NOT_FOUND);

            verify(keywordValidator, never()).validateAndGetSelectableKeyword(any());
            verify(bookValidator, never()).validateReviewNotExists(any(), any());
            verify(bookReviewRepository, never()).save(any(BookReview.class));
        }
    }

    @Test
    @DisplayName("선택 불가 키워드를 요청하면 예외가 발생한다")
    void createReview_throwsWhenKeywordNotSelectable() {
        BookReviewRequest request = new BookReviewRequest(new BigDecimal("4.5"), 2L);
        Book book = Book.builder().id(1L).build();

        try (MockedStatic<SecurityUtil> securityUtilMock = org.mockito.Mockito.mockStatic(SecurityUtil.class)) {
            securityUtilMock.when(SecurityUtil::getCurrentUserId).thenReturn(3L);

            when(bookValidator.validateAndGetBook(1L)).thenReturn(book);
            doThrow(new BookException(BookErrorCode.KEYWORD_NOT_SELECTABLE))
                    .when(keywordValidator).validateAndGetSelectableKeyword(2L);

            assertThatThrownBy(() -> bookReviewService.createReview(1L, request))
                    .isInstanceOf(BookException.class)
                    .hasFieldOrPropertyWithValue("errorCode", BookErrorCode.KEYWORD_NOT_SELECTABLE);

            verify(bookValidator, never()).validateReviewNotExists(any(), any());
            verify(bookReviewRepository, never()).save(any(BookReview.class));
        }
    }

    @Test
    @DisplayName("인증 정보가 없으면 예외가 발생한다")
    void createReview_throwsWhenUnauthenticated() {
        BookReviewRequest request = new BookReviewRequest(new BigDecimal("4.5"), 2L);

        try (MockedStatic<SecurityUtil> securityUtilMock = org.mockito.Mockito.mockStatic(SecurityUtil.class)) {
            securityUtilMock.when(SecurityUtil::getCurrentUserId)
                    .thenThrow(new GlobalException(GlobalErrorCode.UNAUTHORIZED));

            assertThatThrownBy(() -> bookReviewService.createReview(1L, request))
                    .isInstanceOf(GlobalException.class)
                    .hasFieldOrPropertyWithValue("errorCode", GlobalErrorCode.UNAUTHORIZED);

            verify(bookValidator, never()).validateReviewNotExists(any(), any());
            verify(bookReviewRepository, never()).save(any(BookReview.class));
        }
    }

    @Test
    @DisplayName("내 책 리뷰를 정상적으로 조회한다")
    void getMyReview_success() {
        Book book = Book.builder().id(1L).build();
        Keyword keyword = Keyword.builder().id(2L).build();
        User user = User.builder().id(3L).build();
        BookReview review = BookReview.builder()
                .id(10L)
                .book(book)
                .user(user)
                .rating(new BigDecimal("4.5"))
                .keyword(keyword)
                .build();

        try (MockedStatic<SecurityUtil> securityUtilMock = org.mockito.Mockito.mockStatic(SecurityUtil.class)) {
            securityUtilMock.when(SecurityUtil::getCurrentUserId).thenReturn(3L);

            when(bookValidator.validateAndGetReview(1L, 3L))
                    .thenReturn(review);

            BookReviewResponse response = bookReviewService.getMyReview(1L);

            assertThat(response.reviewId()).isEqualTo(10L);
            assertThat(response.bookId()).isEqualTo(1L);
            assertThat(response.userId()).isEqualTo(3L);
            assertThat(response.rating()).isEqualTo(new BigDecimal("4.5"));
            assertThat(response.keywordId()).isEqualTo(2L);
        }
    }

    @Test
    @DisplayName("내 책 리뷰가 없으면 예외가 발생한다")
    void getMyReview_throwsWhenReviewMissing() {
        try (MockedStatic<SecurityUtil> securityUtilMock = org.mockito.Mockito.mockStatic(SecurityUtil.class)) {
            securityUtilMock.when(SecurityUtil::getCurrentUserId).thenReturn(3L);

            when(bookValidator.validateAndGetReview(1L, 3L))
                    .thenThrow(new BookException(BookErrorCode.BOOK_REVIEW_NOT_FOUND));

            assertThatThrownBy(() -> bookReviewService.getMyReview(1L))
                    .isInstanceOf(BookException.class)
                    .hasFieldOrPropertyWithValue("errorCode", BookErrorCode.BOOK_REVIEW_NOT_FOUND);
        }
    }
}

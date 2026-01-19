package com.dokdok.book.service;

import com.dokdok.book.dto.request.BookReviewRequest;
import com.dokdok.book.dto.response.BookReviewResponse;
import com.dokdok.book.entity.Book;
import com.dokdok.book.entity.BookReview;
import com.dokdok.book.entity.BookReviewKeyword;
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
import java.util.List;

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
        Keyword keywordSecond = Keyword.builder().id(4L).build();
        User user = User.builder().id(3L).build();
        BookReviewRequest request = new BookReviewRequest(new BigDecimal("4.5"), List.of(2L, 4L));

        BookReview saved = BookReview.builder()
                .id(10L)
                .book(book)
                .user(user)
                .rating(new BigDecimal("4.5"))
                .keywords(List.of(
                        BookReviewKeyword.builder().keyword(keyword).build(),
                        BookReviewKeyword.builder().keyword(keywordSecond).build()
                ))
                .build();

        try (MockedStatic<SecurityUtil> securityUtilMock = org.mockito.Mockito.mockStatic(SecurityUtil.class)) {
            securityUtilMock.when(SecurityUtil::getCurrentUserId).thenReturn(3L);
            securityUtilMock.when(SecurityUtil::getCurrentUserEntity).thenReturn(user);

            doNothing().when(bookValidator).validateReviewNotExists(1L, 3L);
            when(bookValidator.validateAndGetBook(1L)).thenReturn(book);
            when(keywordValidator.validateAndGetSelectableKeyword(2L)).thenReturn(keyword);
            when(keywordValidator.validateAndGetSelectableKeyword(4L)).thenReturn(keywordSecond);
            when(bookReviewRepository.save(any(BookReview.class))).thenReturn(saved);

            BookReviewResponse response = bookReviewService.createReview(1L, request);

            assertThat(response.reviewId()).isEqualTo(10L);
            assertThat(response.bookId()).isEqualTo(1L);
            assertThat(response.userId()).isEqualTo(3L);
            assertThat(response.rating()).isEqualTo(new BigDecimal("4.5"));
            assertThat(response.keywordIds()).containsExactly(2L, 4L);

            verify(bookValidator).validateReviewNotExists(1L, 3L);
            verify(bookValidator).validateAndGetBook(1L);
            verify(keywordValidator).validateAndGetSelectableKeyword(2L);
            verify(keywordValidator).validateAndGetSelectableKeyword(4L);
            verify(bookReviewRepository).save(any(BookReview.class));
        }
    }

    @Test
    @DisplayName("책이 없으면 예외가 발생한다")
    void createReview_throwsWhenBookMissing() {
        BookReviewRequest request = new BookReviewRequest(new BigDecimal("4.5"), List.of(2L));

        try (MockedStatic<SecurityUtil> securityUtilMock = org.mockito.Mockito.mockStatic(SecurityUtil.class)) {
            securityUtilMock.when(SecurityUtil::getCurrentUserId).thenReturn(3L);

            doThrow(new BookException(BookErrorCode.BOOK_NOT_FOUND))
                    .when(bookValidator).validateAndGetBook(1L);

            assertThatThrownBy(() -> bookReviewService.createReview(1L, request))
                    .isInstanceOf(BookException.class)
                    .hasFieldOrPropertyWithValue("errorCode", BookErrorCode.BOOK_NOT_FOUND);

            verify(keywordValidator, never()).validateAndGetSelectableKeyword(any());
            verify(bookReviewRepository, never()).save(any(BookReview.class));
        }
    }

    @Test
    @DisplayName("선택 불가 키워드를 요청하면 예외가 발생한다")
    void createReview_throwsWhenKeywordNotSelectable() {
        BookReviewRequest request = new BookReviewRequest(new BigDecimal("4.5"), List.of(2L));
        Book book = Book.builder().id(1L).build();

        try (MockedStatic<SecurityUtil> securityUtilMock = org.mockito.Mockito.mockStatic(SecurityUtil.class)) {
            securityUtilMock.when(SecurityUtil::getCurrentUserId).thenReturn(3L);

            when(bookValidator.validateAndGetBook(1L)).thenReturn(book);
            doThrow(new BookException(BookErrorCode.KEYWORD_NOT_SELECTABLE))
                    .when(keywordValidator).validateAndGetSelectableKeyword(2L);

            assertThatThrownBy(() -> bookReviewService.createReview(1L, request))
                    .isInstanceOf(BookException.class)
                    .hasFieldOrPropertyWithValue("errorCode", BookErrorCode.KEYWORD_NOT_SELECTABLE);

            verify(bookReviewRepository, never()).save(any(BookReview.class));
        }
    }

    @Test
    @DisplayName("인증 정보가 없으면 예외가 발생한다")
    void createReview_throwsWhenUnauthenticated() {
        BookReviewRequest request = new BookReviewRequest(new BigDecimal("4.5"), List.of(2L));

        try (MockedStatic<SecurityUtil> securityUtilMock = org.mockito.Mockito.mockStatic(SecurityUtil.class)) {
            securityUtilMock.when(SecurityUtil::getCurrentUserId)
                    .thenThrow(new GlobalException(GlobalErrorCode.UNAUTHORIZED));

            assertThatThrownBy(() -> bookReviewService.createReview(1L, request))
                    .isInstanceOf(GlobalException.class)
                    .hasFieldOrPropertyWithValue("errorCode", GlobalErrorCode.UNAUTHORIZED);

            verify(bookReviewRepository, never()).save(any(BookReview.class));
        }
    }

    @Test
    @DisplayName("별점이 유효하지 않으면 생성 시 예외가 발생한다")
    void createReview_throwsWhenInvalidRating() {
        BookReviewRequest request = new BookReviewRequest(new BigDecimal("4.7"), List.of(2L));

        try (MockedStatic<SecurityUtil> securityUtilMock = org.mockito.Mockito.mockStatic(SecurityUtil.class)) {
            securityUtilMock.when(SecurityUtil::getCurrentUserId).thenReturn(3L);

            doThrow(new BookException(BookErrorCode.BOOK_REVIEW_INVALID_RATING))
                    .when(bookValidator).validateRating(request.rating());

            assertThatThrownBy(() -> bookReviewService.createReview(1L, request))
                    .isInstanceOf(BookException.class)
                    .hasFieldOrPropertyWithValue("errorCode", BookErrorCode.BOOK_REVIEW_INVALID_RATING);

            verify(bookValidator, never()).validateAndGetBook(any());
            verify(keywordValidator, never()).validateAndGetSelectableKeyword(any());
            verify(bookReviewRepository, never()).save(any(BookReview.class));
        }
    }

    @Test
    @DisplayName("리뷰가 이미 존재하면 생성 시 예외가 발생한다")
    void createReview_throwsWhenAlreadyExists() {
        Book book = Book.builder().id(1L).build();
        Keyword keyword = Keyword.builder().id(2L).build();
        User user = User.builder().id(3L).build();
        BookReviewRequest request = new BookReviewRequest(new BigDecimal("4.5"), List.of(2L));

        try (MockedStatic<SecurityUtil> securityUtilMock = org.mockito.Mockito.mockStatic(SecurityUtil.class)) {
            securityUtilMock.when(SecurityUtil::getCurrentUserId).thenReturn(3L);
            securityUtilMock.when(SecurityUtil::getCurrentUserEntity).thenReturn(user);

            when(bookValidator.validateAndGetBook(1L)).thenReturn(book);
            when(keywordValidator.validateAndGetSelectableKeyword(2L)).thenReturn(keyword);
            doThrow(new BookException(BookErrorCode.BOOK_REVIEW_ALREADY_EXISTS))
                    .when(bookValidator).validateReviewNotExists(1L, 3L);

            assertThatThrownBy(() -> bookReviewService.createReview(1L, request))
                    .isInstanceOf(BookException.class)
                    .hasFieldOrPropertyWithValue("errorCode", BookErrorCode.BOOK_REVIEW_ALREADY_EXISTS);

            verify(bookReviewRepository, never()).save(any(BookReview.class));
        }
    }

    @Test
    @DisplayName("내 책 리뷰를 정상적으로 조회한다")
    void getMyReview_success() {
        Book book = Book.builder().id(1L).build();
        Keyword keyword = Keyword.builder().id(2L).build();
        Keyword keywordSecond = Keyword.builder().id(4L).build();
        User user = User.builder().id(3L).build();
        BookReview review = BookReview.builder()
                .id(10L)
                .book(book)
                .user(user)
                .rating(new BigDecimal("4.5"))
                .keywords(List.of(
                        BookReviewKeyword.builder().keyword(keyword).build(),
                        BookReviewKeyword.builder().keyword(keywordSecond).build()
                ))
                .build();

        try (MockedStatic<SecurityUtil> securityUtilMock = org.mockito.Mockito.mockStatic(SecurityUtil.class)) {
            securityUtilMock.when(SecurityUtil::getCurrentUserId).thenReturn(3L);

            when(bookValidator.validateAndGetActiveReview(1L, 3L))
                    .thenReturn(review);

            BookReviewResponse response = bookReviewService.getMyReview(1L);

            assertThat(response.reviewId()).isEqualTo(10L);
            assertThat(response.bookId()).isEqualTo(1L);
            assertThat(response.userId()).isEqualTo(3L);
            assertThat(response.rating()).isEqualTo(new BigDecimal("4.5"));
            assertThat(response.keywordIds()).containsExactly(2L, 4L);
        }
    }

    @Test
    @DisplayName("내 책 리뷰가 없으면 예외가 발생한다")
    void getMyReview_throwsWhenReviewMissing() {
        try (MockedStatic<SecurityUtil> securityUtilMock = org.mockito.Mockito.mockStatic(SecurityUtil.class)) {
            securityUtilMock.when(SecurityUtil::getCurrentUserId).thenReturn(3L);

            when(bookValidator.validateAndGetActiveReview(1L, 3L))
                    .thenThrow(new BookException(BookErrorCode.BOOK_REVIEW_NOT_FOUND));

            assertThatThrownBy(() -> bookReviewService.getMyReview(1L))
                    .isInstanceOf(BookException.class)
                    .hasFieldOrPropertyWithValue("errorCode", BookErrorCode.BOOK_REVIEW_NOT_FOUND);
        }
    }

    @Test
    @DisplayName("내 책 리뷰를 수정한다")
    void updateMyReview_success() {
        Book book = Book.builder().id(1L).build();
        Keyword keyword = Keyword.builder().id(2L).build();
        Keyword newKeyword = Keyword.builder().id(5L).build();
        Keyword newKeywordSecond = Keyword.builder().id(8L).build();
        User user = User.builder().id(3L).build();
        BookReview review = BookReview.builder()
                .id(10L)
                .book(book)
                .user(user)
                .rating(new BigDecimal("4.5"))
                .keywords(List.of(
                        BookReviewKeyword.builder().keyword(keyword).build()
                ))
                .build();
        BookReviewRequest request = new BookReviewRequest(new BigDecimal("3.5"), List.of(5L, 8L));

        try (MockedStatic<SecurityUtil> securityUtilMock = org.mockito.Mockito.mockStatic(SecurityUtil.class)) {
            securityUtilMock.when(SecurityUtil::getCurrentUserId).thenReturn(3L);

            when(bookValidator.validateAndGetReviewForUpdate(1L, 3L)).thenReturn(review);
            when(keywordValidator.validateAndGetSelectableKeyword(5L)).thenReturn(newKeyword);
            when(keywordValidator.validateAndGetSelectableKeyword(8L)).thenReturn(newKeywordSecond);

            BookReviewResponse response = bookReviewService.updateMyReview(1L, request);

            assertThat(review.getRating()).isEqualTo(new BigDecimal("3.5"));
            assertThat(review.getKeywords()).hasSize(2);
            assertThat(response.keywordIds()).containsExactly(5L, 8L);
            assertThat(response.reviewId()).isEqualTo(10L);
        }
    }

    @Test
    @DisplayName("내 책 리뷰가 없으면 수정 시 예외가 발생한다")
    void updateMyReview_throwsWhenReviewMissing() {
        BookReviewRequest request = new BookReviewRequest(new BigDecimal("3.5"), List.of(5L));

        try (MockedStatic<SecurityUtil> securityUtilMock = org.mockito.Mockito.mockStatic(SecurityUtil.class)) {
            securityUtilMock.when(SecurityUtil::getCurrentUserId).thenReturn(3L);

            when(bookValidator.validateAndGetReviewForUpdate(1L, 3L))
                    .thenThrow(new BookException(BookErrorCode.BOOK_REVIEW_NOT_FOUND));

            assertThatThrownBy(() -> bookReviewService.updateMyReview(1L, request))
                    .isInstanceOf(BookException.class)
                    .hasFieldOrPropertyWithValue("errorCode", BookErrorCode.BOOK_REVIEW_NOT_FOUND);
        }
    }

    @Test
    @DisplayName("선택 불가 키워드면 수정 시 예외가 발생한다")
    void updateMyReview_throwsWhenKeywordNotSelectable() {
        Book book = Book.builder().id(1L).build();
        Keyword keyword = Keyword.builder().id(2L).build();
        User user = User.builder().id(3L).build();
        BookReview review = BookReview.builder()
                .id(10L)
                .book(book)
                .user(user)
                .rating(new BigDecimal("4.5"))
                .keywords(List.of(
                        BookReviewKeyword.builder().keyword(keyword).build()
                ))
                .build();
        BookReviewRequest request = new BookReviewRequest(new BigDecimal("3.5"), List.of(5L));

        try (MockedStatic<SecurityUtil> securityUtilMock = org.mockito.Mockito.mockStatic(SecurityUtil.class)) {
            securityUtilMock.when(SecurityUtil::getCurrentUserId).thenReturn(3L);

            when(bookValidator.validateAndGetReviewForUpdate(1L, 3L)).thenReturn(review);
            when(keywordValidator.validateAndGetSelectableKeyword(5L))
                    .thenThrow(new BookException(BookErrorCode.KEYWORD_NOT_SELECTABLE));

            assertThatThrownBy(() -> bookReviewService.updateMyReview(1L, request))
                    .isInstanceOf(BookException.class)
                    .hasFieldOrPropertyWithValue("errorCode", BookErrorCode.KEYWORD_NOT_SELECTABLE);
        }
    }

    @Test
    @DisplayName("삭제된 리뷰면 수정 시 예외가 발생한다")
    void updateMyReview_throwsWhenReviewDeleted() {
        BookReviewRequest request = new BookReviewRequest(new BigDecimal("3.5"), List.of(5L));

        try (MockedStatic<SecurityUtil> securityUtilMock = org.mockito.Mockito.mockStatic(SecurityUtil.class)) {
            securityUtilMock.when(SecurityUtil::getCurrentUserId).thenReturn(3L);

            when(bookValidator.validateAndGetReviewForUpdate(1L, 3L))
                    .thenThrow(new BookException(BookErrorCode.BOOK_REVIEW_NOT_FOUND));

            assertThatThrownBy(() -> bookReviewService.updateMyReview(1L, request))
                    .isInstanceOf(BookException.class)
                    .hasFieldOrPropertyWithValue("errorCode", BookErrorCode.BOOK_REVIEW_NOT_FOUND);
        }
    }

    @Test
    @DisplayName("별점이 유효하지 않으면 수정 시 예외가 발생한다")
    void updateMyReview_throwsWhenInvalidRating() {
        BookReviewRequest request = new BookReviewRequest(new BigDecimal("0.3"), List.of(2L));

        try (MockedStatic<SecurityUtil> securityUtilMock = org.mockito.Mockito.mockStatic(SecurityUtil.class)) {
            securityUtilMock.when(SecurityUtil::getCurrentUserId).thenReturn(3L);

            doThrow(new BookException(BookErrorCode.BOOK_REVIEW_INVALID_RATING))
                    .when(bookValidator).validateRating(request.rating());

            assertThatThrownBy(() -> bookReviewService.updateMyReview(1L, request))
                    .isInstanceOf(BookException.class)
                    .hasFieldOrPropertyWithValue("errorCode", BookErrorCode.BOOK_REVIEW_INVALID_RATING);

            verify(bookValidator, never()).validateAndGetReviewForUpdate(any(), any());
            verify(keywordValidator, never()).validateAndGetSelectableKeyword(any());
        }
    }

    @Test
    @DisplayName("내 책 리뷰를 삭제한다")
    void deleteMyReview_success() {
        Book book = Book.builder().id(1L).build();
        Keyword keyword = Keyword.builder().id(2L).build();
        User user = User.builder().id(3L).build();
        BookReview review = BookReview.builder()
                .id(10L)
                .book(book)
                .user(user)
                .rating(new BigDecimal("4.5"))
                .keywords(List.of(
                        BookReviewKeyword.builder().keyword(keyword).build()
                ))
                .build();

        try (MockedStatic<SecurityUtil> securityUtilMock = org.mockito.Mockito.mockStatic(SecurityUtil.class)) {
            securityUtilMock.when(SecurityUtil::getCurrentUserId).thenReturn(3L);

            when(bookValidator.validateAndGetReviewForUpdate(1L, 3L)).thenReturn(review);

            bookReviewService.deleteMyReview(1L);

            assertThat(review.isDeleted()).isTrue();
        }
    }

    @Test
    @DisplayName("삭제된 리뷰를 다시 삭제하면 예외가 발생한다")
    void deleteMyReview_throwsWhenReviewDeleted() {
        try (MockedStatic<SecurityUtil> securityUtilMock = org.mockito.Mockito.mockStatic(SecurityUtil.class)) {
            securityUtilMock.when(SecurityUtil::getCurrentUserId).thenReturn(3L);

            when(bookValidator.validateAndGetReviewForUpdate(1L, 3L))
                    .thenThrow(new BookException(BookErrorCode.BOOK_REVIEW_NOT_FOUND));

            assertThatThrownBy(() -> bookReviewService.deleteMyReview(1L))
                    .isInstanceOf(BookException.class)
                    .hasFieldOrPropertyWithValue("errorCode", BookErrorCode.BOOK_REVIEW_NOT_FOUND);
        }
    }
}

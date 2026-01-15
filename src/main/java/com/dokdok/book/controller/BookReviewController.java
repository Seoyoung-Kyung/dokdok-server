package com.dokdok.book.controller;

import com.dokdok.book.api.BookReviewApi;
import com.dokdok.book.dto.request.BookReviewRequest;
import com.dokdok.book.dto.response.BookReviewResponse;
import com.dokdok.book.service.BookReviewService;
import com.dokdok.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/book")
public class BookReviewController implements BookReviewApi {

    private final BookReviewService bookReviewService;

    @Override
    public ResponseEntity<ApiResponse<BookReviewResponse>> createReview(
            Long bookId,
            BookReviewRequest request
    ) {
        BookReviewResponse response = bookReviewService.createReview(bookId, request);
        return ApiResponse.created(response, "책 리뷰가 저장되었습니다.");
    }

    @Override
    public ResponseEntity<ApiResponse<BookReviewResponse>> getMyReview(
            Long bookId
    ) {
        BookReviewResponse response = bookReviewService.getMyReview(bookId);
        return ApiResponse.success(response, "책 리뷰 조회가 완료되었습니다.");
    }

    @Override
    public ResponseEntity<ApiResponse<BookReviewResponse>> updateMyReview(
            Long bookId,
            BookReviewRequest request
    ) {
        BookReviewResponse response = bookReviewService.updateMyReview(bookId, request);
        return ApiResponse.success(response, "책 리뷰가 수정되었습니다.");
    }

    @Override
    public ResponseEntity<ApiResponse<Void>> deleteMyReview(
            Long bookId
    ) {
        bookReviewService.deleteMyReview(bookId);
        return ApiResponse.deleted("책 리뷰가 삭제되었습니다.");
    }
}

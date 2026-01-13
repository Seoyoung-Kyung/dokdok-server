package com.dokdok.book.controller;

import com.dokdok.book.api.BookApi;
import com.dokdok.book.dto.response.KakaoBookResponse;
import com.dokdok.book.service.BookService;
import com.dokdok.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/book")
public class BookController implements BookApi {
    private final BookService bookService;

    @Override
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<KakaoBookResponse>> searchBook(@RequestParam String title) {
        return ApiResponse.success(bookService.searchBook(title), "책 정보 조회 성공");
    }
}
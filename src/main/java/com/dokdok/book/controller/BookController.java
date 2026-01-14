package com.dokdok.book.controller;

import com.dokdok.book.api.BookApi;
import com.dokdok.book.dto.request.BookCreateRequest;
import com.dokdok.book.dto.response.KakaoBookResponse;
import com.dokdok.book.dto.response.PersonalBookCreateResponse;
import com.dokdok.book.service.BookService;
import com.dokdok.book.service.PersonalBookService;
import com.dokdok.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/book")
public class BookController implements BookApi {

    private final BookService bookService;
    private final PersonalBookService personalBookService;

    @Override
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<KakaoBookResponse>> searchBook(@RequestParam String query) {
        return ApiResponse.success(bookService.searchBook(query), "책 정보 조회 성공");
    }

    @Override
    @PostMapping
    public ResponseEntity<ApiResponse<PersonalBookCreateResponse>> createBook(@Valid @RequestBody BookCreateRequest bookCreateRequest) {
        PersonalBookCreateResponse book = personalBookService.createBook(bookCreateRequest);
        return ApiResponse.created(book, "내 책장에 책 등록 성공");
    }
}
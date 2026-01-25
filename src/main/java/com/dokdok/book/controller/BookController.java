package com.dokdok.book.controller;

import com.dokdok.book.api.BookApi;
import com.dokdok.book.dto.request.BookCreateRequest;
import com.dokdok.book.dto.response.*;
import com.dokdok.book.entity.BookReadingStatus;
import com.dokdok.book.service.BookService;
import com.dokdok.book.service.PersonalBookService;
import com.dokdok.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    @Override
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<PersonalBookListResponse>>> getMyBooks(BookReadingStatus readingStatus, Long gatheringId, Pageable pageable) {
        Page<PersonalBookListResponse> personalBookList = personalBookService.getPersonalBookList(readingStatus, gatheringId, pageable);
        PageResponse<PersonalBookListResponse> response = PageResponse.from(personalBookList);
        return ApiResponse.success(response, "책 리스트 조회 성공");
    }

    @Override
    @GetMapping("/{bookId}")
    public ResponseEntity<ApiResponse<PersonalBookDetailResponse>> getMyBook(@PathVariable Long bookId) {
        PersonalBookDetailResponse personalBook = personalBookService.getPersonalBook(bookId);
        return ApiResponse.success(personalBook, "책 상세 정보 조회 성공");
    }

    @Override
    @DeleteMapping("/{bookId}")
    public ResponseEntity<ApiResponse<Void>> deleteMyBook(@PathVariable Long bookId) {
        personalBookService.deleteBook(bookId);
        return ApiResponse.deleted("책 삭제 성공");
    }


    @Override
    @GetMapping("/reading")
    public ResponseEntity<ApiResponse<PageResponse<PersonalBookListResponse>>> getMyReadingBooks(Pageable pageable) {
        Page<PersonalBookListResponse> personalBookList = personalBookService.getPersonalBookList(BookReadingStatus.READING, null, pageable);
//        Page<PersonalBookListResponse> personalBookList = personalBookService.getPersonalBookReadingList( pageable);
        PageResponse<PersonalBookListResponse> response = PageResponse.from(personalBookList);
        return ApiResponse.success(response, "읽고 있는 책 리스트 조회 성공");
    }
}

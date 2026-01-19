package com.dokdok.book.controller;

import com.dokdok.book.api.BookApi;
import com.dokdok.book.dto.request.BookCreateRequest;
import com.dokdok.book.dto.request.PersonalReadingRecordCreateRequest;
import com.dokdok.book.dto.request.PersonalReadingRecordUpdateRequest;
import com.dokdok.book.dto.response.*;
import com.dokdok.book.service.BookService;
import com.dokdok.book.service.PersonalBookService;
import com.dokdok.book.service.PersonalReadingRecordService;
import com.dokdok.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/book")
public class BookController implements BookApi {

    private final BookService bookService;
    private final PersonalBookService personalBookService;
    private final PersonalReadingRecordService personalReadingRecordService;

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
    public ResponseEntity<ApiResponse<Page<PersonalBookListResponse>>> getMyBooks(Pageable pageable) {
        Page<PersonalBookListResponse> personalBookList = personalBookService.getPersonalBookList(pageable);
        return ApiResponse.success(personalBookList, "책 리스트 조회 성공");
    }

    @Override
    @GetMapping("/{personalBookId}")
    public ResponseEntity<ApiResponse<PersonalBookDetailResponse>> getMyBook(@PathVariable Long personalBookId) {
        PersonalBookDetailResponse personalBook = personalBookService.getPersonalBook(personalBookId);
        return ApiResponse.success(personalBook, "책 상세 정보 조회 성공");
    }

    @Override
    @DeleteMapping("/{personalBookId}")
    public ResponseEntity<ApiResponse<Void>> deleteMyBook(@PathVariable Long personalBookId) {
        personalBookService.deleteBook(personalBookId);
        return ApiResponse.deleted("책 삭제 성공");
    }

    @Override
    @PostMapping("/{personalBookId}")
    public ResponseEntity<ApiResponse<PersonalReadingRecordCreateResponse>> createMyReadingRecord(@PathVariable Long personalBookId, @RequestBody PersonalReadingRecordCreateRequest request) {
        PersonalReadingRecordCreateResponse response = personalReadingRecordService.create(personalBookId, request);
        return ApiResponse.created(response, "기록 등록 성공");
    }

    @Override
    @PatchMapping("/{personalBookId}/records/{recordId}")
    public ResponseEntity<ApiResponse<PersonalReadingRecordCreateResponse>> updateMyReadingRecord(@PathVariable Long personalBookId, @PathVariable Long recordId, @RequestBody PersonalReadingRecordUpdateRequest request) {
        PersonalReadingRecordCreateResponse response = personalReadingRecordService.update(personalBookId, recordId, request);
        return ApiResponse.success(response, "기록 수정 성공");
    }

    @Override
    @DeleteMapping("/{personalBookId}/records/{recordId}")
    public ResponseEntity<ApiResponse<Void>> deleteMyReadingRecord(@PathVariable Long personalBookId, @PathVariable Long recordId) {
        personalReadingRecordService.delete(personalBookId, recordId);
        return ApiResponse.deleted("기록 삭제 성공");
    }

    @Override
    @GetMapping("/{personalBookId}/records")
    public ResponseEntity<ApiResponse<Page<PersonalReadingRecordListResponse>>> getMyReadingRecords(
            @PathVariable Long personalBookId,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {

        Page<PersonalReadingRecordListResponse> records = personalReadingRecordService.getRecords(personalBookId, pageable);
        return ApiResponse.success(records, "기록 조회 성공");

    }
}

package com.dokdok.book.controller;

import com.dokdok.book.api.PersonalBookRecordApi;
import com.dokdok.book.dto.request.PersonalReadingRecordCreateRequest;
import com.dokdok.book.dto.request.PersonalReadingRecordUpdateRequest;
import com.dokdok.book.dto.response.*;
import com.dokdok.book.service.PersonalReadingRecordService;
import com.dokdok.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/book")
public class PersonalBookRecordController implements PersonalBookRecordApi {

    private final PersonalReadingRecordService personalReadingRecordService;

    @Override
    @PostMapping("/{personalBookId}/records")
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
    public ResponseEntity<ApiResponse<CursorPageResponse<PersonalReadingRecordListResponse, ReadingRecordCursor>>> getMyReadingRecords(
            @PathVariable Long personalBookId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            OffsetDateTime cursorCreatedAt,
            @RequestParam(required = false) Long cursorRecordId,
            @RequestParam(required = false) Integer size
    ) {
        CursorPageResponse<PersonalReadingRecordListResponse, ReadingRecordCursor> response =
                personalReadingRecordService.getRecords(personalBookId, cursorCreatedAt, cursorRecordId, size);
        return ApiResponse.success(response, "기록 조회 성공");

    }
}

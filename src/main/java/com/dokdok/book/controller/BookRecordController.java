package com.dokdok.book.controller;

import com.dokdok.book.api.BookRecordApi;
import com.dokdok.retrospective.dto.response.RetrospectiveRecordResponse;
import com.dokdok.global.response.ApiResponse;
import com.dokdok.retrospective.service.PersonalRetrospectiveService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/book/{bookId}")
public class BookRecordController implements BookRecordApi {

    private final PersonalRetrospectiveService retrospectiveService;

    @Override
    @GetMapping(value = "/retrospectives", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<List<RetrospectiveRecordResponse>>> getRetrospectiveRecords(
            @PathVariable Long bookId
    ) {
        List<RetrospectiveRecordResponse> response = retrospectiveService.getRetrospectiveRecords(bookId);

        return ApiResponse.success(response, "해당 책의 개인 회고 목록 조회를 성공했습니다.");
    }
}

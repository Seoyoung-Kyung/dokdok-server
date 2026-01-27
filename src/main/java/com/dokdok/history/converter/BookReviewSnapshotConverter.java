package com.dokdok.history.converter;

import com.dokdok.history.dto.BookReviewSnapshot;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Converter
public class BookReviewSnapshotConverter implements AttributeConverter<BookReviewSnapshot, String> {

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    @Override
    public String convertToDatabaseColumn(BookReviewSnapshot bookReviewSnapshot) {

        try {
            return objectMapper.writeValueAsString(bookReviewSnapshot);
        } catch (JsonProcessingException e) {
            log.error("책 리뷰 히스토리 스냅샷 JSON 변환 중 오류 : {}", e.getMessage());
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public BookReviewSnapshot convertToEntityAttribute(String bookReviewSnapshot) {

        try {
            return objectMapper.readValue(bookReviewSnapshot, BookReviewSnapshot.class);
        } catch (JsonProcessingException e) {
            log.error("책 리뷰 히스토리 스냅샷 JSON 파싱 중 오류 : {}", e.getMessage());
            throw new IllegalArgumentException(e);
        }
    }
}

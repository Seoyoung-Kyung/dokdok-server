package com.dokdok.book.dto.response;

import com.dokdok.book.entity.PersonalReadingRecord;
import com.dokdok.book.entity.RecordType;
import lombok.Builder;

import java.util.Map;

@Builder
public record PersonalReadingRecordListResponse(
        Long recordId,
        RecordType recordType,
        String recordContent,
        Map<String, Object> meta,
        Long bookId
) {
    public static PersonalReadingRecordListResponse from(PersonalReadingRecord record) {
        return PersonalReadingRecordListResponse.builder()
                .recordId(record.getId())
                .recordType(record.getRecordType())
                .recordContent(record.getRecordContent())
                .meta(record.getMeta())
                .bookId(record.getPersonalBook().getBook().getId())
                .build();
    }
}

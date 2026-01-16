package com.dokdok.book.dto.response;

import com.dokdok.book.entity.PersonalReadingRecord;
import com.dokdok.book.entity.RecordType;
import lombok.Builder;

import java.util.Map;

@Builder
public record PersonalReadingRecordCreateResponse(
        RecordType recordType,
        String recordContent,
        Map<String, Object> meta,
        Long personalBookId
) {

    public static PersonalReadingRecordCreateResponse from(PersonalReadingRecord personalReadingRecordEntity) {
        return PersonalReadingRecordCreateResponse.builder()
                .recordType(personalReadingRecordEntity.getRecordType())
                .recordContent(personalReadingRecordEntity.getRecordContent())
                .meta(personalReadingRecordEntity.getMeta())
                .personalBookId(personalReadingRecordEntity.getPersonalBook().getId())
                .build();
    }
}



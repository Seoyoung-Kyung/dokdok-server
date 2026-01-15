package com.dokdok.book.dto.response;

import com.dokdok.book.entity.PersonalReadingRecord;
import lombok.Builder;

@Builder
public record PersonalReadingRecordCreateResponse(
        String recordContent,
        Long personalBookId
) {

    public static PersonalReadingRecordCreateResponse from(PersonalReadingRecord personalReadingRecordEntity) {
        return PersonalReadingRecordCreateResponse.builder()
                .recordContent(personalReadingRecordEntity.getRecordContent())
                .personalBookId(personalReadingRecordEntity.getPersonalBook().getId())
                .build();
    }
}



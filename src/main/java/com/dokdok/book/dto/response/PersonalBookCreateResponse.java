package com.dokdok.book.dto.response;

import com.dokdok.book.entity.BookReadingStatus;
import com.dokdok.book.entity.PersonalBook;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record PersonalBookCreateResponse(
        String isbn,
        BookReadingStatus readingStatus,
        LocalDateTime addedAt
) {

    public static PersonalBookCreateResponse from(PersonalBook personalBookEntity) {
        return PersonalBookCreateResponse.builder()
                .isbn(personalBookEntity.getBook().getIsbn())
                .readingStatus(personalBookEntity.getReadingStatus())
                .addedAt(personalBookEntity.getAddedAt())
                .build();
    }
}

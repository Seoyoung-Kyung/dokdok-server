package com.dokdok.book.dto.response;

import com.dokdok.book.entity.BookReadingStatus;
import com.dokdok.book.entity.PersonalBook;
import lombok.Builder;

@Builder
public record PersonalBookListResponse(
        String title,
        String publisher,
        String authors,
        BookReadingStatus bookReadingStatus
) {
    public static PersonalBookListResponse from(PersonalBook entity) {
        return PersonalBookListResponse.builder()
                .title(entity.getBook().getBookName())
                .publisher(entity.getBook().getPublisher())
                .authors(entity.getBook().getAuthor())
                .bookReadingStatus(entity.getReadingStatus())
                .build();
    }
}

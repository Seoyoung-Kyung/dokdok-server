package com.dokdok.book.dto.response;

import com.dokdok.book.entity.BookReadingStatus;
import com.dokdok.book.entity.PersonalBook;
import lombok.Builder;


@Builder
public record PersonalBookDetailResponse(
        Long personalBookId,
        Long bookId,
        String title,
        String publisher,
        String authors,
        BookReadingStatus bookReadingStatus
) {
    public static PersonalBookDetailResponse from(PersonalBook entity) {
        return PersonalBookDetailResponse.builder()
                .personalBookId(entity.getId())
                .bookId(entity.getBook().getId())
                .title(entity.getBook().getBookName())
                .publisher(entity.getBook().getPublisher())
                .authors(entity.getBook().getAuthor())
                .bookReadingStatus(entity.getReadingStatus())
                .build();
    }
}

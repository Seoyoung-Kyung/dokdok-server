package com.dokdok.book.dto.response;

import com.dokdok.book.entity.BookReadingStatus;
import com.dokdok.book.entity.PersonalBook;
import lombok.Builder;

@Builder
public record PersonalBookListResponse(
        Long personalBookId,
        Long bookId,
        String title,
        String publisher,
        String authors,
        BookReadingStatus bookReadingStatus,
        String thumbnail
) {
    public static PersonalBookListResponse from(PersonalBook entity) {
        // TODO: 참여한 약속(모임) 수 계산 후 응답에 포함한다.
        return PersonalBookListResponse.builder()
                .personalBookId(entity.getId())
                .bookId(entity.getBook().getId())
                .title(entity.getBook().getBookName())
                .publisher(entity.getBook().getPublisher())
                .authors(entity.getBook().getAuthor())
                .bookReadingStatus(entity.getReadingStatus())
                .thumbnail(entity.getBook().getThumbnail())
                .build();
    }
}

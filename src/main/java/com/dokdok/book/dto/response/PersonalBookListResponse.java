package com.dokdok.book.dto.response;

import com.dokdok.book.entity.BookReadingStatus;
import com.dokdok.book.entity.PersonalBook;
import com.dokdok.book.repository.PersonalBookListProjection;
import lombok.Builder;

@Builder
public record PersonalBookListResponse(
        Long personalBookId,
        Long bookId,
        String title,
        String publisher,
        String authors,
        BookReadingStatus bookReadingStatus,
        String thumbnail,
        String gatheringName
) {
    public static PersonalBookListResponse from(PersonalBook entity) {
        return PersonalBookListResponse.builder()
                .personalBookId(entity.getId())
                .bookId(entity.getBook().getId())
                .title(entity.getBook().getBookName())
                .publisher(entity.getBook().getPublisher())
                .authors(entity.getBook().getAuthor())
                .bookReadingStatus(entity.getReadingStatus())
                .thumbnail(entity.getBook().getThumbnail())
                .gatheringName(null)
                .build();
    }

    public static PersonalBookListResponse from(PersonalBookListProjection projection) {
        return PersonalBookListResponse.builder()
                .personalBookId(projection.getPersonalBookId())
                .bookId(projection.getBookId())
                .title(projection.getTitle())
                .publisher(projection.getPublisher())
                .authors(projection.getAuthors())
                .bookReadingStatus(projection.getBookReadingStatus())
                .thumbnail(projection.getThumbnail())
                .gatheringName(projection.getGatheringName())
                .build();
    }
}

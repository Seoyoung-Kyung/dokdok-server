package com.dokdok.book.dto.response;

import com.dokdok.book.entity.Book;
import lombok.Builder;

@Builder
public record BookDetailResponse(
    String title,
    String authors,
    String publisher,
    String isbn,
    String thumbnail
) {
    public static BookDetailResponse from(Book book) {
        return BookDetailResponse.builder()
                .title(book.getBookName())
                .thumbnail(book.getThumbnail())
                .authors(book.getAuthor())
                .isbn(book.getIsbn())
                .publisher(book.getPublisher())
                .build();
    }
}

package com.dokdok.book.dto.request;

import com.dokdok.book.entity.Book;
import lombok.Builder;

@Builder
public record BookCreateRequest(
    String title,
    String authors,
    String publisher,
    String isbn,
    String thumbnail
) {
    public Book of() {
        return Book.builder()
                .bookName(title)
                .author(authors)
                .publisher(publisher)
                .isbn(isbn)
                .thumbnail(thumbnail)
                .build();
    }
}

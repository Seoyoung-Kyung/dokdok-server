package com.dokdok.book.service;

import com.dokdok.book.dto.response.BookDetailResponse;
import com.dokdok.book.dto.response.KakaoBookResponse;
import com.dokdok.book.entity.Book;
import com.dokdok.book.exception.BookException;
import com.dokdok.book.repository.BookRepository;
import com.dokdok.book.webClient.KakaoBookClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static com.dokdok.book.exception.BookErrorCode.BOOK_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class BookService {

    private final KakaoBookClient kakaoBookClient;
    private final BookRepository bookRepository;

    // 외부 API로 책 검색 후 가지고오기 or 저장하기
    public KakaoBookResponse searchBook(String query) {
        return kakaoBookClient.searchBooks(query);
    }

    public BookDetailResponse getBook(String isbn) {
        Book entity = bookRepository.findByIsbn(isbn)
                .orElseThrow(() -> new BookException(BOOK_NOT_FOUND));
        return BookDetailResponse.from(entity);
    }

}

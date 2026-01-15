package com.dokdok.book.service;

import com.dokdok.book.entity.PersonalBook;
import com.dokdok.book.exception.BookErrorCode;
import com.dokdok.book.exception.BookException;
import com.dokdok.book.repository.PersonalBookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BookValidator {

    private final PersonalBookRepository personalBookRepository;

    public PersonalBook validateInBookShelf(Long userId, Long personalBookId) {
        return personalBookRepository.findByUserIdAndId(userId, personalBookId)
                .orElseThrow(() -> new BookException(BookErrorCode.BOOK_NOT_IN_SHELF));
    }

    public void validateDuplicatePersonalBook(Long userId, Long bookId) {
        personalBookRepository.findByUserIdAndBookId(userId, bookId)
                .ifPresent(personalBook ->
                {
                    throw new BookException(BookErrorCode.BOOK_ALREADY_EXISTS);
                });
    }
}

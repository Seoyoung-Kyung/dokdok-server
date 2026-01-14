package com.dokdok.book.service;

import com.dokdok.book.dto.request.BookCreateRequest;
import com.dokdok.book.dto.response.PersonalBookCreateResponse;
import com.dokdok.book.dto.response.PersonalBookDetailResponse;
import com.dokdok.book.dto.response.PersonalBookListResponse;
import com.dokdok.book.entity.Book;
import com.dokdok.book.entity.BookReadingStatus;
import com.dokdok.book.entity.PersonalBook;
import com.dokdok.book.exception.BookErrorCode;
import com.dokdok.book.exception.BookException;
import com.dokdok.book.repository.BookRepository;
import com.dokdok.book.repository.PersonalBookRepository;
import com.dokdok.global.util.SecurityUtil;
import com.dokdok.user.entity.User;
import com.dokdok.user.service.UserValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PersonalBookService {

    private final PersonalBookRepository personalBookRepository;
    private final BookRepository bookRepository;
    private final UserValidator userValidator;

    // 생성
    @Transactional
    public PersonalBookCreateResponse createBook(BookCreateRequest bookCreateRequest) {
        // 사용자 유효성 검증
        User userEntity = userValidator.findUserOrThrow(SecurityUtil.getCurrentUserId());
        // 책 유효성 검증 && 없으면 book entity에 저장
        Book entity = bookRepository.findByIsbn(bookCreateRequest.isbn())
                .orElseGet(() -> bookRepository.save(bookCreateRequest.of()));

        validateDuplicatePersonalBook(userEntity.getId(), entity.getId());
        PersonalBook personalBookEntity = PersonalBook.create(userEntity, entity, BookReadingStatus.READING);

        personalBookRepository.save(personalBookEntity);

        return PersonalBookCreateResponse.from(personalBookEntity);
    }

    // List
    public Page<PersonalBookListResponse> getPersonalBookList(Pageable pageable) {
        User userEntity = userValidator.findUserOrThrow(SecurityUtil.getCurrentUserId());
        Page<PersonalBook> page= personalBookRepository.findByUserId(userEntity.getId(), pageable);


        if (page.isEmpty()) {
            throw new BookException(BookErrorCode.BOOK_NOT_IN_SHELF);
        }

        return page.map(PersonalBookListResponse::from);
    }

    public PersonalBookDetailResponse getPersonalBook(Long bookId) {
        User userEntity = userValidator.findUserOrThrow(SecurityUtil.getCurrentUserId());
        // 책 정보 GET Logic
        PersonalBook entity = personalBookRepository.findByUserIdAndBookId(userEntity.getId(), bookId)
                .orElseThrow(() -> new BookException(BookErrorCode.BOOK_NOT_IN_SHELF));

        return PersonalBookDetailResponse.from(entity);
    }

    private void validateDuplicatePersonalBook(Long userId, Long bookId) {
       personalBookRepository.findByUserIdAndBookId(userId, bookId)
               .ifPresent(personalBook ->
               {
                   throw new BookException(BookErrorCode.BOOK_ALREADY_EXISTS);
               });
   }
}

package com.dokdok.book.service;

import com.dokdok.book.dto.request.PersonalBookCreateRequest;
import com.dokdok.book.dto.response.PersonalBookCreateResponse;
import com.dokdok.book.entity.Book;
import com.dokdok.book.entity.BookReadingStatus;
import com.dokdok.book.entity.PersonalBook;
import com.dokdok.book.exception.BookErrorCode;
import com.dokdok.book.exception.BookException;
import com.dokdok.book.repository.BookRepository;
import com.dokdok.book.repository.PersonalBookRepository;
import com.dokdok.global.util.SecurityUtil;
import com.dokdok.user.entity.User;
import com.dokdok.user.exception.UserErrorCode;
import com.dokdok.user.exception.UserException;
import com.dokdok.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PersonalBookService 테스트")
class PersonalBookServiceTest {

    @InjectMocks
    private PersonalBookService personalBookService;

    @Mock
    private PersonalBookRepository personalBookRepository;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private UserRepository userRepository;

    private MockedStatic<SecurityUtil> securityUtilMock;

    @BeforeEach
    void setUp() {
        securityUtilMock = mockStatic(SecurityUtil.class);
    }

    @AfterEach
    void tearDown() {
        securityUtilMock.close();
    }

    @Test
    @DisplayName("개인 도서 등록 성공 시 READING 상태로 저장")
    void createBook_Success() {
        // given
        Long userId = 1L;
        PersonalBookCreateRequest request = PersonalBookCreateRequest.builder()
                .isbn("9788994757254")
                .build();

        User user = User.builder()
                .id(userId)
                .kakaoId(12345L)
                .nickname("tester")
                .build();

        Book book = Book.builder()
                .id(10L)
                .isbn(request.isbn())
                .bookName("테스트 책")
                .author("작가")
                .publisher("출판사")
                .build();

        securityUtilMock.when(SecurityUtil::getCurrentUserId).thenReturn(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(bookRepository.findByIsbn(request.isbn())).thenReturn(Optional.of(book));

        // when
        PersonalBookCreateResponse response = personalBookService.createBook(request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.isbn()).isEqualTo(request.isbn());
        assertThat(response.readingStatus()).isEqualTo(BookReadingStatus.READING);
        assertThat(response.addedAt()).isNotNull();

        ArgumentCaptor<PersonalBook> personalBookCaptor = ArgumentCaptor.forClass(PersonalBook.class);
        verify(personalBookRepository, times(1)).save(personalBookCaptor.capture());

        PersonalBook savedPersonalBook = personalBookCaptor.getValue();
        assertThat(savedPersonalBook.getUser()).isEqualTo(user);
        assertThat(savedPersonalBook.getBook()).isEqualTo(book);
        assertThat(savedPersonalBook.getReadingStatus()).isEqualTo(BookReadingStatus.READING);
        assertThat(savedPersonalBook.getAddedAt()).isNotNull();
        assertThat(response.addedAt()).isEqualTo(savedPersonalBook.getAddedAt());

        securityUtilMock.verify(SecurityUtil::getCurrentUserId, times(1));
        verify(userRepository, times(1)).findById(userId);
        verify(bookRepository, times(1)).findByIsbn(request.isbn());
    }

    @Test
    @DisplayName("현재 사용자 정보가 없으면 UserException 발생")
    void createBook_UserNotFound() {
        // given
        Long userId = 99L;
        PersonalBookCreateRequest request = PersonalBookCreateRequest.builder()
                .isbn("9788994757254")
                .build();

        securityUtilMock.when(SecurityUtil::getCurrentUserId).thenReturn(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> personalBookService.createBook(request))
                .isInstanceOf(UserException.class)
                .hasFieldOrPropertyWithValue("errorCode", UserErrorCode.USER_NOT_FOUND);

        securityUtilMock.verify(SecurityUtil::getCurrentUserId, times(1));
        verify(userRepository, times(1)).findById(userId);
        verify(bookRepository, never()).findByIsbn(anyString());
        verify(personalBookRepository, never()).save(any());
    }

    @Test
    @DisplayName("도서 정보를 찾지 못하면 BookException 발생")
    void createBook_BookNotFound() {
        // given
        Long userId = 1L;
        PersonalBookCreateRequest request = PersonalBookCreateRequest.builder()
                .isbn("9788994757254")
                .build();

        User user = User.builder()
                .id(userId)
                .kakaoId(12345L)
                .nickname("tester")
                .build();

        securityUtilMock.when(SecurityUtil::getCurrentUserId).thenReturn(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(bookRepository.findByIsbn(request.isbn())).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> personalBookService.createBook(request))
                .isInstanceOf(BookException.class)
                .hasFieldOrPropertyWithValue("errorCode", BookErrorCode.BOOK_NOT_FOUND);

        securityUtilMock.verify(SecurityUtil::getCurrentUserId, times(1));
        verify(userRepository, times(1)).findById(userId);
        verify(bookRepository, times(1)).findByIsbn(request.isbn());
        verify(personalBookRepository, never()).save(any());
    }

    @Test
    @DisplayName("같은 사용자가 같은 도서를 다시 등록하면 BookException 발생")
    void createBook_DuplicatePersonalBook() {
        // given
        Long userId = 1L;
        Long bookId = 10L;
        PersonalBookCreateRequest request = PersonalBookCreateRequest.builder()
                .isbn("9788994757254")
                .build();

        User user = User.builder()
                .id(userId)
                .kakaoId(12345L)
                .nickname("tester")
                .build();

        Book book = Book.builder()
                .id(bookId)
                .isbn(request.isbn())
                .bookName("테스트 책")
                .author("작가")
                .publisher("출판사")
                .build();

        PersonalBook existing = PersonalBook.create(user, book, BookReadingStatus.READING);

        securityUtilMock.when(SecurityUtil::getCurrentUserId).thenReturn(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(bookRepository.findByIsbn(request.isbn())).thenReturn(Optional.of(book));
        when(personalBookRepository.findByUserIdAndBookId(userId, bookId)).thenReturn(Optional.of(existing));

        // when & then
        assertThatThrownBy(() -> personalBookService.createBook(request))
                .isInstanceOf(BookException.class)
                .hasFieldOrPropertyWithValue("errorCode", BookErrorCode.BOOK_ALREADY_EXISTS);

        securityUtilMock.verify(SecurityUtil::getCurrentUserId, times(1));
        verify(userRepository, times(1)).findById(userId);
        verify(bookRepository, times(1)).findByIsbn(request.isbn());
        verify(personalBookRepository, times(1)).findByUserIdAndBookId(userId, bookId);
        verify(personalBookRepository, never()).save(any());
    }
}

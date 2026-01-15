package com.dokdok.book.service;

import com.dokdok.book.dto.request.PersonalReadingRecordCreateRequest;
import com.dokdok.book.dto.response.PersonalReadingRecordCreateResponse;
import com.dokdok.book.entity.Book;
import com.dokdok.book.entity.BookReadingStatus;
import com.dokdok.book.entity.PersonalBook;
import com.dokdok.book.entity.RecordType;
import com.dokdok.book.exception.RecordErrorCode;
import com.dokdok.book.exception.RecordException;
import com.dokdok.book.repository.PersonalReadingRecordRepository;
import com.dokdok.global.util.SecurityUtil;
import com.dokdok.user.entity.User;
import com.dokdok.user.service.UserValidator;
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

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PersonalReadingRecordService 테스트")
class PersonalReadingRecordServiceTest {

    @InjectMocks
    private PersonalReadingRecordService personalReadingRecordService;

    @Mock
    private PersonalReadingRecordRepository personalReadingRecordRepository;

    @Mock
    private UserValidator userValidator;

    @Mock
    private BookValidator bookValidator;

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
    @DisplayName("메모 기록 생성 시 meta는 null로 저장된다")
    void createMemoRecord_Success() {
        // given
        Long userId = 1L;
        Long personalBookId = 10L;
        PersonalReadingRecordCreateRequest request = new PersonalReadingRecordCreateRequest(
                RecordType.MEMO,
                "메모 내용",
                new HashMap<>()
        );

        User user = User.builder()
                .id(userId)
                .kakaoId(12345L)
                .nickname("tester")
                .build();

        Book book = Book.builder()
                .id(100L)
                .isbn("9788994757254")
                .bookName("테스트 책")
                .author("저자")
                .publisher("출판사")
                .build();

        PersonalBook personalBook = PersonalBook.builder()
                .id(personalBookId)
                .user(user)
                .book(book)
                .readingStatus(BookReadingStatus.READING)
                .build();

        securityUtilMock.when(SecurityUtil::getCurrentUserId).thenReturn(userId);
        when(userValidator.findUserOrThrow(userId)).thenReturn(user);
        when(bookValidator.validateInBookShelf(userId, personalBookId)).thenReturn(personalBook);

        // when
        PersonalReadingRecordCreateResponse response = personalReadingRecordService.create(personalBookId, request);

        // then
        assertThat(response.recordType()).isEqualTo(RecordType.MEMO);
        assertThat(response.recordContent()).isEqualTo(request.recordContent());
        assertThat(response.meta()).isNull();
        assertThat(response.personalBookId()).isEqualTo(personalBookId);

        ArgumentCaptor<com.dokdok.book.entity.PersonalReadingRecord> recordCaptor =
                ArgumentCaptor.forClass(com.dokdok.book.entity.PersonalReadingRecord.class);
        verify(personalReadingRecordRepository, times(1)).save(recordCaptor.capture());

        com.dokdok.book.entity.PersonalReadingRecord savedRecord = recordCaptor.getValue();
        assertThat(savedRecord.getRecordType()).isEqualTo(RecordType.MEMO);
        assertThat(savedRecord.getMeta()).isNull();
        assertThat(savedRecord.getPersonalBook()).isEqualTo(personalBook);

        securityUtilMock.verify(SecurityUtil::getCurrentUserId, times(1));
        verify(userValidator, times(1)).findUserOrThrow(userId);
        verify(bookValidator, times(1)).validateInBookShelf(userId, personalBookId);
    }

    @Test
    @DisplayName("인용 기록 생성 시 meta가 정규화되어 저장된다")
    void createQuoteRecord_Success() {
        // given
        Long userId = 1L;
        Long personalBookId = 20L;
        Map<String, Object> meta = new HashMap<>();
        meta.put("page", "12");
        meta.put("excerpt", "인용 내용");

        PersonalReadingRecordCreateRequest request = new PersonalReadingRecordCreateRequest(
                RecordType.QUOTE,
                "인용 기록",
                meta
        );

        User user = User.builder()
                .id(userId)
                .kakaoId(98765L)
                .nickname("reader")
                .build();

        Book book = Book.builder()
                .id(200L)
                .isbn("9781234567890")
                .bookName("다른 책")
                .author("다른 저자")
                .publisher("다른 출판사")
                .build();

        PersonalBook personalBook = PersonalBook.builder()
                .id(personalBookId)
                .user(user)
                .book(book)
                .readingStatus(BookReadingStatus.READING)
                .build();

        securityUtilMock.when(SecurityUtil::getCurrentUserId).thenReturn(userId);
        when(userValidator.findUserOrThrow(userId)).thenReturn(user);
        when(bookValidator.validateInBookShelf(userId, personalBookId)).thenReturn(personalBook);

        // when
        PersonalReadingRecordCreateResponse response = personalReadingRecordService.create(personalBookId, request);

        // then
        assertThat(response.recordType()).isEqualTo(RecordType.QUOTE);
        assertThat(response.recordContent()).isEqualTo(request.recordContent());
        assertThat(response.meta()).isNotNull();
        assertThat(response.meta().get("page")).isEqualTo(12);
        assertThat(response.meta().get("excerpt")).isEqualTo("인용 내용");
        assertThat(response.personalBookId()).isEqualTo(personalBookId);

        ArgumentCaptor<com.dokdok.book.entity.PersonalReadingRecord> recordCaptor =
                ArgumentCaptor.forClass(com.dokdok.book.entity.PersonalReadingRecord.class);
        verify(personalReadingRecordRepository, times(1)).save(recordCaptor.capture());

        com.dokdok.book.entity.PersonalReadingRecord savedRecord = recordCaptor.getValue();
        assertThat(savedRecord.getMeta()).isNotNull();
        assertThat(savedRecord.getMeta().get("page")).isEqualTo(12);
        assertThat(savedRecord.getMeta().get("excerpt")).isEqualTo("인용 내용");

        securityUtilMock.verify(SecurityUtil::getCurrentUserId, times(1));
        verify(userValidator, times(1)).findUserOrThrow(userId);
        verify(bookValidator, times(1)).validateInBookShelf(userId, personalBookId);
    }

    @Test
    @DisplayName("인용 기록 meta가 없으면 RecordException이 발생한다")
    void createQuoteRecord_MissingMeta() {
        // given
        Long userId = 1L;
        Long personalBookId = 30L;
        PersonalReadingRecordCreateRequest request = new PersonalReadingRecordCreateRequest(
                RecordType.QUOTE,
                "인용 기록",
                null
        );

        User user = User.builder()
                .id(userId)
                .kakaoId(123L)
                .nickname("reader")
                .build();

        PersonalBook personalBook = PersonalBook.builder()
                .id(personalBookId)
                .user(user)
                .book(Book.builder().id(300L).isbn("9780000000000").bookName("책").author("저자").publisher("출판").build())
                .readingStatus(BookReadingStatus.READING)
                .build();

        securityUtilMock.when(SecurityUtil::getCurrentUserId).thenReturn(userId);
        when(userValidator.findUserOrThrow(userId)).thenReturn(user);
        when(bookValidator.validateInBookShelf(userId, personalBookId)).thenReturn(personalBook);

        // when & then
        assertThatThrownBy(() -> personalReadingRecordService.create(personalBookId, request))
                .isInstanceOf(RecordException.class)
                .hasFieldOrPropertyWithValue("errorCode", RecordErrorCode.INVALID_RECORD_REQUEST);

        verify(personalReadingRecordRepository, never()).save(any());
        securityUtilMock.verify(SecurityUtil::getCurrentUserId, times(1));
        verify(userValidator, times(1)).findUserOrThrow(userId);
        verify(bookValidator, times(1)).validateInBookShelf(userId, personalBookId);
    }

    @Test
    @DisplayName("지원하지 않는 기록 타입이면 RecordException이 발생한다")
    void createRecord_InvalidRecordType() {
        // given
        Long userId = 1L;
        Long personalBookId = 40L;
        PersonalReadingRecordCreateRequest request = new PersonalReadingRecordCreateRequest(
                null,
                "잘못된 타입",
                null
        );

        User user = User.builder()
                .id(userId)
                .kakaoId(999L)
                .nickname("reader")
                .build();

        PersonalBook personalBook = PersonalBook.builder()
                .id(personalBookId)
                .user(user)
                .book(Book.builder().id(400L).isbn("9781111111111").bookName("책").author("저자").publisher("출판").build())
                .readingStatus(BookReadingStatus.READING)
                .build();

        securityUtilMock.when(SecurityUtil::getCurrentUserId).thenReturn(userId);
        when(userValidator.findUserOrThrow(userId)).thenReturn(user);
        when(bookValidator.validateInBookShelf(userId, personalBookId)).thenReturn(personalBook);

        // when & then
        assertThatThrownBy(() -> personalReadingRecordService.create(personalBookId, request))
                .isInstanceOf(RecordException.class)
                .hasFieldOrPropertyWithValue("errorCode", RecordErrorCode.INVALID_RECORD_TYPE);

        verify(personalReadingRecordRepository, never()).save(any());
        securityUtilMock.verify(SecurityUtil::getCurrentUserId, times(1));
        verify(userValidator, times(1)).findUserOrThrow(userId);
        verify(bookValidator, times(1)).validateInBookShelf(userId, personalBookId);
    }
}

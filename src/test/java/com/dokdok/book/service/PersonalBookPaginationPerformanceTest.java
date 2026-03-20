package com.dokdok.book.service;

import com.dokdok.book.dto.request.PersonalBookSortBy;
import com.dokdok.book.dto.request.PersonalBookSortOrder;
import com.dokdok.oauth2.CustomOAuth2User;
import com.dokdok.user.entity.User;
import com.dokdok.user.repository.UserRepository;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@SpringBootTest
@Slf4j
@ActiveProfiles("perf-test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PersonalBookPaginationPerformanceTest {

    private static final int REPEAT_COUNT = 10;
    private static final int WARM_UP_COUNT = 5;
    private static final int PAGE_SIZE = 10;
    private static final int TOTAL_BOOKS = 200;

    @Autowired private PersonalBookService personalBookService;
    @Autowired private MeterRegistry meterRegistry;
    @Autowired private DataSource dataSource;
    @Autowired private JdbcTemplate jdbcTemplate;
    @Autowired private UserRepository userRepository;

    private User testUser;

    @BeforeAll
    void setUpTestData() throws Exception {
        try (Connection conn = dataSource.getConnection()) {
            ScriptUtils.executeSqlScript(conn, new ClassPathResource("sql/seed_personal_book_perf_data.sql"));
        }

        Long userId = jdbcTemplate.queryForObject(
                "SELECT user_id FROM users WHERE user_email = 'book-perf@dokdok.com' AND deleted_at IS NULL",
                Long.class);
        testUser = userRepository.findById(userId).orElseThrow();

        log.info("책장 성능 테스트 데이터 로드 완료 | userId={}", userId);
    }

    @AfterAll
    void cleanUpTestData() throws Exception {
        try (Connection conn = dataSource.getConnection()) {
            ScriptUtils.executeSqlScript(conn, new ClassPathResource("sql/cleanup_personal_book_perf_data.sql"));
        }
        log.info("책장 성능 테스트 데이터 정리 완료");
    }

    @BeforeEach
    void setUp() {
        CustomOAuth2User oAuth2User = CustomOAuth2User.builder()
                .user(testUser)
                .attributes(Map.of("id", testUser.getId()))
                .build();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(oAuth2User, null, oAuth2User.getAuthorities())
        );
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @DisplayName("책장 조회 - Before (전체 조회 후 Java-side 필터·정렬·커서)")
    @RepeatedTest(REPEAT_COUNT)
    @Order(1)
    void testBeforePagination(RepetitionInfo rep) {
        Timer timer = meterRegistry.timer("bookshelf-before");

        if (rep.getCurrentRepetition() <= WARM_UP_COUNT) {
            personalBookService.getPersonalBookListCursorLegacy(
                    null, null,
                    PersonalBookSortBy.TIME, PersonalBookSortOrder.DESC,
                    null, null, null, null, null, PAGE_SIZE);
        } else {
            timer.record(() ->
                personalBookService.getPersonalBookListCursorLegacy(
                        null, null,
                        PersonalBookSortBy.TIME, PersonalBookSortOrder.DESC,
                        null, null, null, null, null, PAGE_SIZE)
            );
        }

        if (rep.getCurrentRepetition() == REPEAT_COUNT) {
            log.info("[BEFORE] DB rows={} (전체 조회), mean={}ms",
                    TOTAL_BOOKS,
                    String.format("%.2f", timer.mean(TimeUnit.MILLISECONDS)));
        }
    }

    @DisplayName("책장 조회 - After (DB-side 페이지네이션: LIMIT + cursor)")
    @RepeatedTest(REPEAT_COUNT)
    @Order(2)
    void testAfterPagination(RepetitionInfo rep) {
        Timer timer = meterRegistry.timer("bookshelf-after");

        if (rep.getCurrentRepetition() <= WARM_UP_COUNT) {
            personalBookService.getPersonalBookListCursor(
                    null, null,
                    PersonalBookSortBy.TIME, PersonalBookSortOrder.DESC,
                    null, null, null, null, null, PAGE_SIZE);
        } else {
            timer.record(() ->
                personalBookService.getPersonalBookListCursor(
                        null, null,
                        PersonalBookSortBy.TIME, PersonalBookSortOrder.DESC,
                        null, null, null, null, null, PAGE_SIZE)
            );
        }

        if (rep.getCurrentRepetition() == REPEAT_COUNT) {
            log.info("[AFTER] DB rows={} (pageSize+1), mean={}ms",
                    PAGE_SIZE + 1,
                    String.format("%.2f", timer.mean(TimeUnit.MILLISECONDS)));
        }
    }

    @AfterAll
    void logComparison() {
        double beforeMean = meterRegistry.timer("bookshelf-before").mean(TimeUnit.MILLISECONDS);
        double afterMean  = meterRegistry.timer("bookshelf-after").mean(TimeUnit.MILLISECONDS);

        log.info("==================================================");
        log.info("[BEFORE]  DB rows={} (전체 조회), mean={}ms", TOTAL_BOOKS, String.format("%.2f", beforeMean));
        log.info("[AFTER]  DB rows={} (pageSize+1), mean={}ms", PAGE_SIZE + 1, String.format("%.2f", afterMean));
        if (beforeMean > 0 && afterMean > 0) {
            log.info("[개선율] {}x 빠름", String.format("%.1f", beforeMean / afterMean));
        }
        log.info("==================================================");
    }
}

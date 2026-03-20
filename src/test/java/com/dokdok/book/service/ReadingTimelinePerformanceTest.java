package com.dokdok.book.service;

import com.dokdok.book.dto.request.PreOpinionTimeType;
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
class ReadingTimelinePerformanceTest {

    private static final int REPEAT_COUNT = 10;
    private static final int WARM_UP_COUNT = 5;

    @Autowired private ReadingTimelineService readingTimelineService;
    @Autowired private MeterRegistry meterRegistry;
    @Autowired private DataSource dataSource;
    @Autowired private JdbcTemplate jdbcTemplate;
    @Autowired private UserRepository userRepository;

    private User testUser;
    private Long personalBookId;

    @BeforeAll
    void setUpTestData() throws Exception {
        try (Connection conn = dataSource.getConnection()) {
            ScriptUtils.executeSqlScript(conn, new ClassPathResource("sql/seed_perf_data.sql"));
        }

        Long userId = jdbcTemplate.queryForObject(
                "SELECT user_id FROM users WHERE user_email = 'perf@dokdok.com' AND deleted_at IS NULL",
                Long.class);
        testUser = userRepository.findById(userId).orElseThrow();

        personalBookId = jdbcTemplate.queryForObject(
                "SELECT pb.personal_book_id FROM personal_book pb " +
                "JOIN book b ON b.book_id = pb.book_id " +
                "WHERE pb.user_id = ? AND b.isbn = 'PERF-ISBN-A' AND pb.deleted_at IS NULL",
                Long.class, userId);

        log.info("✅ 성능 테스트 데이터 로드 완료 | userId={} personalBookId={}", userId, personalBookId);
    }

    @AfterAll
    void cleanUpTestData() throws Exception {
        try (Connection conn = dataSource.getConnection()) {
            ScriptUtils.executeSqlScript(conn, new ClassPathResource("sql/cleanup_perf_data.sql"));
        }
        log.info("✅ 성능 테스트 데이터 정리 완료");
    }

    @BeforeEach
    void setUpSecurityContext() {
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

    @DisplayName("타임라인 조회")
    @RepeatedTest(REPEAT_COUNT)
    @Order(1)
    void testGetTimeline(RepetitionInfo rep) {
        Timer timer = meterRegistry.timer("timeline");

        if (rep.getCurrentRepetition() <= WARM_UP_COUNT) {
            readingTimelineService.getTimeline(
                    personalBookId, null, null, null, 10, PreOpinionTimeType.ANSWER_CREATED);
        } else {
            timer.record(() -> readingTimelineService.getTimeline(
                    personalBookId, null, null, null, 10, PreOpinionTimeType.ANSWER_CREATED));
        }

        if (rep.getCurrentRepetition() == REPEAT_COUNT) {
            log.info("[timeline] mean = {}ms", String.format("%.2f", timer.mean(TimeUnit.MILLISECONDS)));
        }
    }

    @AfterAll
    void logComparison() {
        double mean = meterRegistry.timer("timeline").mean(TimeUnit.MILLISECONDS);

        log.info("========================================");
        log.info("[timeline] mean = {}ms", String.format("%.2f", mean));
        log.info("========================================");
    }
}

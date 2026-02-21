package com.dokdok.meeting.service;

import com.dokdok.oauth2.CustomOAuth2User;
import com.dokdok.storage.service.StorageService;
import com.dokdok.user.entity.User;
import com.dokdok.user.repository.UserRepository;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
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

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest
@Slf4j
@ActiveProfiles("perf-test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MeetingDetailPerformanceTest {

    private static final int REPEAT_COUNT = 10;
    private static final int WARM_UP_COUNT = 5;

    @Autowired private MeetingService meetingService;
    @Autowired private MeetingFetchService meetingFetchService;
    @Autowired private MeterRegistry meterRegistry;
    @Autowired private DataSource dataSource;
    @Autowired private JdbcTemplate jdbcTemplate;
    @Autowired private UserRepository userRepository;

    @MockitoBean private StorageService storageService;

    private User testUser;
    private Long meetingId;

    @BeforeAll
    void setUpTestData() throws Exception {
        try (Connection conn = dataSource.getConnection()) {
            ScriptUtils.executeSqlScript(conn, new ClassPathResource("sql/seed_meeting_perf_data.sql"));
        }

        Long userId = jdbcTemplate.queryForObject(
                "SELECT user_id FROM users WHERE user_email = 'perf_meeting@dokdok.com' AND deleted_at IS NULL",
                Long.class);
        testUser = userRepository.findById(userId).orElseThrow();

        meetingId = jdbcTemplate.queryForObject(
                "SELECT m.meeting_id FROM meeting m " +
                "JOIN gathering g ON g.gathering_id = m.gathering_id " +
                "WHERE g.invitation_link = 'MEET-PERF-INVITE' " +
                "LIMIT 1",
                Long.class);

        log.info("성능 테스트 데이터 로드 완료 | userId={} meetingId={}", userId, meetingId);
    }

    @AfterAll
    void cleanUpTestData() throws Exception {
        try (Connection conn = dataSource.getConnection()) {
            ScriptUtils.executeSqlScript(conn, new ClassPathResource("sql/cleanup_meeting_perf_data.sql"));
        }
        log.info("성능 테스트 데이터 정리 완료");
    }

    @BeforeEach
    void setUpSecurityContext() {
        when(storageService.getPresignedProfileImage(anyString())).thenAnswer(inv -> {
            Thread.sleep(50);
            return "https://mock-url/" + inv.getArgument(0);
        });

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

    @DisplayName("약속 상세 조회 - 동기 순차 실행")
    @RepeatedTest(REPEAT_COUNT)
    @Order(1)
    void syncTest(RepetitionInfo rep) {
        Timer timer = meterRegistry.timer("meeting-sync");

        if (rep.getCurrentRepetition() <= WARM_UP_COUNT) {
            meetingService.findMeeting(meetingId);
        } else {
            timer.record(() -> meetingService.findMeeting(meetingId));
        }

        if (rep.getCurrentRepetition() == REPEAT_COUNT) {
            log.info("[SYNC]  mean = {}ms", String.format("%.2f", timer.mean(TimeUnit.MILLISECONDS)));
        }
    }

    @DisplayName("약속 상세 조회 - 비동기 병렬 실행")
    @RepeatedTest(REPEAT_COUNT)
    @Order(2)
    void asyncTest(RepetitionInfo rep) {
        Timer timer = meterRegistry.timer("meeting-async");

        if (rep.getCurrentRepetition() <= WARM_UP_COUNT) {
            meetingFetchService.findMeetingAsync(meetingId, testUser.getId());
        } else {
            timer.record(() -> meetingFetchService.findMeetingAsync(meetingId, testUser.getId()));
        }

        if (rep.getCurrentRepetition() == REPEAT_COUNT) {
            log.info("[ASYNC] mean = {}ms", String.format("%.2f", timer.mean(TimeUnit.MILLISECONDS)));
        }
    }

    @AfterAll
    void logComparison() {
        double syncMean  = meterRegistry.timer("meeting-sync").mean(TimeUnit.MILLISECONDS);
        double asyncMean = meterRegistry.timer("meeting-async").mean(TimeUnit.MILLISECONDS);

        log.info("========================================");
        log.info("[SYNC]  mean = {}ms", String.format("%.2f", syncMean));
        log.info("[ASYNC] mean = {}ms", String.format("%.2f", asyncMean));
        if (syncMean > 0) {
            log.info("[비율]  async / sync = {}x", String.format("%.2f", asyncMean / syncMean));
        }
        log.info("========================================");
    }
}

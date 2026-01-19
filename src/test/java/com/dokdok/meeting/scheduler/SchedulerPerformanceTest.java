package com.dokdok.meeting.scheduler;

import com.dokdok.meeting.entity.Meeting;
import com.dokdok.meeting.entity.MeetingStatus;
import com.dokdok.meeting.repository.MeetingRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Scheduler 성능 측정 테스트
 * - 1,000건, 5,000건, 10,000건 데이터에 대한 처리 시간 측정
 */
@SpringBootTest
@Slf4j
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SchedulerPerformanceTest {

    @Autowired
    private MeetingStatusScheduler scheduler;

    @Autowired
    private MeetingRepository meetingRepository;

    @Autowired
    private TestDataGenerator testDataGenerator;

    @AfterEach
    void cleanup() {
        testDataGenerator.cleanupTestData();
    }

    @Test
    @Order(1)
    @DisplayName("Scheduler 기본 동작 테스트 - 10건")
    void testSchedulerBasicOperation() {
        // Given: 소량의 테스트 데이터 생성
        int dataSize = 10;
        testDataGenerator.generateExpiredMeetings(dataSize);

        // When: Scheduler 실행
        scheduler.updateExpiredMeetings();

        // Then: CONFIRMED 상태의 Meeting이 없어야 함
        List<Meeting> confirmedMeetings = meetingRepository
                .findByMeetingEndDateBeforeAndMeetingStatus(
                        LocalDateTime.now(), MeetingStatus.CONFIRMED);

        assertThat(confirmedMeetings).isEmpty();

        log.info("기본 동작 테스트 통과: {}건 모두 DONE으로 변경됨", dataSize);
    }

    @Test
    @Order(2)
    @DisplayName("Scheduler 성능 테스트: 1,000건")
    void testSchedulerPerformance_1000() {
        performanceTest(1000);
    }

    @Test
    @Order(3)
    @DisplayName("Scheduler 성능 테스트: 5,000건")
    void testSchedulerPerformance_5000() {
        performanceTest(5000);
    }

    @Test
    @Order(4)
    @DisplayName("Scheduler 성능 테스트: 10,000건")
    void testSchedulerPerformance_10000() {
        performanceTest(10000);
    }

    /**
     * 공통 성능 테스트 로직
     */
    private void performanceTest(int dataSize) {
        System.out.println("\n");
        System.out.println("========================================");
        System.out.println("테스트 시작: " + dataSize + "건");
        System.out.println("========================================");
        
        // Given: 지정된 개수만큼 만료된 Meeting 생성
        testDataGenerator.generateExpiredMeetings(dataSize);
        
        List<Meeting> beforeMeetings = meetingRepository
                .findByMeetingEndDateBeforeAndMeetingStatus(
                        LocalDateTime.now(), MeetingStatus.CONFIRMED);
        
        assertThat(beforeMeetings).hasSize(dataSize);
        System.out.println("데이터 생성 완료: " + beforeMeetings.size() + "건");

        // When: Scheduler 실행 및 시간 측정
        long startTime = System.currentTimeMillis();
        scheduler.updateExpiredMeetings();
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        // Then: 모든 Meeting이 DONE 상태로 변경되었는지 확인
        List<Meeting> remainingConfirmed = meetingRepository
                .findByMeetingEndDateBeforeAndMeetingStatus(
                        LocalDateTime.now(), MeetingStatus.CONFIRMED);
        assertThat(remainingConfirmed).isEmpty();

        // 성능 지표 계산 및 출력
        double throughput = duration > 0 ? (dataSize * 1000.0 / duration) : dataSize;
        double avgTimePerMeeting = duration / (double) dataSize;

        System.out.println("");
        System.out.println("========================================");
        System.out.println("성능 측정 결과 [" + dataSize + "건]");
        System.out.println("========================================");
        System.out.println("총 처리 시간: " + duration + "ms (" + (duration / 1000.0) + " 초)");
        System.out.println("처리량 (TPS): " + String.format("%.2f", throughput) + "/sec");
        System.out.println("평균 처리 시간: " + String.format("%.3f", avgTimePerMeeting) + "ms/meeting");
        System.out.println("성공률: 100% (" + dataSize + "/" + dataSize + ")");
        System.out.println("========================================");
        System.out.println("\n");
    }
}

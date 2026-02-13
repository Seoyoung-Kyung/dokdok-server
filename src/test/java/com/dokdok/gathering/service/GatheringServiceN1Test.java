package com.dokdok.gathering.service;

import com.dokdok.gathering.entity.*;
import com.dokdok.gathering.repository.GatheringMemberRepository;
import com.dokdok.gathering.repository.GatheringRepository;
import com.dokdok.global.util.SecurityUtil;
import com.dokdok.meeting.entity.Meeting;
import com.dokdok.meeting.entity.MeetingStatus;
import com.dokdok.meeting.repository.MeetingRepository;
import com.dokdok.user.entity.User;
import com.dokdok.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
import org.hibernate.Session;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.mockito.Mockito.mockStatic;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("Gathering N+1 쿼리 테스트")
class GatheringServiceN1Test {

    @Autowired
    private GatheringService gatheringService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GatheringRepository gatheringRepository;

    @Autowired
    private GatheringMemberRepository gatheringMemberRepository;

    @Autowired
    private MeetingRepository meetingRepository;

    @Autowired
    private EntityManager entityManager;

    private MockedStatic<SecurityUtil> securityUtilMock;
    private User testUser;
    private Statistics statistics;

    @BeforeEach
    void setUp() {
        // Hibernate Statistics 활성화
        Session session = entityManager.unwrap(Session.class);
        statistics = session.getSessionFactory().getStatistics();
        statistics.setStatisticsEnabled(true);

        // SecurityUtil Mock
        securityUtilMock = mockStatic(SecurityUtil.class);

        // 테스트 유저 생성
        testUser = userRepository.save(User.builder()
                .kakaoId(12345L)
                .nickname("테스트유저")
                .profileImageUrl("test.jpg")
                .build());

        securityUtilMock.when(SecurityUtil::getCurrentUserId).thenReturn(testUser.getId());
    }

    @AfterEach
    void tearDown() {
        securityUtilMock.close();
        statistics.clear();
    }

    @Test
    @DisplayName("getFavoriteGatherings - N+1 쿼리 발생 확인")
    void getFavoriteGatherings_N1_Problem() {
        // given : 즐겨찾기 모임 5개 생성
        int gatheringCount = 5;
        createFavoriteGatherings(gatheringCount);

        // 영속성 컨텍스트 초기화
        entityManager.flush();
        entityManager.clear();
        statistics.clear();

        // when
        gatheringService.getFavoriteGatherings();

        // then: 쿼리 수 확인
        long queryCount = statistics.getQueryExecutionCount();

        System.out.println("========================================");
        System.out.println("즐겨찾기 모임 수: " + gatheringCount);
        System.out.println("실행된 쿼리 수: " + queryCount);
        System.out.println("예상 쿼리 수 (N+1 발생 시): " + (1 + gatheringCount * 2));
        System.out.println("예상 쿼리 수 (최적화 후): 3");
        System.out.println("========================================");

        // N+1 발생 시: 1(목록조회) + N(멤버카운트) + N(미팅카운트) = 1 + 2N
        // 최적화 후: 1(목록조회) + 1(멤버카운트) + 1(미팅카운트) = 3

        // 현재 N+1 문제가 있으므로 쿼리가 많이 발생함을 확인
        System.out.println("N+1 문제 발생 여부: " + (queryCount > 3 ? "YES" : "NO"));
    }

    @Test
    @DisplayName("getMyGatherings - N+1 쿼리 발생 확인")
    void getMyGatherings_N1_Problem() {
        // given: 내 모임 10개 생성
        int gatheringCount = 10;
        createMyGatherings(gatheringCount);

        entityManager.flush();
        entityManager.clear();
        statistics.clear();

        // when
        gatheringService.getMyGatherings(10, null, null);

        // then
        long queryCount = statistics.getQueryExecutionCount();

        System.out.println("========================================");
        System.out.println("내 모임 수: " + gatheringCount);
        System.out.println("실행된 쿼리 수: " + queryCount);
        System.out.println("예상 쿼리 수 (N+1 발생 시): " + (2 + gatheringCount * 2));
        System.out.println("예상 쿼리 수 (최적화 후): 4");
        System.out.println("========================================");

        System.out.println("N+1 문제 발생 여부: " + (queryCount > 4 ? "YES" : "NO"));
    }

    private void createFavoriteGatherings(int count) {
        for (int i = 0; i < count; i++) {
            Gathering gathering = gatheringRepository.save(Gathering.builder()
                    .gatheringName("모임" + i)
                    .description("설명" + i)
                    .gatheringStatus(GatheringStatus.ACTIVE)
                    .invitationLink("invite" + i)
                    .gatheringLeader(testUser)
                    .build());

            gatheringMemberRepository.save(GatheringMember.builder()
                    .gathering(gathering)
                    .user(testUser)
                    .role(GatheringRole.LEADER)
                    .memberStatus(GatheringMemberStatus.ACTIVE)
                    .isFavorite(true)
                    .joinedAt(LocalDateTime.now())
                    .build());

            // 미팅도 몇 개 생성
            meetingRepository.save(Meeting.builder()
                    .gathering(gathering)
                    .meetingStatus(MeetingStatus.DONE)
                    .meetingStartDate(LocalDateTime.now().minusDays(1))
                    .meetingEndDate(LocalDateTime.now())
                    .build());
        }
    }

    private void createMyGatherings(int count) {
        for (int i = 0; i < count; i++) {
            Gathering gathering = gatheringRepository.save(Gathering.builder()
                    .gatheringName("모임" + i)
                    .description("설명" + i)
                    .gatheringStatus(GatheringStatus.ACTIVE)
                    .invitationLink("invite" + i)
                    .gatheringLeader(testUser)
                    .build());

            gatheringMemberRepository.save(GatheringMember.builder()
                    .gathering(gathering)
                    .user(testUser)
                    .role(GatheringRole.LEADER)
                    .memberStatus(GatheringMemberStatus.ACTIVE)
                    .isFavorite(false)
                    .joinedAt(LocalDateTime.now().minusDays(i))
                    .build());

            meetingRepository.save(Meeting.builder()
                    .gathering(gathering)
                    .meetingStatus(MeetingStatus.DONE)
                    .meetingStartDate(LocalDateTime.now().minusDays(1))
                    .meetingEndDate(LocalDateTime.now())
                    .build());
        }
    }
}

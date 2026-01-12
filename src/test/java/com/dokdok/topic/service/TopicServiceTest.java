package com.dokdok.topic.service;

import com.dokdok.gathering.exception.GatheringErrorCode;
import com.dokdok.gathering.exception.GatheringException;
import com.dokdok.gathering.service.GatheringValidator;
import com.dokdok.global.exception.GlobalErrorCode;
import com.dokdok.global.exception.GlobalException;
import com.dokdok.global.util.SecurityUtil;
import com.dokdok.meeting.entity.Meeting;
import com.dokdok.meeting.entity.MeetingMember;
import com.dokdok.meeting.exception.MeetingErrorCode;
import com.dokdok.meeting.exception.MeetingException;
import com.dokdok.meeting.service.MeetingValidator;
import com.dokdok.topic.dto.request.SuggestTopicRequest;
import com.dokdok.topic.dto.response.SuggestTopicResponse;
import com.dokdok.topic.entity.Topic;
import com.dokdok.topic.entity.TopicType;
import com.dokdok.topic.repository.TopicRepository;
import com.dokdok.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TopicService - createTopic 테스트")
class TopicServiceTest {

    @Mock
    private TopicRepository topicRepository;

    @Mock
    private MeetingValidator meetingValidator;

    @Mock
    private GatheringValidator gatheringValidator;

    @InjectMocks
    private TopicService topicService;

    private User testUser;
    private Meeting testMeeting;
    private MeetingMember testMeetingMember;
    private Topic testTopic;
    private SuggestTopicRequest testRequest;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .userName("김독서")
                .nickname("책벌레김")
                .userEmail("kim@example.com")
                .kakaoId(1001L)
                .build();

        testMeeting = Meeting.builder()
                .id(1L)
                .meetingName("클린 코드 1회차")
                .build();

        testMeetingMember = MeetingMember.builder()
                .id(1L)
                .meeting(testMeeting)
                .user(testUser)
                .attendanceStatus("ATTENDING")
                .build();

        testTopic = Topic.builder()
                .id(1L)
                .meeting(testMeeting)
                .proposedBy(testUser)
                .title("의미 있는 이름 짓기")
                .description("변수명, 함수명, 클래스명을 짓는 원칙에 대해 토론합니다.")
                .topicType(TopicType.DISCUSSION)
                .build();

        testRequest = new SuggestTopicRequest(
                "의미 있는 이름 짓기",
                "변수명, 함수명, 클래스명을 짓는 원칙에 대해 토론합니다.",
                TopicType.DISCUSSION
        );
    }

    @Test
    @DisplayName("정상적으로 주제를 생성한다")
    void createTopic_Success() {
        Long gatheringId = 1L;
        Long meetingId = 1L;
        Long userId = 1L;

        try (MockedStatic<SecurityUtil> securityUtilMock = mockStatic(SecurityUtil.class)) {
            // SecurityUtil에서 userId 가져오기
            securityUtilMock.when(SecurityUtil::getCurrentUserId)
                    .thenReturn(userId);

            // 모임 멤버 검증 통과
            doNothing().when(gatheringValidator)
                    .validateMembership(gatheringId, userId);

            // 회차 소속 검증 통과
            doNothing().when(meetingValidator)
                    .validateMemberInGathering(meetingId, gatheringId);

            // 회차 참석자 조회 성공
            given(meetingValidator.getMeetingMember(meetingId, userId))
                    .willReturn(testMeetingMember);

            // Topic 저장 성공
            given(topicRepository.save(any(Topic.class)))
                    .willReturn(testTopic);

            SuggestTopicResponse response =
                    topicService.createTopic(gatheringId, meetingId, testRequest);

            assertThat(response).isNotNull();
            assertThat(response.title()).isEqualTo("의미 있는 이름 짓기");
            assertThat(response.topicType()).isEqualTo(TopicType.DISCUSSION);
            assertThat(response.createdBy().userId()).isEqualTo(1L);
            assertThat(response.createdBy().nickname()).isEqualTo("책벌레김");

            verify(gatheringValidator).validateMembership(gatheringId, userId);
            verify(meetingValidator).validateMemberInGathering(meetingId, gatheringId);
            verify(meetingValidator).getMeetingMember(meetingId, userId);
            verify(topicRepository).save(any(Topic.class));
        }
    }

    @Test
    @DisplayName("인증되지 않은 사용자인 경우 예외가 발생한다")
    void createTopic_Unauthorized_ThrowsException() {
        Long gatheringId = 1L;
        Long meetingId = 1L;

        try (MockedStatic<SecurityUtil> securityUtilMock = mockStatic(SecurityUtil.class)) {
            securityUtilMock.when(SecurityUtil::getCurrentUserId)
                    .thenThrow(new GlobalException(GlobalErrorCode.UNAUTHORIZED));

            assertThatThrownBy(() ->
                    topicService.createTopic(gatheringId, meetingId, testRequest))
                    .isInstanceOf(GlobalException.class)
                    .hasFieldOrPropertyWithValue("errorCode",
                            GlobalErrorCode.UNAUTHORIZED);

            verify(gatheringValidator, never()).validateMembership(any(), any());
            verify(meetingValidator, never()).validateMemberInGathering(any(), any());
            verify(meetingValidator, never()).getMeetingMember(any(), any());
            verify(topicRepository, never()).save(any());
        }
    }

    @Test
    @DisplayName("모임 멤버가 아닌 경우 예외가 발생한다")
    void createTopic_NotGatheringMember_ThrowsException() {
        Long gatheringId = 1L;
        Long meetingId = 1L;
        Long userId = 1L;

        try (MockedStatic<SecurityUtil> securityUtilMock = mockStatic(SecurityUtil.class)) {
            securityUtilMock.when(SecurityUtil::getCurrentUserId)
                    .thenReturn(userId);

            doThrow(new GatheringException(GatheringErrorCode.NOT_GATHERING_MEMBER))
                    .when(gatheringValidator)
                    .validateMembership(gatheringId, userId);

            assertThatThrownBy(() ->
                    topicService.createTopic(gatheringId, meetingId, testRequest))
                    .isInstanceOf(GatheringException.class)
                    .hasFieldOrPropertyWithValue("errorCode",
                            GatheringErrorCode.NOT_GATHERING_MEMBER);

            verify(meetingValidator, never()).validateMemberInGathering(any(), any());
            verify(meetingValidator, never()).getMeetingMember(any(), any());
            verify(topicRepository, never()).save(any());
        }
    }

    @Test
    @DisplayName("회차가 해당 모임에 속하지 않는 경우 예외가 발생한다")
    void createTopic_MeetingNotInGathering_ThrowsException() {
        Long gatheringId = 1L;
        Long meetingId = 999L;
        Long userId = 1L;

        try (MockedStatic<SecurityUtil> securityUtilMock = mockStatic(SecurityUtil.class)) {
            securityUtilMock.when(SecurityUtil::getCurrentUserId)
                    .thenReturn(userId);

            doNothing().when(gatheringValidator)
                    .validateMembership(gatheringId, userId);

            doThrow(new MeetingException(MeetingErrorCode.NOT_GATHERING_MEETING))
                    .when(meetingValidator)
                    .validateMemberInGathering(meetingId, gatheringId);

            assertThatThrownBy(() ->
                    topicService.createTopic(gatheringId, meetingId, testRequest))
                    .isInstanceOf(MeetingException.class)
                    .hasFieldOrPropertyWithValue("errorCode",
                            MeetingErrorCode.NOT_GATHERING_MEETING);

            verify(meetingValidator, never()).getMeetingMember(any(), any());
            verify(topicRepository, never()).save(any());
        }
    }

    @Test
    @DisplayName("회차 참석자가 아닌 경우 예외가 발생한다")
    void createTopic_NotMeetingMember_ThrowsException() {
        Long gatheringId = 1L;
        Long meetingId = 1L;
        Long userId = 1L;

        try (MockedStatic<SecurityUtil> securityUtilMock = mockStatic(SecurityUtil.class)) {
            securityUtilMock.when(SecurityUtil::getCurrentUserId)
                    .thenReturn(userId);

            doNothing().when(gatheringValidator)
                    .validateMembership(gatheringId, userId);

            doNothing().when(meetingValidator)
                    .validateMemberInGathering(meetingId, gatheringId);

            given(meetingValidator.getMeetingMember(meetingId, userId))
                    .willThrow(new MeetingException(MeetingErrorCode.NOT_MEETING_MEMBER));

            assertThatThrownBy(() ->
                    topicService.createTopic(gatheringId, meetingId, testRequest))
                    .isInstanceOf(MeetingException.class)
                    .hasFieldOrPropertyWithValue("errorCode",
                            MeetingErrorCode.NOT_MEETING_MEMBER);

            verify(topicRepository, never()).save(any());
        }
    }
}

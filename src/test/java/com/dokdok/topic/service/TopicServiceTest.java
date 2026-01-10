package com.dokdok.topic.service;

import com.dokdok.gathering.service.GatheringMemberValidator;
import com.dokdok.global.exception.GlobalErrorCode;
import com.dokdok.global.exception.GlobalException;
import com.dokdok.meeting.entity.Meeting;
import com.dokdok.meeting.repository.MeetingRepository;
import com.dokdok.topic.dto.SuggestTopicRequest;
import com.dokdok.topic.dto.SuggestTopicResponse;
import com.dokdok.topic.entity.Topic;
import com.dokdok.topic.enums.TopicType;
import com.dokdok.topic.repository.TopicRepository;
import com.dokdok.user.entity.User;
import com.dokdok.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TopicService 테스트")
class TopicServiceTest {

    @Mock
    private TopicRepository topicRepository;

    @Mock
    private MeetingRepository meetingRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private GatheringMemberValidator gatheringMemberValidator;

    @InjectMocks
    private TopicService topicService;

    private User testUser;
    private Meeting testMeeting;
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

        // 멤버 검증은 통과 (아무 동작 안 함)
        doNothing().when(gatheringMemberValidator)
                .validateMembership(gatheringId, userId);

        given(meetingRepository.findByIdAndGatheringId(meetingId, gatheringId))
                .willReturn(Optional.of(testMeeting));
        given(userRepository.findById(userId))
                .willReturn(Optional.of(testUser));
        given(topicRepository.save(any(Topic.class)))
                .willReturn(testTopic);

        SuggestTopicResponse response =
                topicService.createTopic(gatheringId, meetingId, userId, testRequest);

        assertThat(response).isNotNull();
        assertThat(response.title()).isEqualTo("의미 있는 이름 짓기");
        assertThat(response.topicType()).isEqualTo(TopicType.DISCUSSION);

        verify(gatheringMemberValidator).validateMembership(gatheringId, userId);
        verify(meetingRepository).findByIdAndGatheringId(meetingId, gatheringId);
        verify(userRepository).findById(userId);
        verify(topicRepository).save(any(Topic.class));
    }

    @Test
    @DisplayName("모임 멤버가 아닌 경우 예외가 발생한다")
    void createTopic_NotGatheringMember_ThrowsException() {
        Long gatheringId = 1L;
        Long meetingId = 1L;
        Long userId = 1L;

        doThrow(new GlobalException(GlobalErrorCode.NOT_GATHERING_MEMBER))
                .when(gatheringMemberValidator)
                .validateMembership(gatheringId, userId);

        assertThatThrownBy(() ->
                topicService.createTopic(gatheringId, meetingId, userId, testRequest))
                .isInstanceOf(GlobalException.class)
                .hasFieldOrPropertyWithValue("errorCode",
                        GlobalErrorCode.NOT_GATHERING_MEMBER);

        verify(meetingRepository, never()).findByIdAndGatheringId(any(), any());
        verify(topicRepository, never()).save(any());
    }

    @Test
    @DisplayName("존재하지 않는 미팅인 경우 예외가 발생한다")
    void createTopic_MeetingNotFound_ThrowsException() {
        Long gatheringId = 1L;
        Long meetingId = 999L;
        Long userId = 1L;

        doNothing().when(gatheringMemberValidator)
                .validateMembership(gatheringId, userId);

        given(meetingRepository.findByIdAndGatheringId(meetingId, gatheringId))
                .willReturn(Optional.empty());

        assertThatThrownBy(() ->
                topicService.createTopic(gatheringId, meetingId, userId, testRequest))
                .isInstanceOf(GlobalException.class)
                .hasFieldOrPropertyWithValue("errorCode",
                        GlobalErrorCode.NOT_FOUND);

        verify(userRepository, never()).findById(any());
        verify(topicRepository, never()).save(any());
    }

    @Test
    @DisplayName("존재하지 않는 사용자인 경우 예외가 발생한다")
    void createTopic_UserNotFound_ThrowsException() {
        Long gatheringId = 1L;
        Long meetingId = 1L;
        Long userId = 999L;

        doNothing().when(gatheringMemberValidator)
                .validateMembership(gatheringId, userId);

        given(meetingRepository.findByIdAndGatheringId(meetingId, gatheringId))
                .willReturn(Optional.of(testMeeting));
        given(userRepository.findById(userId))
                .willReturn(Optional.empty());

        assertThatThrownBy(() ->
                topicService.createTopic(gatheringId, meetingId, userId, testRequest))
                .isInstanceOf(GlobalException.class)
                .hasFieldOrPropertyWithValue("errorCode",
                        GlobalErrorCode.NOT_FOUND);

        verify(topicRepository, never()).save(any());
    }
}

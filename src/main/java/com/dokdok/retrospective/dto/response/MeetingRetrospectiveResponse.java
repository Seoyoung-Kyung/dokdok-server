package com.dokdok.retrospective.dto.response;

import com.dokdok.meeting.entity.Meeting;
import com.dokdok.retrospective.entity.MeetingRetrospective;
import com.dokdok.retrospective.entity.TopicRetrospectiveSummary;
import com.dokdok.topic.entity.Topic;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "모임 회고 응답")
@Builder
public record MeetingRetrospectiveResponse(
        @Schema(description = "약속 ID", example = "1")
        Long meetingId,
        @Schema(description = "약속 이름", example = "2월 첫쨋주 독서 모임")
        String meetingName,
        @Schema(description = "약속 날짜", example = "2025-02-01")
        LocalDate meetingDate,
        @Schema(description = "약속 시간", example = "14:00")
        String meetingTime,
        @Schema(description = "주제 목록")
        List<TopicResponse> topics
) {
    public static MeetingRetrospectiveResponse from (
            Meeting meeting,
            List<TopicResponse> topics
    ){
        return MeetingRetrospectiveResponse.builder()
                .meetingId(meeting.getId())
                .meetingName(meeting.getMeetingName())
                .meetingDate(meeting.getMeetingStartDate().toLocalDate())
                .meetingTime(meeting.getFormattedTime())
                .topics(topics)
                .build();
    }

    @Schema(description = "주제 회고 정보")
    @Builder
    public record TopicResponse(
            @Schema(description = "주제 ID", example = "1")
            Long topicId,
            @Schema(description = "주제 제목", example = "가짜 욕망, 유사 욕망에 대해 이야기해봅시다.")
            String topicName,
            @Schema(description = "요약된 의견 목록", example = "[\"의견1\", \"의견2\"]")
            List<String> summarizedOpinions,
            @Schema(description = "코멘트 목록")
            List<CommentResponse> comments
    ){
        public static TopicResponse from(
                Topic topic,
                TopicRetrospectiveSummary summary,
                List<CommentResponse> comments
        ) {
            return TopicResponse.builder()
                    .topicId(topic.getId())
                    .topicName(topic.getTitle())
                    .summarizedOpinions(summary.getSummarizedOpinions())
                    .comments(comments)
                    .build();
        }
    }

    @Schema(description = "모임 회고 코멘트")
    @Builder
    public record CommentResponse(
            @Schema(description = "모임 회고 ID", example = "1")
            Long meetingRetrospectiveId,
            @Schema(description = "작성자 사용자 ID", example = "1")
            Long userId,
            @Schema(description = "닉네임", example = "독서왕")
            String nickname,
            @Schema(description = "프로필 이미지 URL", example = "https://example.com/profile.jpg")
            String profileImageUrl,
            @Schema(description = "코멘트 내용", example = "이번 모임에서 핵심 논의가 잘 정리되었습니다.")
            String comment,
            @Schema(description = "작성 일시", example = "2025-02-01T16:30:00")
            LocalDateTime createdAt
    ) {

        public static CommentResponse from(MeetingRetrospective retrospective) {
            return CommentResponse.builder()
                    .meetingRetrospectiveId(retrospective.getId())
                    .userId(retrospective.getCreatedBy().getId())
                    .nickname(retrospective.getCreatedBy().getNickname())
                    .profileImageUrl(retrospective.getCreatedBy().getProfileImageUrl())
                    .comment(retrospective.getComment())
                    .createdAt(retrospective.getCreatedAt())
                    .build();
        }
    }
}

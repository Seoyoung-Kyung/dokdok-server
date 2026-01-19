package com.dokdok.retrospective.dto.response;

import com.dokdok.meeting.entity.Meeting;
import com.dokdok.retrospective.entity.MeetingRetrospective;
import com.dokdok.retrospective.entity.TopicRetrospectiveSummary;
import com.dokdok.topic.entity.Topic;
import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;


@Builder
public record MeetingRetrospectiveResponse(
        Long meetingId,
        String meetingName,
        LocalDate meetingDate,
        String meetingTime,
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

    @Builder
    public record TopicResponse(
            Long topicId,
            String topicName,
            List<String> summarizedOpinions,
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

    @Builder
    public record CommentResponse(
            Long meetingRetrospectiveId,
            Long userId,
            String nickname,
            String profileImageUrl,
            String comment,
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

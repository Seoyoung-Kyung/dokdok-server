package com.dokdok.meeting.dto;

import com.dokdok.book.entity.Book;
import com.dokdok.gathering.entity.Gathering;
import com.dokdok.meeting.entity.Meeting;
import com.dokdok.meeting.entity.MeetingMember;
import com.dokdok.meeting.entity.MeetingStatus;
import com.dokdok.topic.entity.Topic;
import com.dokdok.topic.entity.TopicStatus;
import com.dokdok.topic.entity.TopicType;
import com.dokdok.user.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;

@Schema(description = "약속 상세 응답")
public record MeetingResponse(
        @Schema(description = "약속 ID", example = "1")
        Long meetingId,

        @Schema(description = "약속 이름", example = "1월 독서 모임")
        String meetingName,

        @Schema(description = "약속 상태", example = "CONFIRMED")
        MeetingStatus meetingStatus,

        @Schema(description = "모임 정보")
        GatheringInfo gathering,

        @Schema(description = "책 정보")
        BookInfo book,

        @Schema(description = "일정 정보")
        ScheduleInfo schedule,

        @Schema(description = "장소", example = "강남역 스타벅스")
        String place,

        @Schema(description = "참가자 정보")
        ParticipantsInfo participants,

        @Schema(description = "주제 목록")
        List<TopicInfo> topics
) {

    public static MeetingResponse from(Meeting meeting, List<MeetingMember> meetingMembers, List<Topic> topics) {
        List<MeetingMember> safeMembers = meetingMembers == null ? Collections.emptyList() : meetingMembers;
        List<Topic> safeTopics = topics == null ? Collections.emptyList() : topics;

        return new MeetingResponse(
                meeting.getId(),
                meeting.getMeetingName(),
                meeting.getMeetingStatus(),
                GatheringInfo.from(meeting.getGathering()),
                BookInfo.from(meeting.getBook()),
                ScheduleInfo.from(meeting.getMeetingStartDate(), meeting.getMeetingEndDate()),
                meeting.getPlace(),
                ParticipantsInfo.from(safeMembers, meeting.getMaxParticipants()),
                safeTopics.stream().map(TopicInfo::from).toList()
        );
    }

    @Schema(description = "모임 정보")
    public record GatheringInfo(
            @Schema(description = "모임 ID", example = "1")
            Long gatheringId,

            @Schema(description = "모임 이름", example = "독서 모임")
            String gatheringName
    ) {
        public static GatheringInfo from(Gathering gathering) {
            if (gathering == null) {
                return null;
            }
            return new GatheringInfo(gathering.getId(), gathering.getGatheringName());
        }
    }

    @Schema(description = "책 정보")
    public record BookInfo(
            @Schema(description = "책 ID", example = "1")
            Long bookId,

            @Schema(description = "책 이름", example = "클린 코드")
            String bookName
    ) {
        public static BookInfo from(Book book) {
            if (book == null) {
                return null;
            }
            return new BookInfo(book.getId(), book.getBookName());
        }
    }

    @Schema(description = "일정 정보")
    public record ScheduleInfo(
            @Schema(description = "약속 날짜", example = "2025-02-01")
            LocalDate date,

            @Schema(description = "약속 시간", example = "14:00:00")
            LocalTime time,

            @Schema(description = "시작 일시", example = "2025-02-01T14:00:00")
            LocalDateTime startDateTime,

            @Schema(description = "종료 일시", example = "2025-02-01T16:00:00")
            LocalDateTime endDateTime
    ) {
        public static ScheduleInfo from(LocalDateTime start, LocalDateTime end) {
            if (start == null && end == null) {
                return null;
            }
            LocalDate date = start != null ? start.toLocalDate() : null;
            LocalTime time = start != null ? start.toLocalTime() : null;
            return new ScheduleInfo(date, time, start, end);
        }
    }

    @Schema(description = "참가자 정보")
    public record ParticipantsInfo(
            @Schema(description = "현재 참가자 수", example = "5")
            Integer currentCount,

            @Schema(description = "최대 참가자 수", example = "10")
            Integer maxCount,

            @Schema(description = "참가자 목록")
            List<MemberInfo> members
    ) {
        public static ParticipantsInfo from(List<MeetingMember> meetingMembers, Integer maxCount) {
            List<MemberInfo> members = meetingMembers.stream()
                    .filter(member -> member.getCanceledAt() == null)
                    .map(MemberInfo::from)
                    .toList();

            return new ParticipantsInfo(members.size(), maxCount, members);
        }
    }

    @Schema(description = "참가자 정보")
    public record MemberInfo(
            @Schema(description = "사용자 ID", example = "1")
            Long userId,

            @Schema(description = "닉네임", example = "독서왕")
            String nickname,

            @Schema(description = "프로필 이미지 URL", example = "https://example.com/profile.jpg")
            String profileImageUrl
    ) {
        public static MemberInfo from(MeetingMember meetingMember) {
            User user = meetingMember.getUser();
            return new MemberInfo(user.getId(), user.getNickname(), user.getProfileImageUrl());
        }
    }

    @Schema(description = "주제 정보")
    public record TopicInfo(
            @Schema(description = "주제 ID", example = "1")
            Long topicId,

            @Schema(description = "주제 제목", example = "1장 깨끗한 코드")
            String title,

            @Schema(description = "주제 타입", example = "FREE")
            TopicType topicType,

            @Schema(description = "주제 상태", example = "SELECTED")
            TopicStatus topicStatus,

            @Schema(description = "투표 수", example = "3")
            Integer voteCount
    ) {
        public static TopicInfo from(Topic topic) {
            return new TopicInfo(
                    topic.getId(),
                    topic.getTitle(),
                    topic.getTopicType(),
                    topic.getTopicStatus(),
                    topic.getLikeCount()
            );
        }
    }
}

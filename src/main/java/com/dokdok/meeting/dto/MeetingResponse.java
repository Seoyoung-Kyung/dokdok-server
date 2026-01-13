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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;

public record MeetingResponse(
        Long meetingId,
        String meetingName,
        MeetingStatus meetingStatus,
        GatheringInfo gathering,
        BookInfo book,
        ScheduleInfo schedule,
        String place,
        ParticipantsInfo participants,
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

    public record GatheringInfo(Long gatheringId, String gatheringName) {
        public static GatheringInfo from(Gathering gathering) {
            if (gathering == null) {
                return null;
            }
            return new GatheringInfo(gathering.getId(), gathering.getGatheringName());
        }
    }

    public record BookInfo(Long bookId, String bookName) {
        public static BookInfo from(Book book) {
            if (book == null) {
                return null;
            }
            return new BookInfo(book.getId(), book.getBookName());
        }
    }

    public record ScheduleInfo(
            LocalDate date,
            LocalTime time,
            LocalDateTime startDateTime,
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

    public record ParticipantsInfo(Integer currentCount, Integer maxCount, List<MemberInfo> members) {
        public static ParticipantsInfo from(List<MeetingMember> meetingMembers, Integer maxCount) {
            List<MemberInfo> members = meetingMembers.stream()
                    .filter(member -> member.getCanceledAt() == null)
                    .map(MemberInfo::from)
                    .toList();

            return new ParticipantsInfo(members.size(), maxCount, members);
        }
    }

    public record MemberInfo(Long userId, String nickname, String profileImageUrl) {
        public static MemberInfo from(MeetingMember meetingMember) {
            User user = meetingMember.getUser();
            return new MemberInfo(user.getId(), user.getNickname(), user.getProfileImageUrl());
        }
    }

    public record TopicInfo(
            Long topicId,
            String title,
            TopicType topicType,
            TopicStatus topicStatus,
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

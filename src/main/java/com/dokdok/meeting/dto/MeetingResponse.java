package com.dokdok.meeting.dto;

import com.dokdok.book.entity.Book;
import com.dokdok.gathering.entity.Gathering;
import com.dokdok.meeting.entity.Meeting;
import com.dokdok.meeting.entity.MeetingMember;
import com.dokdok.meeting.entity.MeetingStatus;
import com.dokdok.topic.entity.Topic;
import com.dokdok.user.entity.User;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;

@Getter
@Builder
public class MeetingResponse {

    private final Long meetingId;
    private final String meetingName;
    private final MeetingStatus meetingStatus;
    private final GatheringInfo gathering;
    private final BookInfo book;
    private final ScheduleInfo schedule;
    private final String place;
    private final ParticipantsInfo participants;
    private final List<TopicInfo> topics;

    public static MeetingResponse from(Meeting meeting, List<MeetingMember> meetingMembers, List<Topic> topics) {
        List<MeetingMember> safeMembers = meetingMembers == null ? Collections.emptyList() : meetingMembers;
        List<Topic> safeTopics = topics == null ? Collections.emptyList() : topics;

        return MeetingResponse.builder()
                .meetingId(meeting.getId())
                .meetingName(meeting.getMeetingName())
                .meetingStatus(meeting.getMeetingStatus())
                .gathering(GatheringInfo.from(meeting.getGathering()))
                .book(BookInfo.from(meeting.getBook()))
                .schedule(ScheduleInfo.from(meeting.getMeetingStartDate(), meeting.getMeetingEndDate()))
                .place(meeting.getPlace())
                .participants(ParticipantsInfo.from(safeMembers, meeting.getMaxParticipants()))
                .topics(safeTopics.stream().map(TopicInfo::from).toList())
                .build();
    }

    @Getter
    @Builder
    public static class GatheringInfo {

        private final Long gatheringId;
        private final String gatheringName;

        public static GatheringInfo from(Gathering gathering) {
            if (gathering == null) {
                return null;
            }
            return GatheringInfo.builder()
                    .gatheringId(gathering.getId())
                    .gatheringName(gathering.getGatheringName())
                    .build();
        }
    }

    @Getter
    @Builder
    public static class BookInfo {

        private final Long bookId;
        private final String bookName;

        public static BookInfo from(Book book) {
            if (book == null) {
                return null;
            }
            return BookInfo.builder()
                    .bookId(book.getId())
                    .bookName(book.getBookName())
                    .build();
        }
    }

    @Getter
    @Builder
    public static class ScheduleInfo {

        private final LocalDate date;
        private final LocalTime time;
        private final LocalDateTime startDateTime;
        private final LocalDateTime endDateTime;

        public static ScheduleInfo from(LocalDateTime start, LocalDateTime end) {
            if (start == null && end == null) {
                return null;
            }
            LocalDate date = start != null ? start.toLocalDate() : null;
            LocalTime time = start != null ? start.toLocalTime() : null;
            return ScheduleInfo.builder()
                    .date(date)
                    .time(time)
                    .startDateTime(start)
                    .endDateTime(end)
                    .build();
        }
    }

    @Getter
    @Builder
    public static class ParticipantsInfo {

        private final Integer currentCount;
        private final Integer maxCount;
        private final List<MemberInfo> members;

        public static ParticipantsInfo from(List<MeetingMember> meetingMembers, Integer maxCount) {
            List<MemberInfo> members = meetingMembers.stream()
                    .filter(member -> member.getCanceledAt() == null)
                    .map(MemberInfo::from)
                    .toList();

            return ParticipantsInfo.builder()
                    .currentCount(members.size())
                    .maxCount(maxCount)
                    .members(members)
                    .build();
        }
    }

    @Getter
    @Builder
    public static class MemberInfo {

        private final Long userId;
        private final String nickname;
        private final String profileImageUrl;

        public static MemberInfo from(MeetingMember meetingMember) {
            User user = meetingMember.getUser();
            return MemberInfo.builder()
                    .userId(user.getId())
                    .nickname(user.getNickname())
                    .profileImageUrl(user.getProfileImageUrl())
                    .build();
        }
    }

    @Getter
    @Builder
    public static class TopicInfo {

        private final Long topicId;
        private final String title;
        private final String topicType;
        private final String topicStatus;
        private final Integer voteCount;

        public static TopicInfo from(Topic topic) {
            return TopicInfo.builder()
                    .topicId(topic.getId())
                    .title(topic.getTitle())
                    .topicType(topic.getTopicType())
                    .topicStatus(topic.getTopicStatus())
                    .voteCount(topic.getVoteCount())
                    .build();
        }
    }
}

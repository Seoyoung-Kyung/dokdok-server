package com.dokdok.retrospective.dto.response;

import com.dokdok.topic.entity.Topic;
import com.dokdok.topic.entity.TopicAnswer;

import java.util.List;

public record PersonalRetrospectiveFormResponse(
        Long meetingId,
        List<PreOpinions> preOpinions,
        List<Topics> topics,
        List<MeetingMembers> meetingMembers
) {
    public record PreOpinions(
        Long topicId,
        String topicName,
        String content
    ) {

        public static PreOpinions from(Topic topic, TopicAnswer topicAnswer) {
            return new PreOpinions(
                    topic.getId(),
                    topic.getTitle(),
                    topicAnswer.getContent()
            );
        }
    }

    public record Topics(
            Long topicId,
            String topicName
    ) {
        public static Topics from(Topic topic) {
            return new Topics(
                    topic.getId(),
                    topic.getTitle()
            );
        }
    }

    public record MeetingMembers(
        Long meetingMemberId,
        String nickName
    ) {
        public static MeetingMembers of(Long meetingMemberId, String nickName) {
            return new MeetingMembers(
                    meetingMemberId,
                    nickName
            );
        }

    }

    public static PersonalRetrospectiveFormResponse of(
            Long meetingId,
            List<PreOpinions> preOpinions,
            List<Topics> topics,
            List<MeetingMembers> meetingMembers
    ) {
        return new PersonalRetrospectiveFormResponse(
                meetingId,
                preOpinions,
                topics,
                meetingMembers
        );
    }
}

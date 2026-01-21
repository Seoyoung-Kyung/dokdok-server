package com.dokdok.retrospective.dto.response;

import com.dokdok.topic.entity.Topic;
import com.dokdok.topic.entity.TopicAnswer;

import java.util.List;

public record PersonalRetrospectiveFormResponse(
        Long meetingId,
        List<PreOpinions> preOpinions,
        List<TopicInfo> topics,
        List<MemberInfo> meetingMembers
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

    public static PersonalRetrospectiveFormResponse of(
            Long meetingId,
            List<PreOpinions> preOpinions,
            List<TopicInfo> topics,
            List<MemberInfo> meetingMembers
    ) {
        return new PersonalRetrospectiveFormResponse(
                meetingId,
                preOpinions,
                topics,
                meetingMembers
        );
    }
}

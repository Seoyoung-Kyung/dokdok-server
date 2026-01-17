package com.dokdok.retrospective.service;

import com.dokdok.meeting.entity.MeetingMember;
import com.dokdok.retrospective.dto.response.PersonalRetrospectiveFormResponse;
import com.dokdok.topic.entity.Topic;
import com.dokdok.topic.entity.TopicAnswer;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class PersonalRetrospectiveFormAssembler {

    public PersonalRetrospectiveFormResponse assemble(
            Long meetingId,
            List<Topic> topics,
            List<TopicAnswer> topicAnswers,
            List<MeetingMember> meetingMembers
    ) {

        List<PersonalRetrospectiveFormResponse.Topics> topicDtos =
                topics.stream()
                        .map(PersonalRetrospectiveFormResponse.Topics::from)
                        .toList();

        Map<Long, TopicAnswer> topicAnswerMap =
                topicAnswers.stream()
                        .collect(Collectors.toMap(
                                ta -> ta.getTopic().getId(), // Key: 주제 ID (1L, 2L)
                                Function.identity() // TopicAnswer
                        ));

        List<PersonalRetrospectiveFormResponse.PreOpinions> preOpinions =
                topics.stream()
                        .filter(topic -> topicAnswerMap.containsKey(topic.getId()))
                        .map(topic ->
                                PersonalRetrospectiveFormResponse.PreOpinions.from(
                                        topic,
                                        topicAnswerMap.get(topic.getId())
                                )
                        )
                        .toList();

        List<PersonalRetrospectiveFormResponse.MeetingMembers> memberDtos =
                meetingMembers.stream()
                        .map(member ->
                                PersonalRetrospectiveFormResponse.MeetingMembers.of(
                                        member.getId(),
                                        member.getUser().getNickname()
                                )
                        )
                        .toList();

        return PersonalRetrospectiveFormResponse.of(
                meetingId,
                preOpinions,
                topicDtos,
                memberDtos
        );
    }
}
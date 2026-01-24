package com.dokdok.retrospective.service;

import com.dokdok.meeting.entity.MeetingMember;
import com.dokdok.retrospective.dto.response.*;
import com.dokdok.retrospective.entity.RetrospectiveChangedThought;
import com.dokdok.retrospective.entity.RetrospectiveFreeText;
import com.dokdok.retrospective.entity.RetrospectiveOthersPerspective;
import com.dokdok.storage.service.StorageService;
import com.dokdok.topic.entity.Topic;
import com.dokdok.topic.entity.TopicAnswer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class PersonalRetrospectiveAssembler {

    private final StorageService storageService;

    public PersonalRetrospectiveFormResponse assembleCreate(
            Long meetingId,
            List<Topic> topics,
            List<TopicAnswer> topicAnswers,
            List<MeetingMember> meetingMembers
    ) {

        List<TopicInfo> topicDtos = toTopicDtos(topics);

        Map<Long, TopicAnswer> topicAnswerMap =
                topicAnswers.stream()
                        .collect(Collectors.toMap(
                                ta -> ta.getTopic().getId(),
                                Function.identity()
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

        List<MemberInfo> memberDtos = toMemberDtos(meetingMembers);

        return PersonalRetrospectiveFormResponse.of(
                meetingId,
                preOpinions,
                topicDtos,
                memberDtos
        );
    }

    public PersonalRetrospectiveEditResponse assembleEdit(
            Long retrospectiveId,
            List<RetrospectiveChangedThought> changedThoughts,
            List<RetrospectiveOthersPerspective> othersPerspectives,
            List<RetrospectiveFreeText> freeTexts,
            List<Topic> topics,
            List<MeetingMember> meetingMembers
    ) {
        List<PersonalRetrospectiveEditResponse.ChangedThought> changedThoughtList =
                changedThoughts.stream()
                        .map(PersonalRetrospectiveEditResponse.ChangedThought::from)
                        .toList();

        List<PersonalRetrospectiveEditResponse.OthersPerspective> othersPerspectiveList =
                othersPerspectives.stream()
                        .map(PersonalRetrospectiveEditResponse.OthersPerspective::from)
                        .toList();

        List<PersonalRetrospectiveEditResponse.FreeText> freeTextList =
                freeTexts.stream()
                        .map(PersonalRetrospectiveEditResponse.FreeText::from)
                        .toList();

        List<TopicInfo> topicDtos = toTopicDtos(topics);
        List<MemberInfo> memberDtos = toMemberDtos(meetingMembers);

        return PersonalRetrospectiveEditResponse.from(
                retrospectiveId,
                changedThoughtList,
                othersPerspectiveList,
                freeTextList,
                topicDtos,
                memberDtos
        );
    }

    public PersonalRetrospectiveDetailResponse assembleView(
            Long retrospectiveId,
            List<RetrospectiveChangedThought> changedThoughts,
            List<RetrospectiveOthersPerspective> othersPerspectives,
            List<RetrospectiveFreeText> freeTexts,
            Map<Long, String> memberProfileImageMap
    ) {
        List<PersonalRetrospectiveDetailResponse.ChangedThought> changedThoughtList =
                changedThoughts.stream()
                        .map(PersonalRetrospectiveDetailResponse.ChangedThought::from)
                        .toList();

        List<PersonalRetrospectiveDetailResponse.OthersPerspective> othersPerspectiveList =
                othersPerspectives.stream()
                        .map(op ->
                                PersonalRetrospectiveDetailResponse.OthersPerspective.from(
                                        op,
                                        memberProfileImageMap.get(op.getMeetingMember().getId())
                                )
                        )
                        .toList();

        List<PersonalRetrospectiveDetailResponse.FreeText> freeTextList =
                freeTexts.stream()
                        .map(PersonalRetrospectiveDetailResponse.FreeText::from)
                        .toList();

        return PersonalRetrospectiveDetailResponse.from(
                retrospectiveId,
                changedThoughtList,
                othersPerspectiveList,
                freeTextList
        );
    }

    private List<TopicInfo> toTopicDtos(List<Topic> topics) {
        return topics.stream()
                .map(TopicInfo::from)
                .toList();
    }

    private List<MemberInfo> toMemberDtos(List<MeetingMember> meetingMembers){
        return meetingMembers.stream()
                .map(member -> {
                    String presignedUrl =
                            storageService.getPresignedProfileImage(
                                    member.getUser().getProfileImageUrl()
                            );

                    return MemberInfo.of(
                            member.getId(),
                            member.getUser().getNickname(),
                            presignedUrl
                    );
                })
                .toList();
    }

}
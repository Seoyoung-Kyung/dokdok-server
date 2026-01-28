package com.dokdok.retrospective.service;

import com.dokdok.meeting.entity.MeetingMember;
import com.dokdok.retrospective.dto.projection.ChangedThoughtProjection;
import com.dokdok.retrospective.dto.projection.FreeTextProjection;
import com.dokdok.retrospective.dto.projection.OtherPerspectiveProjection;
import com.dokdok.retrospective.dto.response.*;
import com.dokdok.retrospective.entity.PersonalMeetingRetrospective;
import com.dokdok.retrospective.entity.RetrospectiveChangedThought;
import com.dokdok.retrospective.entity.RetrospectiveFreeText;
import com.dokdok.retrospective.entity.RetrospectiveOthersPerspective;
import com.dokdok.storage.service.StorageService;
import com.dokdok.topic.entity.Topic;
import com.dokdok.topic.entity.TopicAnswer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;

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
            List<RetrospectiveFreeText> freeTexts
    ) {
        Map<Long, String> memberProfileImageMap = buildMemberProfileImageMap(othersPerspectives);

        List<PersonalRetrospectiveDetailResponse.ChangedThought> changedThoughtList =
                changedThoughts.stream()
                        .map(PersonalRetrospectiveDetailResponse.ChangedThought::from)
                        .toList();

        List<PersonalRetrospectiveDetailResponse.OthersPerspective> othersPerspectiveList =
                othersPerspectives.stream()
                        .map(op -> PersonalRetrospectiveDetailResponse.OthersPerspective.from(
                                op,
                                memberProfileImageMap.get(op.getMeetingMember().getId())
                        ))
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

    public List<RetrospectiveRecordResponse> assembleRecords(
            List<PersonalMeetingRetrospective> retrospectives,
            Map<Long, List<ChangedThoughtProjection>> changedThoughtsMap,
            Map<Long, List<OtherPerspectiveProjection>> othersPerspectivesMap,
            Map<Long, List<FreeTextProjection>> freeTextsMap
    ) {
        return retrospectives.stream()
                .map(retrospective -> {
                    Long retroId = retrospective.getId();

                    List<ChangedThoughtProjection> changedThoughts =
                            changedThoughtsMap.getOrDefault(retroId, List.of());
                    List<OtherPerspectiveProjection> othersPerspectives =
                            othersPerspectivesMap.getOrDefault(retroId, List.of());

                    List<RetrospectiveRecordResponse.TopicGroup> topicGroups =
                            buildTopicGroups(changedThoughts, othersPerspectives);

                    List<RetrospectiveRecordResponse.FreeText> freeTexts =
                            freeTextsMap.getOrDefault(retroId, List.of()).stream()
                                    .map(RetrospectiveRecordResponse.FreeText::from)
                                    .toList();

                    return RetrospectiveRecordResponse.of(
                            retroId,
                            retrospective.getMeeting().getGathering().getGatheringName(),
                            "개인 회고",
                            retrospective.getCreatedAt(),
                            topicGroups,
                            freeTexts
                    );
                })
                .toList();
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

    private Map<Long, String> buildMemberProfileImageMap(
            List<RetrospectiveOthersPerspective> othersPerspectives
    ) {
        Map<Long, String> memberProfileImageMap = new HashMap<>();
        othersPerspectives.stream()
                .map(RetrospectiveOthersPerspective::getMeetingMember)
                .distinct()
                .forEach(mm -> {
                    String profileImageUrl = mm.getUser().getProfileImageUrl();
                    String presignedUrl = profileImageUrl != null
                            ? storageService.getPresignedProfileImage(profileImageUrl)
                            : null;
                    memberProfileImageMap.put(mm.getId(), presignedUrl);
                });
        return memberProfileImageMap;
    }

    private List<RetrospectiveRecordResponse.TopicGroup> buildTopicGroups(
            List<ChangedThoughtProjection> changedThoughts,
            List<OtherPerspectiveProjection> othersPerspectives
    ) {
        Map<Long, Integer> topicIdToConfirmOrder = new LinkedHashMap<>();

        changedThoughts.forEach(ct -> {
            if (ct.topicId() != null) {
                topicIdToConfirmOrder.putIfAbsent(ct.topicId(), ct.confirmOrder());
            }
        });

        othersPerspectives.forEach(op -> {
            if (op.topicId() != null) {
                topicIdToConfirmOrder.putIfAbsent(op.topicId(), op.confirmOrder());
            }
        });

        boolean hasNullTopic = othersPerspectives.stream()
                .anyMatch(op -> op.topicId() == null);

        Map<Long, List<ChangedThoughtProjection>> changedThoughtsByTopic = changedThoughts.stream()
                .filter(ct -> ct.topicId() != null)
                .collect(groupingBy(ChangedThoughtProjection::topicId));

        Map<Long, List<OtherPerspectiveProjection>> othersPerspectivesByTopic = othersPerspectives.stream()
                .filter(op -> op.topicId() != null)
                .collect(groupingBy(OtherPerspectiveProjection::topicId));

        List<RetrospectiveRecordResponse.TopicGroup> topicGroups = topicIdToConfirmOrder.entrySet().stream()
                .sorted(Comparator.comparing(
                        Map.Entry::getValue,
                        Comparator.nullsLast(Comparator.naturalOrder())
                ))
                .map(entry -> {
                    Long topicId = entry.getKey();
                    Integer confirmOrder = entry.getValue();

                    List<RetrospectiveRecordResponse.ChangedThought> thoughts =
                            changedThoughtsByTopic.getOrDefault(topicId, List.of()).stream()
                                    .map(RetrospectiveRecordResponse.ChangedThought::from)
                                    .toList();

                    List<RetrospectiveRecordResponse.OthersPerspective> perspectives =
                            othersPerspectivesByTopic.getOrDefault(topicId, List.of()).stream()
                                    .map(RetrospectiveRecordResponse.OthersPerspective::from)
                                    .toList();

                    return new RetrospectiveRecordResponse.TopicGroup(
                            topicId,
                            confirmOrder,
                            thoughts,
                            perspectives
                    );
                })
                .collect(Collectors.toCollection(ArrayList::new));

        if (hasNullTopic) {
            List<RetrospectiveRecordResponse.OthersPerspective> nullTopicPerspectives =
                    othersPerspectives.stream()
                            .filter(op -> op.topicId() == null)
                            .map(RetrospectiveRecordResponse.OthersPerspective::from)
                            .toList();

            topicGroups.add(new RetrospectiveRecordResponse.TopicGroup(
                    null,
                    null,
                    List.of(),
                    nullTopicPerspectives
            ));
        }

        return topicGroups;
    }
}
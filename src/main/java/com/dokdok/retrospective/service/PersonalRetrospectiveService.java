package com.dokdok.retrospective.service;

import com.dokdok.book.entity.RecordType;
import com.dokdok.book.service.BookValidator;
import com.dokdok.retrospective.dto.projection.ChangedThoughtProjection;
import com.dokdok.retrospective.dto.projection.FreeTextProjection;
import com.dokdok.retrospective.dto.projection.OtherPerspectiveProjection;
import com.dokdok.retrospective.dto.response.*;
import com.dokdok.meeting.entity.MeetingMember;
import com.dokdok.meeting.repository.MeetingMemberRepository;
import com.dokdok.meeting.service.MeetingValidator;
import com.dokdok.retrospective.repository.ChangedThoughtRepository;
import com.dokdok.retrospective.repository.FreeTextRepository;
import com.dokdok.retrospective.repository.OthersPerspectiveRepository;
import com.dokdok.retrospective.repository.PersonalRetrospectiveRepository;
import com.dokdok.storage.service.StorageService;
import com.dokdok.topic.repository.TopicAnswerRepository;
import com.dokdok.topic.repository.TopicRepository;
import com.dokdok.topic.service.TopicValidator;
import com.dokdok.user.service.UserValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dokdok.global.util.SecurityUtil;
import com.dokdok.meeting.entity.Meeting;
import com.dokdok.retrospective.dto.request.PersonalRetrospectiveRequest;
import com.dokdok.retrospective.entity.PersonalMeetingRetrospective;
import com.dokdok.retrospective.entity.RetrospectiveChangedThought;
import com.dokdok.retrospective.entity.RetrospectiveFreeText;
import com.dokdok.retrospective.entity.RetrospectiveOthersPerspective;
import com.dokdok.topic.entity.Topic;
import com.dokdok.topic.entity.TopicAnswer;
import com.dokdok.user.entity.User;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PersonalRetrospectiveService {

    private final PersonalRetrospectiveRepository personalRetrospectiveRepository;
    private final ChangedThoughtRepository changedThoughtRepository;
    private final OthersPerspectiveRepository othersPerspectiveRepository;
    private final FreeTextRepository freeTextRepository;
    private final TopicRepository topicRepository;
    private final MeetingMemberRepository meetingMemberRepository;
    private final TopicAnswerRepository topicAnswerRepository;
    private final MeetingValidator meetingValidator;
    private final UserValidator userValidator;
    private final RetrospectiveValidator retrospectiveValidator;
    private final TopicValidator topicValidator;
    private final BookValidator bookValidator;
    private final PersonalRetrospectiveAssembler assembler;
    private final StorageService storageService;

    @Transactional
    public PersonalRetrospectiveResponse createPersonalRetrospective(Long meetingId, PersonalRetrospectiveRequest request) {
        Long userId = SecurityUtil.getCurrentUserId();

        Meeting meeting = meetingValidator.findMeetingOrThrow(meetingId);
        User user = userValidator.findUserOrThrow(userId);

        retrospectiveValidator.validateRetrospective(meetingId, userId);

        PersonalMeetingRetrospective retrospective = PersonalMeetingRetrospective.create(meeting, user);

        setRetrospectiveData(retrospective, request, meetingId, userId);

        PersonalMeetingRetrospective saved = personalRetrospectiveRepository.save(retrospective);

        return PersonalRetrospectiveResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public PersonalRetrospectiveFormResponse getPersonalRetrospectiveForm(Long meetingId) {

        Long userId = SecurityUtil.getCurrentUserId();

        meetingValidator.validateMeeting(meetingId);
        meetingValidator.validateMeetingMember(meetingId, userId);
        retrospectiveValidator.validateRetrospective(meetingId, userId);

        List<Topic> topics = topicValidator.getConfirmedTopics(meetingId);
        List<TopicAnswer> topicAnswers =  topicAnswerRepository.findByMeetingIdUserId(meetingId, userId);
        List<MeetingMember> meetingMembers = meetingMemberRepository.findOtherMembersByMeetingId(meetingId, userId);

        return assembler.assembleCreate(
                meetingId,
                topics,
                topicAnswers,
                meetingMembers
        );
    }

    @Transactional(readOnly = true)
    public PersonalRetrospectiveEditResponse getPersonalRetrospectiveEditForm(
            Long meetingId,
            Long retrospectiveId
    ) {

        Long userId = SecurityUtil.getCurrentUserId();

        meetingValidator.validateMeeting(meetingId);
        meetingValidator.validateMeetingMember(meetingId, userId);
        retrospectiveValidator.validateRetrospective(retrospectiveId);

        List<RetrospectiveChangedThought> changedThoughts
                = changedThoughtRepository.findByPersonalMeetingRetrospective(retrospectiveId);

        List<RetrospectiveOthersPerspective> othersPerspectives
                = othersPerspectiveRepository.findByPersonalMeetingRetrospective(retrospectiveId);

        List<RetrospectiveFreeText> freeTexts =
                freeTextRepository.findByPersonalMeetingRetrospective_Id(retrospectiveId);

        List<Topic> topics = topicValidator.getConfirmedTopics(meetingId);

        List<MeetingMember> meetingMembers
                = meetingMemberRepository.findOtherMembersByMeetingId(meetingId, userId);

        return assembler.assembleEdit(
                retrospectiveId,
                changedThoughts,
                othersPerspectives,
                freeTexts,
                topics,
                meetingMembers
        );
    }

    @Transactional
    public PersonalRetrospectiveResponse editPersonalRetrospective(
            Long meetingId,
            Long retrospectiveId,
            PersonalRetrospectiveRequest request
    ) {
        Long userId = SecurityUtil.getCurrentUserId();

        meetingValidator.validateMeeting(meetingId);
        meetingValidator.validateMeetingMember(meetingId, userId);

        PersonalMeetingRetrospective retrospective
                = retrospectiveValidator.getRetrospective(retrospectiveId, userId);

        retrospective.clearChangedThoughts();
        retrospective.clearOthersPerspectives();
        retrospective.clearFreeTexts();

        setRetrospectiveData(retrospective, request, meetingId, userId);

        PersonalMeetingRetrospective saved = personalRetrospectiveRepository.save(retrospective);

        return PersonalRetrospectiveResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<RetrospectiveRecordResponse> getRetrospectiveRecords(Long bookId) {
        Long userId = SecurityUtil.getCurrentUserId();

        bookValidator.validateBook(bookId);

        List<PersonalMeetingRetrospective> retrospectives
                = retrospectiveValidator.getRetrospectives(bookId, userId);

        List<Long> retrospectiveIds = retrospectives.stream()
                .map(PersonalMeetingRetrospective::getId)
                .toList();

        if(retrospectiveIds.isEmpty()) {
            return List.of();
        }

        Map<Long, List<ChangedThoughtProjection>> changedThoughtsMap =
                changedThoughtRepository.findByRetrospectiveIds(retrospectiveIds)
                        .stream()
                        .collect(groupingBy(ChangedThoughtProjection::retrospectiveId));

        Map<Long, List<OtherPerspectiveProjection>> othersPerspectivesMap =
                othersPerspectiveRepository.findByRetrospectiveIds(retrospectiveIds)
                        .stream()
                        .collect(groupingBy(OtherPerspectiveProjection::retrospectiveId));

        Map<Long, List<FreeTextProjection>> freeTextsMap =
                freeTextRepository.findByRetrospectiveIds(retrospectiveIds)
                        .stream()
                        .collect(groupingBy(FreeTextProjection::retrospectiveId));

        return retrospectives.stream()
                .map(retrospective -> RetrospectiveRecordResponse.of(
                        retrospective.getId(),
                        retrospective.getMeeting().getGathering().getGatheringName(),
                        RecordType.RETROSPECTIVE,
                        retrospective.getCreatedAt(),
                        changedThoughtsMap.getOrDefault(retrospective.getId(), List.of())
                                .stream().map(RetrospectiveRecordResponse.ChangedThought::from).toList(),
                        othersPerspectivesMap.getOrDefault(retrospective.getId(), List.of())
                                .stream().map(RetrospectiveRecordResponse.OthersPerspective::from).toList(),
                        freeTextsMap.getOrDefault(retrospective.getId(), List.of())
                                .stream().map(RetrospectiveRecordResponse.FreeText::from).toList()
                ))
                .toList();
    }

    @Transactional
    public void deletePersonalRetrospective(Long meetingId, Long retrospectiveId) {
        Long userId = SecurityUtil.getCurrentUserId();

        meetingValidator.validateMeeting(meetingId);
        meetingValidator.validateMeetingMember(meetingId, userId);

        PersonalMeetingRetrospective retrospective
                = retrospectiveValidator.getRetrospective(retrospectiveId, userId);

        retrospective.softDelete();
    }

    @Transactional(readOnly = true)
    public PersonalRetrospectiveDetailResponse getPersonalRetrospective(
            Long meetingId,
            Long retrospectiveId
    ) {

        Long userId = SecurityUtil.getCurrentUserId();

        meetingValidator.validateMeeting(meetingId);
        meetingValidator.validateMeetingMember(meetingId, userId);
        retrospectiveValidator.validateRetrospective(retrospectiveId, userId);

        List<RetrospectiveChangedThought> changedThoughts
                = changedThoughtRepository.findByPersonalMeetingRetrospective(retrospectiveId);

        List<RetrospectiveOthersPerspective> othersPerspectives
                = othersPerspectiveRepository.findByPersonalMeetingRetrospective(retrospectiveId);

        List<RetrospectiveFreeText> freeTexts =
                freeTextRepository.findByPersonalMeetingRetrospective_Id(retrospectiveId);

        Map<Long, String> memberProfileImageMap =
                othersPerspectives.stream()
                        .map(RetrospectiveOthersPerspective::getMeetingMember)
                        .distinct()
                        .collect(Collectors.toMap(
                                MeetingMember::getId,
                                mm -> storageService.getPresignedProfileImage(
                                        mm.getUser().getProfileImageUrl()
                                )
                        ));

        return assembler.assembleView(
                retrospectiveId,
                changedThoughts,
                othersPerspectives,
                freeTexts,
                memberProfileImageMap
        );
    }

    @Transactional(readOnly = true)
    public PersonalRetrospectiveDetailResponse getPersonalRetrospective(
            Long meetingId,
            Long retrospectiveId
    ) {

        Long userId = SecurityUtil.getCurrentUserId();

        meetingValidator.validateMeeting(meetingId);
        meetingValidator.validateMeetingMember(meetingId, userId);
        retrospectiveValidator.validateRetrospective(retrospectiveId);

        List<RetrospectiveChangedThought> changedThoughts
                = changedThoughtRepository.findByPersonalMeetingRetrospective(retrospectiveId);

        List<RetrospectiveOthersPerspective> othersPerspectives
                = othersPerspectiveRepository.findByPersonalMeetingRetrospective(retrospectiveId);

        List<RetrospectiveFreeText> freeTexts =
                freeTextRepository.findByPersonalMeetingRetrospective_Id(retrospectiveId);

        List<Topic> topics = topicValidator.getConfirmedTopics(meetingId);

        List<MeetingMember> meetingMembers
                = meetingMemberRepository.findOtherMembersByMeetingId(meetingId, userId);

        return assembler.assembleDetail(
                retrospectiveId,
                changedThoughts,
                othersPerspectives,
                freeTexts,
                topics,
                meetingMembers
        );
    }

    private void setRetrospectiveData(
            PersonalMeetingRetrospective retrospective,
            PersonalRetrospectiveRequest request,
            Long meetingId,
            Long userId
    ) {
        // ChangedThoughts 추가
        if (request.changedThoughts() != null) {
            for (var thought : request.changedThoughts()) {
                Topic topic = topicValidator.getTopicInMeeting(thought.topicId(), meetingId);

                // preOpinion은 TopicAnswer에서 조회
                TopicAnswer topicAnswer = topicAnswerRepository.findPreOpinion(topic.getId(), userId);
                String preOpinion = topicAnswer != null ? topicAnswer.getContent() : null;

                RetrospectiveChangedThought changedThought = RetrospectiveChangedThought.create(
                        topic,
                        retrospective,
                        thought.keyIssue(),
                        preOpinion,
                        thought.postOpinion()
                );

                retrospective.addChangedThought(changedThought);
            }
        }

        // OthersPerspectives 추가
        if (request.othersPerspectives() != null) {
            for (var perspective : request.othersPerspectives()) {
                Optional<Topic> topic = Optional.ofNullable(perspective.topicId())
                        .flatMap(topicRepository::findById);

                MeetingMember meetingMember = meetingValidator.getMeetingMember(meetingId, perspective.meetingMemberId());

                RetrospectiveOthersPerspective othersPerspective = RetrospectiveOthersPerspective.create(
                        retrospective,
                        topic.orElse(null),
                        meetingMember,
                        perspective.opinionContent(),
                        perspective.impressiveReason()
                );

                retrospective.addOthersPerspective(othersPerspective);
            }
        }

        // FreeTexts 추가
        if (request.freeTexts() != null) {
            for (var freeText : request.freeTexts()) {
                RetrospectiveFreeText text = RetrospectiveFreeText.of(
                        retrospective,
                        freeText.title(),
                        freeText.content()
                );

                retrospective.addFreeText(text);
            }
        }
    }
}
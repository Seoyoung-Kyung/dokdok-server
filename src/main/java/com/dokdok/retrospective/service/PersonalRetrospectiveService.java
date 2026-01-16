package com.dokdok.retrospective.service;

import com.dokdok.meeting.entity.MeetingMember;
import com.dokdok.meeting.service.MeetingValidator;
import com.dokdok.retrospective.dto.response.PersonalRetrospectiveResponse;
import com.dokdok.retrospective.repository.PersonalRetrospectiveRepository;
import com.dokdok.topic.repository.TopicRepository;
import com.dokdok.topic.service.TopicValidator;
import com.dokdok.user.service.UserValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class PersonalRetrospectiveService {

    private final PersonalRetrospectiveRepository personalRetrospectiveRepository;
    private final TopicRepository topicRepository;
    private final MeetingValidator meetingValidator;
    private final UserValidator userValidator;
    private final RetrospectiveValidator retrospectiveValidator;
    private final TopicValidator topicValidator;

    @Transactional
    public PersonalRetrospectiveResponse createPersonalRetrospective(Long meetingId, PersonalRetrospectiveRequest request) {
        Long userId = SecurityUtil.getCurrentUserId();

        Meeting meeting = meetingValidator.findMeetingOrThrow(meetingId);

        User user = userValidator.findUserOrThrow(userId);

        retrospectiveValidator.validateRetrospective(meetingId, userId);

        PersonalMeetingRetrospective retrospective = PersonalMeetingRetrospective.of(meeting, user);

        // ChangedThoughts 추가
        if (request.changedThoughts() != null) {
            for (var thought : request.changedThoughts()) {
                Topic topic = topicValidator.getTopic(thought.topicId());

                // preOpinion은 TopicAnswer에서 조회
                TopicAnswer topicAnswer = topicValidator.getPreOpinion(topic.getId(), userId);
                String preOpinion = topicAnswer != null ? topicAnswer.getContent() : null;

                RetrospectiveChangedThought changedThought = RetrospectiveChangedThought.of(
                        topic,
                        retrospective,
                        thought.keyIssue(),
                        preOpinion,
                        thought.postOpinion(),
                        thought.sortOrder()
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

                RetrospectiveOthersPerspective othersPerspective = RetrospectiveOthersPerspective.of(
                        retrospective,
                        topic.orElse(null),
                        meetingMember,
                        perspective.opinionContent(),
                        perspective.impressiveReason(),
                        perspective.sortOrder()
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
                        freeText.content(),
                        freeText.sortOrder()
                );

                retrospective.addFreeText(text);
            }
        }

        PersonalMeetingRetrospective saved = personalRetrospectiveRepository.save(retrospective);

        return PersonalRetrospectiveResponse.from(saved);
    }
}
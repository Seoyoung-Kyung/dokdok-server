package com.dokdok.retrospective.entity;

import com.dokdok.global.BaseTimeEntity;
import com.dokdok.meeting.entity.MeetingMember;
import com.dokdok.topic.entity.Topic;
import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLRestriction;

import java.util.Optional;

@Entity
@Table(name = "retrospective_others_perspective")
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLRestriction("deleted_at IS NULL")
public class RetrospectiveOthersPerspective extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "others_perspective_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "personal_meeting_retrospective_id", nullable = false)
    private PersonalMeetingRetrospective personalMeetingRetrospective;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "topic_id")
    private Topic topic;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meeting_member_id", nullable = false)
    private MeetingMember meetingMember;

    @Column(name = "opinion_content", columnDefinition = "TEXT")
    private String opinionContent;

    @Column(name = "impressive_reason", columnDefinition = "TEXT")
    private String impressiveReason;

    public static RetrospectiveOthersPerspective create(
            PersonalMeetingRetrospective personalMeetingRetrospective,
            @Nullable Topic topic,
            MeetingMember meetingMember,
            String opinionContent,
            String impressiveReason
    ) {
        return RetrospectiveOthersPerspective.builder()
                .personalMeetingRetrospective(personalMeetingRetrospective)
                .topic(topic)
                .meetingMember(meetingMember)
                .opinionContent(opinionContent)
                .impressiveReason(impressiveReason)
                .build();
    }

    // 연관관계 편의 메서드
    protected void setPersonalMeetingRetrospective(PersonalMeetingRetrospective personalMeetingRetrospective) {
        this.personalMeetingRetrospective = personalMeetingRetrospective;
    }

    public void softDelete() {
        if (!isDeleted()) {
            markDeletedNow();
        }
    }
}
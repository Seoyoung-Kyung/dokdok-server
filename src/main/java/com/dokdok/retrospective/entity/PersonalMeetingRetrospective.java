package com.dokdok.retrospective.entity;

import com.dokdok.global.BaseTimeEntity;
import com.dokdok.meeting.entity.Meeting;
import com.dokdok.retrospective.exception.RetrospectiveErrorCode;
import com.dokdok.retrospective.exception.RetrospectiveException;
import com.dokdok.topic.exception.TopicErrorCode;
import com.dokdok.topic.exception.TopicException;
import com.dokdok.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "personal_meeting_retrospective")
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLDelete(sql = "UPDATE personal_meeting_retrospective SET deleted_at = NOW() WHERE personal_meeting_retrospective_id = ?")
@SQLRestriction("deleted_at IS NULL")
public class PersonalMeetingRetrospective extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "personal_meeting_retrospective_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meeting_id", nullable = false)
    private Meeting meeting;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "personalMeetingRetrospective", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<RetrospectiveOthersPerspective> othersPerspectives = new ArrayList<>();

    @OneToMany(mappedBy = "personalMeetingRetrospective", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<RetrospectiveFreeText> freeTexts = new ArrayList<>();

    @OneToMany(mappedBy = "personalMeetingRetrospective", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<RetrospectiveChangedThought> changedThoughts = new ArrayList<>();

    public static PersonalMeetingRetrospective create(Meeting meeting, User user) {
        return PersonalMeetingRetrospective.builder()
                .meeting(meeting)
                .user(user)
                .build();
    }

    // 연관관계 편의 메서드
    public void addOthersPerspective(RetrospectiveOthersPerspective othersPerspective) {
        this.othersPerspectives.add(othersPerspective);
        othersPerspective.setPersonalMeetingRetrospective(this);
    }

    public void addFreeText(RetrospectiveFreeText freeText) {
        this.freeTexts.add(freeText);
        freeText.setPersonalMeetingRetrospective(this);
    }

    public void addChangedThought(RetrospectiveChangedThought changedThought) {
        this.changedThoughts.add(changedThought);
        changedThought.setPersonalMeetingRetrospective(this);
    }

    public void clearChangedThoughts() {
        this.changedThoughts.clear();
    }

    public void clearOthersPerspectives() {
        this.othersPerspectives.clear();
    }

    public void clearFreeTexts() {
        this.freeTexts.clear();
    }

    public void softDelete() {
        if (isDeleted()) {
            throw new RetrospectiveException(RetrospectiveErrorCode.RETROSPECTIVE_ALREADY_DELETED);
        }

        markDeletedNow();

        changedThoughts.forEach(RetrospectiveChangedThought::softDelete);
        freeTexts.forEach(RetrospectiveFreeText::softDelete);
        othersPerspectives.forEach(RetrospectiveOthersPerspective::softDelete);
    }
}
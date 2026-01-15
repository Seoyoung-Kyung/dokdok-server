package com.dokdok.retrospective.entity;

import com.dokdok.global.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "retrospective_others_perspective")
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLDelete(sql = "UPDATE retrospective_others_perspective SET deleted_at = NOW() WHERE others_perspective_id = ?")
@SQLRestriction("deleted_at IS NULL")
public class RetrospectiveOthersPerspective extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "others_perspective_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "personal_meeting_retrospective_id", nullable = false)
    private PersonalMeetingRetrospective personalMeetingRetrospective;

    @Column(name = "topic_name", length = 255)
    private String topicName;

    @Column(name = "opinion_content", columnDefinition = "TEXT")
    private String opinionContent;

    @Column(name = "impressive_reason", columnDefinition = "TEXT")
    private String impressiveReason;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    public static RetrospectiveOthersPerspective of(
            PersonalMeetingRetrospective personalMeetingRetrospective,
            String topicName,
            String opinionContent,
            String impressiveReason,
            Integer sortOrder
    ) {
        return RetrospectiveOthersPerspective.builder()
                .personalMeetingRetrospective(personalMeetingRetrospective)
                .topicName(topicName)
                .opinionContent(opinionContent)
                .impressiveReason(impressiveReason)
                .sortOrder(sortOrder)
                .build();
    }

    // 연관관계 편의 메서드
    protected void setPersonalMeetingRetrospective(PersonalMeetingRetrospective personalMeetingRetrospective) {
        this.personalMeetingRetrospective = personalMeetingRetrospective;
    }

    // 비즈니스 메서드
    public void updateContent(String topicName, String opinionContent, String impressiveReason) {
        this.topicName = topicName;
        this.opinionContent = opinionContent;
        this.impressiveReason = impressiveReason;
    }

    public void updateSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }
}
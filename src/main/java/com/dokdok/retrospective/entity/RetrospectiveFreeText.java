package com.dokdok.retrospective.entity;

import com.dokdok.global.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;

@Entity
@Table(name = "retrospective_free_text")
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLDelete(sql = "UPDATE retrospective_free_text SET deleted_at = NOW() WHERE free_text_id = ?")
@SQLRestriction("deleted_at IS NULL")
public class RetrospectiveFreeText extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "free_text_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "personal_meeting_retrospective_id", nullable = false)
    private PersonalMeetingRetrospective personalMeetingRetrospective;

    @Column(name = "title", length = 255, nullable = false)
    private String title;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Builder
    public static RetrospectiveFreeText of(
            PersonalMeetingRetrospective personalMeetingRetrospective,
            String title,
            String content,
            Integer sortOrder
    ) {
        return RetrospectiveFreeText.builder()
                .personalMeetingRetrospective(personalMeetingRetrospective)
                .title(title)
                .content(content)
                .sortOrder(sortOrder)
                .build();
    }

    // 연관관계 편의 메서드
    protected void setPersonalMeetingRetrospective(PersonalMeetingRetrospective personalMeetingRetrospective) {
        this.personalMeetingRetrospective = personalMeetingRetrospective;
    }

    // 비즈니스 메서드
    public void updateContent(String title, String content) {
        this.title = title;
        this.content = content;
    }

    public void updateSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }
}
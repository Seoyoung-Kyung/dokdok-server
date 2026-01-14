package com.dokdok.topic.entity;

import com.dokdok.global.BaseTimeEntity;
import com.dokdok.meeting.entity.Meeting;
import com.dokdok.topic.exception.TopicErrorCode;
import com.dokdok.topic.exception.TopicException;
import com.dokdok.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import static com.dokdok.topic.entity.TopicStatus.PROPOSED;

@Entity
@Table(name = "topic")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@SuperBuilder
@SQLDelete(sql = "UPDATE topic SET deleted_at = CURRENT_TIMESTAMP WHERE topic_id = ?")
// @SQLRestriction("deleted_at IS NULL")
public class Topic extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "topic_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meeting_id", nullable = false)
    private Meeting meeting;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proposed_by", nullable = false)
    private User proposedBy;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "topic_type", nullable = false, length = 30)
    @Enumerated(EnumType.STRING)
    private TopicType topicType;

    @Column(name = "topic_status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private TopicStatus topicStatus = PROPOSED;

    @Column(name = "like_count", nullable = false)
    @Builder.Default
    private Integer likeCount = 0;

    @Column(name = "confirm_order")
    private Integer confirmOrder;

    public static Topic create(

            Meeting meeting,
            User user,
            String title,
            String description,
            TopicType topicType
    ) {
        return Topic.builder()
                .meeting(meeting)
                .proposedBy(user)
                .title(title)
                .description(description)
                .topicType(topicType)
                .build();
    }

    public void updateStatus(TopicStatus topicStatus) {
        this.topicStatus = topicStatus;
    }

    public void updateConfirmOrder(Integer confirmOrder) {
        this.confirmOrder = confirmOrder;
    }

    public void softDelete() {
        if (isDeleted()) {
            throw new TopicException(TopicErrorCode.TOPIC_ALREADY_DELETED);
        }

        markDeletedNow();
    }

}
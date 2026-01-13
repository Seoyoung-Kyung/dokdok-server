package com.dokdok.topic.entity;

import com.dokdok.global.BaseTimeEntity;
import com.dokdok.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;

@Entity
@Table(name = "topic_answer")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@SuperBuilder
@SQLDelete(sql = "UPDATE topic_answer SET deleted_at = CURRENT_TIMESTAMP WHERE topic_answer_id = ?")
@SQLRestriction("deleted_at IS NULL")
public class TopicAnswer extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "topic_answer_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "topic_id", nullable = false)
    private Topic topic;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "book_rating", precision = 2, scale = 1)
    private BigDecimal bookRating;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Column(name = "is_submitted", nullable = false)
    @Builder.Default
    private Boolean isSubmitted = false;

    public static TopicAnswer create(Topic topic, User user, String content) {
        return TopicAnswer.builder()
                .topic(topic)
                .user(user)
                .content(content)
                .build();
    }

    public void updateContent(String content) {
        this.content = content;
    }

    public void submit() {
        this.isSubmitted = true;
    }
}

package com.dokdok.gathering.entity;

import com.dokdok.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Entity
@Table(name = "gathering_member")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@SQLRestriction("removed_at IS NULL")
public class GatheringMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "gathering_member_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gathering_id", nullable = false)
    private Gathering gathering;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "is_favorite", nullable = false)
    @Builder.Default
    private Boolean isFavorite = false;

    @Column(name = "role", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private GatheringRole role = GatheringRole.MEMBER;

    @CreatedDate
    @Column(name = "joined_at", nullable = false, updatable = false)
    private LocalDateTime joinedAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "removed_at")
    private LocalDateTime removedAt;

    /**
     * 가입일로부터 경과한 일수를 계산합니다.
     */
    public Integer getDaysFromJoined(){
        if(this.joinedAt == null){
            return 0;
        }
        return (int) ChronoUnit.DAYS.between(
                this.joinedAt.toLocalDate(),
                LocalDate.now()
        );
    }

    public void remove() {
        this.removedAt = LocalDateTime.now();
    }
}
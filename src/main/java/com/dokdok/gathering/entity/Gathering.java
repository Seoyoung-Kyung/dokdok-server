package com.dokdok.gathering.entity;

import com.dokdok.global.BaseTimeEntity;
import com.dokdok.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Entity
@Table(name = "gathering")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@SuperBuilder
@SQLDelete(sql = "UPDATE gathering SET deleted_at = CURRENT_TIMESTAMP, gathering_status = 'INACTIVE' WHERE gathering_id = ?")
@SQLRestriction("deleted_at IS NULL")
public class Gathering extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "gathering_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gathering_leader_id", nullable = false)
    private User gatheringLeader;

    @Column(name = "gathering_name", nullable = false, length = 255)
    private String gatheringName;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "invitation_link", nullable = false, length = 255, unique = true)
    private String invitationLink;

    @Column(name = "gathering_status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private GatheringStatus gatheringStatus = GatheringStatus.ACTIVE;

    public static Gathering of(String gatheringName, String description, String invitationLink, User gatheringLeader) {
        return Gathering.builder()
                .gatheringName(gatheringName)
                .description(description)
                .invitationLink(invitationLink)
                .gatheringLeader(gatheringLeader)
                .build();
    }

    /**
     * 생성일일로부터 경과한 일수를 계산합니다.
     */
    public Integer getDaysFromCreation(){
        if(this.getCreatedAt()== null){
            return 0;
        }
        return (int) ChronoUnit.DAYS.between(
                this.getCreatedAt().toLocalDate(), LocalDate.now()
        );
    }

    /**
     * 모임 정보를 수정합니다.
     */
    public void updateGatheringInfo(String gatheringName, String description){
        this.gatheringName = gatheringName;
        if(description != null){
            this.description = description;
        }
    }

    public void deleteGathering() {
        this.gatheringStatus = GatheringStatus.INACTIVE;
        this.markDeletedNow();
    }
}

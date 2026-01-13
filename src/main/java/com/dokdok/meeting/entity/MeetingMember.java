package com.dokdok.meeting.entity;

import com.dokdok.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "meeting_member")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class MeetingMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "meeting_member_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meeting_id", nullable = false)
    private Meeting meeting;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "attendance_status", nullable = false, length = 20)
    @Builder.Default
    private String attendanceStatus = "PENDING";

    @Column(name = "meeting_role", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private MeetingMemberRole meetingRole = MeetingMemberRole.MEMBER;

    @CreatedDate
    @Column(name = "joined_at", nullable = false, updatable = false)
    private LocalDateTime joinedAt;

    @Column(name = "cancled_at")
    private LocalDateTime canceledAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public void changeRole(MeetingMemberRole meetingRole) {
        this.meetingRole = meetingRole;
    }

    public void cancel() {
        this.canceledAt = LocalDateTime.now();
    }

    public void restore() {
        this.canceledAt = null;
    }
}

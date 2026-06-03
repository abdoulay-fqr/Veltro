package com.veltro.messaging.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "conversation",
       uniqueConstraints = @UniqueConstraint(columnNames = {"member_id", "coach_id"}))
public class Conversation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "coach_id", nullable = false)
    private Long coachId;

    @Column(name = "last_message_at")
    private LocalDateTime lastMessageAt;

    @Column(name = "last_message_preview", length = 100)
    private String lastMessagePreview;

    @Column(name = "unread_count_member", nullable = false)
    private int unreadCountMember = 0;

    @Column(name = "unread_count_coach", nullable = false)
    private int unreadCountCoach = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}

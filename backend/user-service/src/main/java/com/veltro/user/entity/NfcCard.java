package com.veltro.user.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "nfc_card")
public class NfcCard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_profile_id", nullable = false)
    private MemberProfile memberProfile;

    // e.g. "card001"..."card005" — matches Node-RED virtual card UIDs
    @Column(name = "card_uid", nullable = false, unique = true)
    private String cardUid;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NfcCardStatus status = NfcCardStatus.ACTIVE;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
package com.irs1099.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;

    @Column(nullable = false)
    private String subject;

    @Column(columnDefinition = "TEXT")
    private String body;

    @Builder.Default
    private boolean emailSent = false;

    private LocalDateTime readAt;

    @CreationTimestamp
    private LocalDateTime createdAt;

    public enum NotificationType {
        REGISTRATION,
        EMAIL_VERIFICATION,
        PASSWORD_RESET,
        PAYMENT_CONFIRMATION,
        SUBMISSION_SUBMITTED,
        SUBMISSION_ACCEPTED,
        SUBMISSION_REJECTED,
        SUBMISSION_ERRORS,
        DEADLINE_REMINDER,
        SUBSCRIPTION_RENEWAL,
        SUBSCRIPTION_EXPIRED,
        SYSTEM
    }
}

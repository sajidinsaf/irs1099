package com.irs1099.entity;

import javax.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "subscriptions")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PlanType planType;

    private String stripeSubscriptionId;
    private String stripeCustomerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubscriptionStatus status;

    private LocalDate startDate;
    private LocalDate endDate;

    private int formsIncluded;
    private int formsUsed;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public enum PlanType {
        PAY_PER_FORM,
        STARTER,      // Up to 50 forms
        PROFESSIONAL,  // Up to 500 forms
        ENTERPRISE     // Unlimited
    }

    public enum SubscriptionStatus {
        ACTIVE, PAST_DUE, CANCELLED, EXPIRED, TRIALING
    }
}

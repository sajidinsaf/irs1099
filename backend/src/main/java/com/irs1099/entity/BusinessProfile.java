package com.irs1099.entity;

import javax.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "business_profiles")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class BusinessProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false)
    private String businessName;

    private String doingBusinessAs;

    /** Encrypted EIN (AES-256) */
    @Column(nullable = false)
    private String einEncrypted;

    @Enumerated(EnumType.STRING)
    private BusinessType businessType;

    private String businessPhone;

    // Physical address (required by IRS)
    private String addressLine1;
    private String addressLine2;
    private String city;
    private String state;
    private String zipCode;
    private String country;

    // Mailing address (if different)
    private String mailingAddressLine1;
    private String mailingAddressLine2;
    private String mailingCity;
    private String mailingState;
    private String mailingZipCode;

    /** IRIS Transmitter Control Code (5-char alphanumeric starting with 'D') */
    private String tcc;

    /** IRS API Client ID for IRIS A2A */
    private String irisClientId;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public enum BusinessType {
        SOLE_PROPRIETORSHIP,
        PARTNERSHIP,
        CORPORATION,
        S_CORPORATION,
        LLC,
        TRUST,
        ESTATE,
        NON_PROFIT,
        GOVERNMENT
    }
}

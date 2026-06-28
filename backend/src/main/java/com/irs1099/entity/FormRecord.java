package com.irs1099.entity;

import javax.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "form_records")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class FormRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submission_id", nullable = false)
    private Submission submission;

    /** Record ID within the submission */
    private String recordId;

    @Column(nullable = false)
    private String formType;

    // Issuer Information
    private String issuerEinEncrypted;
    private String issuerName;
    private String issuerAddressLine1;
    private String issuerAddressLine2;
    private String issuerCity;
    private String issuerState;
    private String issuerZipCode;
    private String issuerPhone;

    // Recipient Information
    /** Encrypted TIN (SSN/EIN) */
    private String recipientTinEncrypted;

    @Enumerated(EnumType.STRING)
    private TinType recipientTinType;

    private String recipientFirstName;
    private String recipientMiddleName;
    private String recipientLastName;
    private String recipientBusinessName;
    private String recipientAddressLine1;
    private String recipientAddressLine2;
    private String recipientCity;
    private String recipientState;
    private String recipientZipCode;
    private String recipientCountry;
    private String recipientAccountNumber;

    /** Form-specific financial data stored as JSON */
    @Column(columnDefinition = "TEXT")
    private String formDataJson;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private RecordStatus status = RecordStatus.DRAFT;

    /** IRS error details for this record */
    @Column(columnDefinition = "TEXT")
    private String irsErrors;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public enum TinType {
        SSN, EIN, ITIN, ATIN
    }

    public enum RecordStatus {
        DRAFT, SUBMITTED, ACCEPTED, REJECTED, CORRECTED
    }
}

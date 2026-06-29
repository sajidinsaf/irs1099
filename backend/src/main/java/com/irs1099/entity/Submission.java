package com.irs1099.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "submissions")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Submission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String formType;

    @Column(nullable = false)
    private int taxYear;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private TransmissionType transmissionType = TransmissionType.ORIGINAL;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private SubmissionStatus status = SubmissionStatus.DRAFT;

    /** IRS Unique Transmission ID (UUID:IRIS:TCC::A) */
    private String utid;

    /** IRS-assigned receipt ID */
    private String receiptId;

    /** Submission ID within the transmission */
    private String submissionId;

    /** Reference to original submission (for corrections/replacements) */
    private String originalUniqueSubmissionId;

    /** Combined Federal/State filing */
    @Builder.Default
    private boolean cfsfFiling = false;

    @OneToMany(mappedBy = "submission", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<FormRecord> records = new ArrayList<>();

    private LocalDateTime submittedAt;
    private LocalDateTime acknowledgedAt;

    /** IRS error details (JSON) */
    @Column(columnDefinition = "TEXT")
    private String irsErrors;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public enum TransmissionType {
        ORIGINAL, CORRECTION, REPLACEMENT
    }

    public enum SubmissionStatus {
        DRAFT,
        VALIDATING,
        SUBMITTED,
        PROCESSING,
        ACCEPTED,
        ACCEPTED_WITH_ERRORS,
        PARTIALLY_ACCEPTED,
        REJECTED,
        CANCELLED
    }
}

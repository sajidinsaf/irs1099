package com.irs1099.dto.response;

import com.irs1099.entity.Submission;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@Builder
public class SubmissionResponse {
    private Long id;
    private String formType;
    private int taxYear;
    private String transmissionType;
    private String status;
    private String utid;
    private String receiptId;
    private boolean cfsfFiling;
    private int recordCount;
    private LocalDateTime submittedAt;
    private LocalDateTime createdAt;
    private String irsErrors;

    public static SubmissionResponse fromEntity(Submission s, int recordCount) {
        return SubmissionResponse.builder()
                .id(s.getId())
                .formType(s.getFormType())
                .taxYear(s.getTaxYear())
                .transmissionType(s.getTransmissionType().name())
                .status(s.getStatus().name())
                .utid(s.getUtid())
                .receiptId(s.getReceiptId())
                .cfsfFiling(s.isCfsfFiling())
                .recordCount(recordCount)
                .submittedAt(s.getSubmittedAt())
                .createdAt(s.getCreatedAt())
                .irsErrors(s.getIrsErrors())
                .build();
    }
}

package com.irs1099.dto.response;

import com.irs1099.entity.FormRecord;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@Builder
public class FormRecordResponse {
    private Long id;
    private String recordId;
    private String formType;

    // Issuer (masked EIN)
    private String issuerEinMasked;
    private String issuerName;
    private String issuerAddressLine1;
    private String issuerAddressLine2;
    private String issuerCity;
    private String issuerState;
    private String issuerZipCode;
    private String issuerPhone;

    // Recipient (masked TIN)
    private String recipientTinMasked;
    private String recipientTinType;
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

    private String formDataJson;
    private String status;
    private LocalDateTime createdAt;

    public static FormRecordResponse fromEntity(FormRecord r, String maskedIssuerEin, String maskedRecipientTin) {
        return FormRecordResponse.builder()
                .id(r.getId())
                .recordId(r.getRecordId())
                .formType(r.getFormType())
                .issuerEinMasked(maskedIssuerEin)
                .issuerName(r.getIssuerName())
                .issuerAddressLine1(r.getIssuerAddressLine1())
                .issuerAddressLine2(r.getIssuerAddressLine2())
                .issuerCity(r.getIssuerCity())
                .issuerState(r.getIssuerState())
                .issuerZipCode(r.getIssuerZipCode())
                .issuerPhone(r.getIssuerPhone())
                .recipientTinMasked(maskedRecipientTin)
                .recipientTinType(r.getRecipientTinType() != null ? r.getRecipientTinType().name() : null)
                .recipientFirstName(r.getRecipientFirstName())
                .recipientMiddleName(r.getRecipientMiddleName())
                .recipientLastName(r.getRecipientLastName())
                .recipientBusinessName(r.getRecipientBusinessName())
                .recipientAddressLine1(r.getRecipientAddressLine1())
                .recipientAddressLine2(r.getRecipientAddressLine2())
                .recipientCity(r.getRecipientCity())
                .recipientState(r.getRecipientState())
                .recipientZipCode(r.getRecipientZipCode())
                .recipientCountry(r.getRecipientCountry())
                .recipientAccountNumber(r.getRecipientAccountNumber())
                .formDataJson(r.getFormDataJson())
                .status(r.getStatus().name())
                .createdAt(r.getCreatedAt())
                .build();
    }
}

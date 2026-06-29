package com.irs1099.dto.request;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Data
public class FormRecordRequest {

    // Issuer info (payer)
    @NotBlank(message = "Issuer EIN is required")
    @Pattern(regexp = "^\\d{2}-\\d{7}$", message = "EIN must be in format XX-XXXXXXX")
    private String issuerEin;

    @NotBlank(message = "Issuer name is required")
    private String issuerName;

    @NotBlank(message = "Issuer address is required")
    private String issuerAddressLine1;

    private String issuerAddressLine2;

    @NotBlank(message = "Issuer city is required")
    private String issuerCity;

    @NotBlank(message = "Issuer state is required")
    private String issuerState;

    @NotBlank(message = "Issuer ZIP is required")
    private String issuerZipCode;

    private String issuerPhone;

    // Recipient info (payee)
    @NotBlank(message = "Recipient TIN is required")
    private String recipientTin;

    @NotBlank(message = "TIN type is required")
    private String recipientTinType; // SSN, EIN, ITIN, ATIN

    private String recipientFirstName;
    private String recipientMiddleName;
    private String recipientLastName;
    private String recipientBusinessName;

    @NotBlank(message = "Recipient address is required")
    private String recipientAddressLine1;

    private String recipientAddressLine2;

    @NotBlank(message = "Recipient city is required")
    private String recipientCity;

    @NotBlank(message = "Recipient state is required")
    private String recipientState;

    @NotBlank(message = "Recipient ZIP is required")
    private String recipientZipCode;

    private String recipientCountry;
    private String recipientAccountNumber;

    /** Form-specific financial data as JSON string */
    private String formDataJson;
}

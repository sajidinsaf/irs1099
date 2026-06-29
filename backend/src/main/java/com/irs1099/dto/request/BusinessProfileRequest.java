package com.irs1099.dto.request;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Data
public class BusinessProfileRequest {

    @NotBlank(message = "Business name is required")
    @Size(max = 255)
    private String businessName;

    @Size(max = 255)
    private String doingBusinessAs;

    @NotBlank(message = "EIN is required")
    @Pattern(regexp = "^\\d{2}-\\d{7}$", message = "EIN must be in format XX-XXXXXXX")
    private String ein;

    private String businessType;

    @Size(max = 20)
    private String businessPhone;

    @NotBlank(message = "Street address is required")
    private String addressLine1;

    private String addressLine2;

    @NotBlank(message = "City is required")
    private String city;

    @NotBlank(message = "State is required")
    @Size(min = 2, max = 2, message = "State must be a 2-letter code")
    private String state;

    @NotBlank(message = "ZIP code is required")
    @Pattern(regexp = "^\\d{5}(-\\d{4})?$", message = "ZIP code must be in format XXXXX or XXXXX-XXXX")
    private String zipCode;

    private String country;

    // Mailing address (if different)
    private String mailingAddressLine1;
    private String mailingAddressLine2;
    private String mailingCity;
    private String mailingState;
    private String mailingZipCode;

    // IRIS credentials (optional)
    private String tcc;
    private String irisClientId;
}

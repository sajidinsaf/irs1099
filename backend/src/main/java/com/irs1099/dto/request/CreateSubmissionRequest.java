package com.irs1099.dto.request;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

@Data
public class CreateSubmissionRequest {

    @NotBlank(message = "Form type is required")
    private String formType;

    @Min(value = 2020, message = "Tax year must be 2020 or later")
    @Max(value = 2025, message = "Tax year must be 2025 or earlier")
    private int taxYear;

    private boolean cfsfFiling;
}

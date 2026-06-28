package com.irs1099.dto.response;

import com.irs1099.entity.BusinessProfile;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class BusinessProfileResponse {

    private Long id;
    private String businessName;
    private String doingBusinessAs;
    private String einMasked; // Show only last 4: ***-***1234
    private String businessType;
    private String businessPhone;

    private String addressLine1;
    private String addressLine2;
    private String city;
    private String state;
    private String zipCode;
    private String country;

    private String mailingAddressLine1;
    private String mailingAddressLine2;
    private String mailingCity;
    private String mailingState;
    private String mailingZipCode;

    private String tcc;
    private boolean hasIrisClientId;

    public static BusinessProfileResponse fromEntity(BusinessProfile profile, String maskedEin) {
        return BusinessProfileResponse.builder()
                .id(profile.getId())
                .businessName(profile.getBusinessName())
                .doingBusinessAs(profile.getDoingBusinessAs())
                .einMasked(maskedEin)
                .businessType(profile.getBusinessType() != null ? profile.getBusinessType().name() : null)
                .businessPhone(profile.getBusinessPhone())
                .addressLine1(profile.getAddressLine1())
                .addressLine2(profile.getAddressLine2())
                .city(profile.getCity())
                .state(profile.getState())
                .zipCode(profile.getZipCode())
                .country(profile.getCountry())
                .mailingAddressLine1(profile.getMailingAddressLine1())
                .mailingAddressLine2(profile.getMailingAddressLine2())
                .mailingCity(profile.getMailingCity())
                .mailingState(profile.getMailingState())
                .mailingZipCode(profile.getMailingZipCode())
                .tcc(profile.getTcc())
                .hasIrisClientId(profile.getIrisClientId() != null && !profile.getIrisClientId().isEmpty())
                .build();
    }
}

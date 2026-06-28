package com.irs1099.service;

import com.irs1099.dto.request.BusinessProfileRequest;
import com.irs1099.dto.response.BusinessProfileResponse;
import com.irs1099.entity.BusinessProfile;
import com.irs1099.entity.User;
import com.irs1099.exception.BadRequestException;
import com.irs1099.exception.ResourceNotFoundException;
import com.irs1099.repository.BusinessProfileRepository;
import com.irs1099.repository.UserRepository;
import com.irs1099.util.EncryptionUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BusinessProfileService {

    private final BusinessProfileRepository profileRepository;
    private final UserRepository userRepository;
    private final EncryptionUtil encryptionUtil;

    public BusinessProfileResponse getProfile(Long userId) {
        BusinessProfile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Business profile", "userId", userId));
        return toResponse(profile);
    }

    public boolean hasProfile(Long userId) {
        return profileRepository.findByUserId(userId).isPresent();
    }

    @Transactional
    public BusinessProfileResponse createProfile(Long userId, BusinessProfileRequest request) {
        if (profileRepository.findByUserId(userId).isPresent()) {
            throw new BadRequestException("Business profile already exists. Use update instead.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        BusinessProfile profile = new BusinessProfile();
        profile.setUser(user);
        mapRequestToEntity(request, profile);

        profileRepository.save(profile);
        return toResponse(profile);
    }

    @Transactional
    public BusinessProfileResponse updateProfile(Long userId, BusinessProfileRequest request) {
        BusinessProfile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Business profile", "userId", userId));

        mapRequestToEntity(request, profile);
        profileRepository.save(profile);
        return toResponse(profile);
    }

    private void mapRequestToEntity(BusinessProfileRequest request, BusinessProfile profile) {
        profile.setBusinessName(request.getBusinessName());
        profile.setDoingBusinessAs(request.getDoingBusinessAs());
        profile.setEinEncrypted(encryptionUtil.encrypt(request.getEin()));

        if (request.getBusinessType() != null && !request.getBusinessType().isEmpty()) {
            profile.setBusinessType(BusinessProfile.BusinessType.valueOf(request.getBusinessType()));
        }

        profile.setBusinessPhone(request.getBusinessPhone());
        profile.setAddressLine1(request.getAddressLine1());
        profile.setAddressLine2(request.getAddressLine2());
        profile.setCity(request.getCity());
        profile.setState(request.getState().toUpperCase());
        profile.setZipCode(request.getZipCode());
        profile.setCountry(request.getCountry() != null ? request.getCountry() : "US");

        profile.setMailingAddressLine1(request.getMailingAddressLine1());
        profile.setMailingAddressLine2(request.getMailingAddressLine2());
        profile.setMailingCity(request.getMailingCity());
        profile.setMailingState(request.getMailingState());
        profile.setMailingZipCode(request.getMailingZipCode());

        profile.setTcc(request.getTcc());
        profile.setIrisClientId(request.getIrisClientId());
    }

    private BusinessProfileResponse toResponse(BusinessProfile profile) {
        String decryptedEin = encryptionUtil.decrypt(profile.getEinEncrypted());
        String maskedEin = "**-***" + decryptedEin.substring(decryptedEin.length() - 4);
        return BusinessProfileResponse.fromEntity(profile, maskedEin);
    }
}

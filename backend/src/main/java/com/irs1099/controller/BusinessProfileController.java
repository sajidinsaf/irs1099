package com.irs1099.controller;

import com.irs1099.dto.request.BusinessProfileRequest;
import com.irs1099.dto.response.BusinessProfileResponse;
import com.irs1099.security.UserPrincipal;
import com.irs1099.service.BusinessProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/profile")
@RequiredArgsConstructor
public class BusinessProfileController {

    private final BusinessProfileService profileService;

    @GetMapping
    public ResponseEntity<BusinessProfileResponse> getProfile(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(profileService.getProfile(principal.getId()));
    }

    @GetMapping("/exists")
    public ResponseEntity<Map<String, Boolean>> hasProfile(
            @AuthenticationPrincipal UserPrincipal principal) {
        boolean exists = profileService.hasProfile(principal.getId());
        return ResponseEntity.ok(Collections.singletonMap("exists", exists));
    }

    @PostMapping
    public ResponseEntity<BusinessProfileResponse> createProfile(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody BusinessProfileRequest request) {
        BusinessProfileResponse response = profileService.createProfile(principal.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping
    public ResponseEntity<BusinessProfileResponse> updateProfile(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody BusinessProfileRequest request) {
        return ResponseEntity.ok(profileService.updateProfile(principal.getId(), request));
    }
}

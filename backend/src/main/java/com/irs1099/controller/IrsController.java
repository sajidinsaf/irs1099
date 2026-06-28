package com.irs1099.controller;

import com.irs1099.dto.response.ApiResponse;
import com.irs1099.entity.BusinessProfile;
import com.irs1099.entity.FormRecord;
import com.irs1099.entity.Submission;
import com.irs1099.exception.BadRequestException;
import com.irs1099.exception.ResourceNotFoundException;
import com.irs1099.repository.BusinessProfileRepository;
import com.irs1099.repository.FormRecordRepository;
import com.irs1099.repository.SubmissionRepository;
import com.irs1099.security.UserPrincipal;
import com.irs1099.service.iris.IrisFilingService;
import com.irs1099.service.iris.IrsXmlGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/irs")
@RequiredArgsConstructor
public class IrsController {

    private final IrsXmlGenerator xmlGenerator;
    private final IrisFilingService filingService;
    private final SubmissionRepository submissionRepository;
    private final FormRecordRepository formRecordRepository;
    private final BusinessProfileRepository businessProfileRepository;

    /**
     * Preview the generated XML for a submission (without submitting to IRS).
     */
    @GetMapping(value = "/preview/{submissionId}", produces = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<String> previewXml(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long submissionId) {

        Submission submission = submissionRepository.findByIdAndUserId(submissionId, principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Submission", "id", submissionId));

        List<FormRecord> records = formRecordRepository.findBySubmissionId(submissionId);
        if (records.isEmpty()) {
            throw new BadRequestException("Submission has no records");
        }

        BusinessProfile profile = businessProfileRepository.findByUserId(principal.getId())
                .orElseThrow(() -> new BadRequestException("Business profile is required to generate XML"));

        IrsXmlGenerator.XmlGenerationResult result = xmlGenerator.generateTransmission(submission, records, profile);

        if (!result.isSuccess()) {
            throw new BadRequestException(result.getError());
        }

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_XML)
                .body(result.getXml());
    }

    /**
     * Validate a submission and return readiness status.
     */
    @GetMapping("/validate/{submissionId}")
    public ResponseEntity<Map<String, Object>> validateSubmission(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long submissionId) {

        Submission submission = submissionRepository.findByIdAndUserId(submissionId, principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Submission", "id", submissionId));

        List<FormRecord> records = formRecordRepository.findBySubmissionId(submissionId);
        BusinessProfile profile = businessProfileRepository.findByUserId(principal.getId()).orElse(null);

        Map<String, Object> validation = new HashMap<>();
        List<String> errors = new java.util.ArrayList<>();
        List<String> warnings = new java.util.ArrayList<>();

        // Check business profile
        if (profile == null) {
            errors.add("Business profile is required");
        } else {
            if (profile.getTcc() == null || profile.getTcc().isEmpty()) {
                errors.add("Transmitter Control Code (TCC) is required in your business profile");
            }
            if (profile.getEinEncrypted() == null || profile.getEinEncrypted().isEmpty()) {
                errors.add("Business EIN is required");
            }
        }

        // Check records
        if (records.isEmpty()) {
            errors.add("At least one record is required");
        }

        // Check each record has required data
        for (int i = 0; i < records.size(); i++) {
            FormRecord r = records.get(i);
            if (r.getRecipientTinEncrypted() == null || r.getRecipientTinEncrypted().isEmpty()) {
                errors.add("Record " + (i + 1) + ": Recipient TIN is missing");
            }
            if (r.getIssuerEinEncrypted() == null || r.getIssuerEinEncrypted().isEmpty()) {
                errors.add("Record " + (i + 1) + ": Issuer EIN is missing");
            }
            if (r.getFormDataJson() == null || r.getFormDataJson().isEmpty()) {
                errors.add("Record " + (i + 1) + ": Form data is missing");
            }
        }

        // Check submission status
        if (submission.getStatus() != Submission.SubmissionStatus.DRAFT) {
            warnings.add("This submission is not in DRAFT status (" + submission.getStatus() + ")");
        }

        validation.put("ready", errors.isEmpty());
        validation.put("errors", errors);
        validation.put("warnings", warnings);
        validation.put("recordCount", records.size());
        validation.put("formType", submission.getFormType());
        validation.put("taxYear", submission.getTaxYear());

        return ResponseEntity.ok(validation);
    }

    /**
     * Submit a draft submission to IRS (or mock service).
     */
    @PostMapping("/submit/{submissionId}")
    public ResponseEntity<Map<String, Object>> submitToIrs(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long submissionId) {
        return ResponseEntity.ok(filingService.submitToIrs(principal.getId(), submissionId));
    }

    /**
     * Check IRS status for a submitted transmission.
     */
    @GetMapping("/status/{submissionId}")
    public ResponseEntity<Map<String, Object>> checkStatus(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long submissionId) {
        return ResponseEntity.ok(filingService.checkStatus(principal.getId(), submissionId));
    }
}

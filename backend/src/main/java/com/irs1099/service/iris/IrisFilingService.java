package com.irs1099.service.iris;

import com.irs1099.entity.BusinessProfile;
import com.irs1099.entity.FormRecord;
import com.irs1099.entity.Submission;
import com.irs1099.exception.BadRequestException;
import com.irs1099.exception.ResourceNotFoundException;
import com.irs1099.repository.BusinessProfileRepository;
import com.irs1099.repository.FormRecordRepository;
import com.irs1099.repository.SubmissionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Orchestrates the full IRS filing flow:
 * 1. Validate submission
 * 2. Generate XML
 * 3. Get OAuth token
 * 4. Submit to IRS (or mock)
 * 5. Store receipt ID
 * 6. Poll for status
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class IrisFilingService {

    private final IrsXmlGenerator xmlGenerator;
    private final IrisSubmissionClient submissionClient;
    private final SubmissionRepository submissionRepository;
    private final FormRecordRepository formRecordRepository;
    private final BusinessProfileRepository businessProfileRepository;

    /**
     * Submit a draft submission to IRS (or mock).
     */
    @Transactional
    public Map<String, Object> submitToIrs(Long userId, Long submissionId) {
        Submission submission = submissionRepository.findByIdAndUserId(submissionId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Submission", "id", submissionId));

        if (submission.getStatus() != Submission.SubmissionStatus.DRAFT) {
            throw new BadRequestException("Only DRAFT submissions can be submitted. Current status: " + submission.getStatus());
        }

        List<FormRecord> records = formRecordRepository.findBySubmissionId(submissionId);
        if (records.isEmpty()) {
            throw new BadRequestException("Submission has no records");
        }

        BusinessProfile profile = businessProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new BadRequestException("Business profile is required"));

        if (profile.getTcc() == null || profile.getTcc().isEmpty()) {
            throw new BadRequestException("Transmitter Control Code (TCC) is required in your business profile");
        }

        // 1. Generate XML
        submission.setStatus(Submission.SubmissionStatus.VALIDATING);
        submissionRepository.save(submission);

        IrsXmlGenerator.XmlGenerationResult xmlResult = xmlGenerator.generateTransmission(submission, records, profile);
        if (!xmlResult.isSuccess()) {
            submission.setStatus(Submission.SubmissionStatus.DRAFT);
            submissionRepository.save(submission);
            throw new BadRequestException("XML generation failed: " + xmlResult.getError());
        }

        // 2. Get access token (for mock, uses dummy JWTs)
        IrisSubmissionClient.TokenResponse tokenResponse = submissionClient.getAccessToken(
                profile.getIrisClientId() != null ? profile.getIrisClientId() : "mock-client-id",
                "mock-client-jwt",
                "mock-user-jwt"
        );

        if (!tokenResponse.success) {
            submission.setStatus(Submission.SubmissionStatus.DRAFT);
            submissionRepository.save(submission);
            throw new BadRequestException("Authentication failed: " + tokenResponse.error);
        }

        // 3. Submit
        IrisSubmissionClient.SubmitResponse submitResponse =
                submissionClient.submitTransmission(tokenResponse.accessToken, xmlResult.getXml());

        if (!submitResponse.success) {
            submission.setStatus(Submission.SubmissionStatus.REJECTED);
            submission.setIrsErrors(submitResponse.error);
            submissionRepository.save(submission);

            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("error", submitResponse.error);
            return result;
        }

        // 4. Update submission with IRS response
        submission.setStatus(Submission.SubmissionStatus.SUBMITTED);
        submission.setUtid(xmlResult.getUtid());
        submission.setSubmissionId(xmlResult.getSubmissionId());
        submission.setReceiptId(submitResponse.receiptId);
        submission.setSubmittedAt(LocalDateTime.now());
        submissionRepository.save(submission);

        // Update record statuses
        for (FormRecord record : records) {
            record.setStatus(FormRecord.RecordStatus.SUBMITTED);
        }
        formRecordRepository.saveAll(records);

        log.info("Submission {} submitted to IRS. ReceiptId={}, UTID={}",
                submissionId, submitResponse.receiptId, xmlResult.getUtid());

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("receiptId", submitResponse.receiptId);
        result.put("utid", xmlResult.getUtid());
        result.put("timestamp", submitResponse.timestamp);
        return result;
    }

    /**
     * Check submission status with IRS (or mock).
     */
    @Transactional
    public Map<String, Object> checkStatus(Long userId, Long submissionId) {
        Submission submission = submissionRepository.findByIdAndUserId(submissionId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Submission", "id", submissionId));

        if (submission.getReceiptId() == null) {
            throw new BadRequestException("Submission has not been submitted to IRS yet");
        }

        // Get token
        IrisSubmissionClient.TokenResponse tokenResponse = submissionClient.getAccessToken(
                "mock-client-id", "mock-client-jwt", "mock-user-jwt");

        if (!tokenResponse.success) {
            throw new BadRequestException("Authentication failed: " + tokenResponse.error);
        }

        // Check status using Receipt ID
        IrisSubmissionClient.StatusResponse statusResponse =
                submissionClient.getStatus(tokenResponse.accessToken, "RECEIPTID", submission.getReceiptId());

        if (!statusResponse.success) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("error", statusResponse.error);
            return result;
        }

        // Update submission status based on IRS response
        Submission.SubmissionStatus newStatus = mapIrsStatus(statusResponse.status);
        if (newStatus != submission.getStatus()) {
            submission.setStatus(newStatus);
            if (newStatus == Submission.SubmissionStatus.ACCEPTED ||
                    newStatus == Submission.SubmissionStatus.REJECTED ||
                    newStatus == Submission.SubmissionStatus.ACCEPTED_WITH_ERRORS) {
                submission.setAcknowledgedAt(LocalDateTime.now());
            }
            if (statusResponse.errorCode != null) {
                submission.setIrsErrors(statusResponse.errorCode + ": " + statusResponse.errorMessage);
            }
            submissionRepository.save(submission);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("status", statusResponse.status);
        result.put("receiptId", submission.getReceiptId());
        result.put("acceptedCount", statusResponse.acceptedCount);
        result.put("rejectedCount", statusResponse.rejectedCount);
        if (statusResponse.errorCode != null) {
            result.put("errorCode", statusResponse.errorCode);
            result.put("errorMessage", statusResponse.errorMessage);
        }
        return result;
    }

    private Submission.SubmissionStatus mapIrsStatus(String irsStatus) {
        if (irsStatus == null) return Submission.SubmissionStatus.PROCESSING;
        switch (irsStatus) {
            case "Accepted": return Submission.SubmissionStatus.ACCEPTED;
            case "Rejected": return Submission.SubmissionStatus.REJECTED;
            case "Accepted With Errors": return Submission.SubmissionStatus.ACCEPTED_WITH_ERRORS;
            case "Partially Accepted": return Submission.SubmissionStatus.PARTIALLY_ACCEPTED;
            case "Processing": return Submission.SubmissionStatus.PROCESSING;
            default: return Submission.SubmissionStatus.PROCESSING;
        }
    }
}

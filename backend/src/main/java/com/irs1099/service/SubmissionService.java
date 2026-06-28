package com.irs1099.service;

import com.irs1099.dto.request.CreateSubmissionRequest;
import com.irs1099.dto.request.FormRecordRequest;
import com.irs1099.dto.response.FormRecordResponse;
import com.irs1099.dto.response.SubmissionResponse;
import com.irs1099.entity.FormRecord;
import com.irs1099.entity.Submission;
import com.irs1099.entity.User;
import com.irs1099.exception.BadRequestException;
import com.irs1099.exception.ResourceNotFoundException;
import com.irs1099.repository.FormRecordRepository;
import com.irs1099.repository.SubmissionRepository;
import com.irs1099.repository.UserRepository;
import com.irs1099.util.EncryptionUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SubmissionService {

    private final SubmissionRepository submissionRepository;
    private final FormRecordRepository formRecordRepository;
    private final UserRepository userRepository;
    private final EncryptionUtil encryptionUtil;

    @Transactional
    public SubmissionResponse createSubmission(Long userId, CreateSubmissionRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Submission submission = new Submission();
        submission.setUser(user);
        submission.setFormType(request.getFormType());
        submission.setTaxYear(request.getTaxYear());
        submission.setCfsfFiling(request.isCfsfFiling());
        submission.setStatus(Submission.SubmissionStatus.DRAFT);
        submission.setTransmissionType(Submission.TransmissionType.ORIGINAL);

        submissionRepository.save(submission);
        return SubmissionResponse.fromEntity(submission, 0);
    }

    public Page<SubmissionResponse> getUserSubmissions(Long userId, Pageable pageable) {
        return submissionRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(s -> {
                    int count = (int) formRecordRepository.countBySubmissionId(s.getId());
                    return SubmissionResponse.fromEntity(s, count);
                });
    }

    public SubmissionResponse getSubmission(Long userId, Long submissionId) {
        Submission submission = submissionRepository.findByIdAndUserId(submissionId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Submission", "id", submissionId));
        int count = (int) formRecordRepository.countBySubmissionId(submissionId);
        return SubmissionResponse.fromEntity(submission, count);
    }

    @Transactional
    public void deleteSubmission(Long userId, Long submissionId) {
        Submission submission = submissionRepository.findByIdAndUserId(submissionId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Submission", "id", submissionId));
        if (submission.getStatus() != Submission.SubmissionStatus.DRAFT) {
            throw new BadRequestException("Only draft submissions can be deleted");
        }
        submissionRepository.delete(submission);
    }

    // --- Form Records ---

    @Transactional
    public FormRecordResponse addRecord(Long userId, Long submissionId, FormRecordRequest request) {
        Submission submission = submissionRepository.findByIdAndUserId(submissionId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Submission", "id", submissionId));

        if (submission.getStatus() != Submission.SubmissionStatus.DRAFT) {
            throw new BadRequestException("Cannot add records to a non-draft submission");
        }

        FormRecord record = new FormRecord();
        record.setSubmission(submission);
        record.setRecordId(UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        record.setFormType(submission.getFormType());
        record.setStatus(FormRecord.RecordStatus.DRAFT);

        mapRecordRequest(request, record);
        formRecordRepository.save(record);

        return toRecordResponse(record);
    }

    @Transactional
    public FormRecordResponse updateRecord(Long userId, Long submissionId, Long recordId, FormRecordRequest request) {
        Submission submission = submissionRepository.findByIdAndUserId(submissionId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Submission", "id", submissionId));

        if (submission.getStatus() != Submission.SubmissionStatus.DRAFT) {
            throw new BadRequestException("Cannot edit records in a non-draft submission");
        }

        FormRecord record = formRecordRepository.findById(recordId)
                .orElseThrow(() -> new ResourceNotFoundException("Record", "id", recordId));

        if (!record.getSubmission().getId().equals(submissionId)) {
            throw new BadRequestException("Record does not belong to this submission");
        }

        mapRecordRequest(request, record);
        formRecordRepository.save(record);

        return toRecordResponse(record);
    }

    public List<FormRecordResponse> getRecords(Long userId, Long submissionId) {
        submissionRepository.findByIdAndUserId(submissionId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Submission", "id", submissionId));

        return formRecordRepository.findBySubmissionId(submissionId).stream()
                .map(this::toRecordResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteRecord(Long userId, Long submissionId, Long recordId) {
        Submission submission = submissionRepository.findByIdAndUserId(submissionId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Submission", "id", submissionId));

        if (submission.getStatus() != Submission.SubmissionStatus.DRAFT) {
            throw new BadRequestException("Cannot delete records from a non-draft submission");
        }

        FormRecord record = formRecordRepository.findById(recordId)
                .orElseThrow(() -> new ResourceNotFoundException("Record", "id", recordId));

        if (!record.getSubmission().getId().equals(submissionId)) {
            throw new BadRequestException("Record does not belong to this submission");
        }

        formRecordRepository.delete(record);
    }

    private void mapRecordRequest(FormRecordRequest request, FormRecord record) {
        record.setIssuerEinEncrypted(encryptionUtil.encrypt(request.getIssuerEin()));
        record.setIssuerName(request.getIssuerName());
        record.setIssuerAddressLine1(request.getIssuerAddressLine1());
        record.setIssuerAddressLine2(request.getIssuerAddressLine2());
        record.setIssuerCity(request.getIssuerCity());
        record.setIssuerState(request.getIssuerState().toUpperCase());
        record.setIssuerZipCode(request.getIssuerZipCode());
        record.setIssuerPhone(request.getIssuerPhone());

        record.setRecipientTinEncrypted(encryptionUtil.encrypt(request.getRecipientTin()));
        record.setRecipientTinType(FormRecord.TinType.valueOf(request.getRecipientTinType()));
        record.setRecipientFirstName(request.getRecipientFirstName());
        record.setRecipientMiddleName(request.getRecipientMiddleName());
        record.setRecipientLastName(request.getRecipientLastName());
        record.setRecipientBusinessName(request.getRecipientBusinessName());
        record.setRecipientAddressLine1(request.getRecipientAddressLine1());
        record.setRecipientAddressLine2(request.getRecipientAddressLine2());
        record.setRecipientCity(request.getRecipientCity());
        record.setRecipientState(request.getRecipientState().toUpperCase());
        record.setRecipientZipCode(request.getRecipientZipCode());
        record.setRecipientCountry(request.getRecipientCountry());
        record.setRecipientAccountNumber(request.getRecipientAccountNumber());
        record.setFormDataJson(request.getFormDataJson());
    }

    private FormRecordResponse toRecordResponse(FormRecord record) {
        String maskedEin = maskTin(encryptionUtil.decrypt(record.getIssuerEinEncrypted()));
        String maskedTin = maskTin(encryptionUtil.decrypt(record.getRecipientTinEncrypted()));
        return FormRecordResponse.fromEntity(record, maskedEin, maskedTin);
    }

    private String maskTin(String tin) {
        if (tin == null || tin.length() < 4) return "****";
        return "***-**-" + tin.substring(tin.length() - 4);
    }
}

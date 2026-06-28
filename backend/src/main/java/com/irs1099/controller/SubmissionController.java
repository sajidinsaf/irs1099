package com.irs1099.controller;

import com.irs1099.dto.request.CreateSubmissionRequest;
import com.irs1099.dto.request.FormRecordRequest;
import com.irs1099.dto.response.ApiResponse;
import com.irs1099.dto.response.FormRecordResponse;
import com.irs1099.dto.response.SubmissionResponse;
import com.irs1099.security.UserPrincipal;
import com.irs1099.service.SubmissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/submissions")
@RequiredArgsConstructor
public class SubmissionController {

    private final SubmissionService submissionService;

    @PostMapping
    public ResponseEntity<SubmissionResponse> createSubmission(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CreateSubmissionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(submissionService.createSubmission(principal.getId(), request));
    }

    @GetMapping
    public ResponseEntity<Page<SubmissionResponse>> getSubmissions(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(
                submissionService.getUserSubmissions(principal.getId(), PageRequest.of(page, size)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SubmissionResponse> getSubmission(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id) {
        return ResponseEntity.ok(submissionService.getSubmission(principal.getId(), id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteSubmission(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id) {
        submissionService.deleteSubmission(principal.getId(), id);
        return ResponseEntity.ok(ApiResponse.success("Submission deleted"));
    }

    // --- Records ---

    @PostMapping("/{submissionId}/records")
    public ResponseEntity<FormRecordResponse> addRecord(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long submissionId,
            @Valid @RequestBody FormRecordRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(submissionService.addRecord(principal.getId(), submissionId, request));
    }

    @GetMapping("/{submissionId}/records")
    public ResponseEntity<List<FormRecordResponse>> getRecords(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long submissionId) {
        return ResponseEntity.ok(submissionService.getRecords(principal.getId(), submissionId));
    }

    @PutMapping("/{submissionId}/records/{recordId}")
    public ResponseEntity<FormRecordResponse> updateRecord(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long submissionId,
            @PathVariable Long recordId,
            @Valid @RequestBody FormRecordRequest request) {
        return ResponseEntity.ok(
                submissionService.updateRecord(principal.getId(), submissionId, recordId, request));
    }

    @DeleteMapping("/{submissionId}/records/{recordId}")
    public ResponseEntity<ApiResponse> deleteRecord(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long submissionId,
            @PathVariable Long recordId) {
        submissionService.deleteRecord(principal.getId(), submissionId, recordId);
        return ResponseEntity.ok(ApiResponse.success("Record deleted"));
    }
}

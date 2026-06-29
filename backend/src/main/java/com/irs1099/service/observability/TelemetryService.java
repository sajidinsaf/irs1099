package com.irs1099.service.observability;

import com.irs1099.entity.AuditLog;
import com.irs1099.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Lightweight telemetry service for Verops observability.
 * Records key business events as spans via Verops REST API.
 * Runs async to avoid impacting request latency.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TelemetryService {

    private final AuditLogRepository auditLogRepository;

    @Value("${app.verops.enabled:false}")
    private boolean veropsEnabled;

    @Value("${app.verops.base-url:https://app.verops.io}")
    private String veropsBaseUrl;

    @Value("${app.verops.service-name:irs1099-production}")
    private String serviceName;

    /**
     * Record a successful IRS submission.
     */
    @Async
    public void recordSubmission(Long userId, Long submissionId, String formType,
                                  int recordCount, String receiptId, long durationMs) {
        Map<String, Object> attrs = new HashMap<>();
        attrs.put("irs.form_type", formType);
        attrs.put("irs.record_count", recordCount);
        attrs.put("irs.receipt_id", receiptId);
        attrs.put("irs.submission_id", submissionId);
        recordEvent("irs.submission", "SUCCESS", durationMs, attrs);
        logAudit(userId, "IRS_SUBMISSION", "Submission", submissionId,
                String.format("Form=%s Records=%d Receipt=%s Duration=%dms", formType, recordCount, receiptId, durationMs));
    }

    /**
     * Record a failed IRS submission.
     */
    @Async
    public void recordSubmissionFailure(Long userId, Long submissionId, String formType,
                                         String error, long durationMs) {
        Map<String, Object> attrs = new HashMap<>();
        attrs.put("irs.form_type", formType);
        attrs.put("irs.error", error);
        attrs.put("irs.submission_id", submissionId);
        recordEvent("irs.submission", "ERROR", durationMs, attrs);
        logAudit(userId, "IRS_SUBMISSION_FAILED", "Submission", submissionId,
                String.format("Form=%s Error=%s Duration=%dms", formType, error, durationMs));
    }

    /**
     * Record a payment event.
     */
    @Async
    public void recordPayment(Long userId, String type, String amount, String status, long durationMs) {
        Map<String, Object> attrs = new HashMap<>();
        attrs.put("payment.type", type);
        attrs.put("payment.amount", amount);
        attrs.put("payment.status", status);
        recordEvent("payment.checkout", "SUCCESS".equals(status) ? "SUCCESS" : "ERROR", durationMs, attrs);
        logAudit(userId, "PAYMENT_" + status.toUpperCase(), "Payment", null,
                String.format("Type=%s Amount=%s Status=%s", type, amount, status));
    }

    /**
     * Record an authentication event.
     */
    @Async
    public void recordAuth(String action, String email, boolean success, long durationMs) {
        Map<String, Object> attrs = new HashMap<>();
        attrs.put("auth.action", action);
        attrs.put("auth.success", success);
        // Never log email in production traces
        recordEvent("auth." + action, success ? "SUCCESS" : "ERROR", durationMs, attrs);
    }

    /**
     * Record an IRS status check.
     */
    @Async
    public void recordStatusCheck(Long submissionId, String irsStatus, long durationMs) {
        Map<String, Object> attrs = new HashMap<>();
        attrs.put("irs.status", irsStatus);
        attrs.put("irs.submission_id", submissionId);
        recordEvent("irs.status_check", "SUCCESS", durationMs, attrs);
    }

    /**
     * Record an XML generation event.
     */
    @Async
    public void recordXmlGeneration(Long submissionId, int recordCount, long xmlSizeBytes, long durationMs) {
        Map<String, Object> attrs = new HashMap<>();
        attrs.put("irs.submission_id", submissionId);
        attrs.put("irs.record_count", recordCount);
        attrs.put("irs.xml_size_bytes", xmlSizeBytes);
        recordEvent("irs.xml_generation", "SUCCESS", durationMs, attrs);
    }

    // === Internal ===

    private void recordEvent(String operationName, String status, long durationMs, Map<String, Object> attributes) {
        if (!veropsEnabled) {
            log.debug("[Telemetry] {} status={} duration={}ms attrs={}", operationName, status, durationMs, attributes);
            return;
        }

        try {
            Map<String, Object> span = new HashMap<>();
            span.put("service_name", serviceName);
            span.put("operation_name", operationName);
            span.put("status", status);
            span.put("duration_ms", durationMs);
            span.put("attributes", attributes);
            span.put("timestamp", System.currentTimeMillis());

            // Send to Verops OTLP endpoint
            WebClient.create(veropsBaseUrl)
                    .post()
                    .uri("/api/v1.0/traces/spans")
                    .bodyValue(span)
                    .retrieve()
                    .bodyToMono(String.class)
                    .subscribe(
                            response -> log.trace("[Telemetry] Span sent: {}", operationName),
                            error -> log.warn("[Telemetry] Failed to send span: {}", error.getMessage())
                    );
        } catch (Exception e) {
            log.warn("[Telemetry] Error recording event: {}", e.getMessage());
        }
    }

    private void logAudit(Long userId, String action, String entityType, Long entityId, String details) {
        try {
            auditLogRepository.save(AuditLog.builder()
                    .userId(userId)
                    .action(action)
                    .entityType(entityType)
                    .entityId(entityId)
                    .details(details)
                    .timestamp(LocalDateTime.now())
                    .build());
        } catch (Exception e) {
            log.warn("[Telemetry] Failed to write audit log: {}", e.getMessage());
        }
    }
}

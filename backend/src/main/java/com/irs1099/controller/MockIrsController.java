package com.irs1099.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Mock IRS IRIS A2A endpoints for testing.
 * Only active when app.iris.use-mock=true.
 * Mimics the real IRS API behavior:
 * - OAuth2 token endpoint
 * - Submission intake
 * - Status/acknowledgment retrieval
 */
@RestController
@RequestMapping("/mock-irs")
@Slf4j
public class MockIrsController {

    // Store submitted transmissions for status lookup
    private final Map<String, MockTransmission> transmissions = new ConcurrentHashMap<>();

    /**
     * Mock OAuth2 token endpoint.
     * Real IRS: POST https://api.www4.irs.gov/auth/oauth/v2/token
     */
    @PostMapping(value = "/auth/oauth/v2/token",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<Map<String, Object>> getToken(
            @RequestParam(required = false) String grant_type,
            @RequestParam(required = false) String assertion,
            @RequestParam(required = false) String client_assertion_type,
            @RequestParam(required = false) String client_assertion) {

        log.info("[Mock IRS] Token request received. grant_type={}", grant_type);

        Map<String, Object> response = new HashMap<>();
        response.put("access_token", "mock-access-" + UUID.randomUUID().toString().substring(0, 16));
        response.put("token_type", "Bearer");
        response.put("refresh_token", "mock-refresh-" + UUID.randomUUID().toString().substring(0, 16));
        response.put("expires_in", 900); // 15 minutes

        return ResponseEntity.ok(response);
    }

    /**
     * Mock submission intake endpoint.
     * Real IRS: POST https://api.www4.irs.gov/IRIntakeAcceptanceA2A/1.0/irisa2a/v1/intake-acceptance
     */
    @PostMapping(value = "/irisa2a/v1/intake-acceptance",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<String> submitTransmission(
            @RequestParam("file") org.springframework.web.multipart.MultipartFile file,
            @RequestHeader("Authorization") String authHeader) {

        log.info("[Mock IRS] Submission received. Size={} bytes", file.getSize());

        // Validate auth header
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body(errorXml("AUTH_ERROR", "Invalid or missing authorization token"));
        }

        try {
            String xmlContent = new String(file.getBytes(), "UTF-8");

            // Basic validation
            if (!xmlContent.contains("IRTransmission")) {
                return ResponseEntity.badRequest().body(errorXml("FORMAT_ERROR", "Invalid XML: missing IRTransmission root element"));
            }

            if (!xmlContent.contains("UniqueTransmissionId")) {
                return ResponseEntity.badRequest().body(errorXml("UTID_ERROR", "Missing UniqueTransmissionId"));
            }

            // Extract UTID from XML
            String utid = extractElement(xmlContent, "UniqueTransmissionId");

            // Check for duplicate UTID
            if (transmissions.containsKey(utid)) {
                return ResponseEntity.badRequest().body(errorXml("DUPLICATE_UTID", "UTID has already been submitted"));
            }

            // Generate receipt
            String receiptId = "RCPT-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase();
            String timestamp = Instant.now().toString();

            // Store for status lookup - simulate processing delay
            MockTransmission mockTx = new MockTransmission();
            mockTx.utid = utid;
            mockTx.receiptId = receiptId;
            mockTx.timestamp = timestamp;
            mockTx.status = "Processing";
            mockTx.xmlContent = xmlContent;
            mockTx.submittedAt = System.currentTimeMillis();
            transmissions.put(receiptId, mockTx);
            transmissions.put(utid, mockTx);

            // Success response
            String responseXml = String.format(
                    "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                    "<IntakeAcceptanceResponse>\n" +
                    "  <ReceiptId>%s</ReceiptId>\n" +
                    "  <UniqueTransmissionId>%s</UniqueTransmissionId>\n" +
                    "  <Timestamp>%s</Timestamp>\n" +
                    "</IntakeAcceptanceResponse>",
                    receiptId, utid, timestamp);

            log.info("[Mock IRS] Submission accepted. ReceiptId={}, UTID={}", receiptId, utid);
            return ResponseEntity.ok(responseXml);

        } catch (Exception e) {
            log.error("[Mock IRS] Submission processing error", e);
            return ResponseEntity.status(500).body(errorXml("INTERNAL_ERROR", "Internal server error"));
        }
    }

    /**
     * Mock status/acknowledgment endpoint.
     * Real IRS: POST https://api.www4.irs.gov/IRIntakeAcceptanceA2A/1.0/iris/transstatusorack
     */
    @PostMapping(value = "/iris/transstatusorack",
            consumes = MediaType.APPLICATION_XML_VALUE,
            produces = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<String> getStatusOrAck(
            @RequestBody String requestXml,
            @RequestHeader("Authorization") String authHeader) {

        log.info("[Mock IRS] Status/Ack request received");

        String searchValue = extractElement(requestXml, "SearchParameterValueTxt");
        if (searchValue == null || searchValue.isEmpty()) {
            return ResponseEntity.badRequest().body(errorXml("INVALID_REQUEST", "Missing search parameter"));
        }

        MockTransmission tx = transmissions.get(searchValue);
        if (tx == null) {
            return ResponseEntity.status(404).body(errorXml("NOT_FOUND", "Transmission not found"));
        }

        // Simulate processing: accepted after 10 seconds
        long elapsed = System.currentTimeMillis() - tx.submittedAt;
        String status;
        String acknowledgment = "";

        if (elapsed < 10000) {
            status = "Processing";
        } else {
            // 90% accepted, 10% rejected (based on hash for determinism)
            boolean accepted = Math.abs(tx.receiptId.hashCode() % 10) != 0;
            if (accepted) {
                status = "Accepted";
                acknowledgment = "<AcknowledgementDetail>\n" +
                        "      <TransmissionStatusCd>Accepted</TransmissionStatusCd>\n" +
                        "      <AcceptedRecordCnt>" + countRecords(tx.xmlContent) + "</AcceptedRecordCnt>\n" +
                        "      <RejectedRecordCnt>0</RejectedRecordCnt>\n" +
                        "    </AcknowledgementDetail>";
            } else {
                status = "Rejected";
                acknowledgment = "<AcknowledgementDetail>\n" +
                        "      <TransmissionStatusCd>Rejected</TransmissionStatusCd>\n" +
                        "      <ErrorMessageDetail>\n" +
                        "        <ErrorMessageCd>MOCK-001</ErrorMessageCd>\n" +
                        "        <ErrorMessageTxt>Mock rejection for testing purposes</ErrorMessageTxt>\n" +
                        "      </ErrorMessageDetail>\n" +
                        "    </AcknowledgementDetail>";
            }
            tx.status = status;
        }

        String responseXml = String.format(
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<TransStatusOrAckResponse>\n" +
                "  <ReceiptId>%s</ReceiptId>\n" +
                "  <UniqueTransmissionId>%s</UniqueTransmissionId>\n" +
                "  <TransmissionStatusCd>%s</TransmissionStatusCd>\n" +
                "  <Timestamp>%s</Timestamp>\n" +
                "  %s\n" +
                "</TransStatusOrAckResponse>",
                tx.receiptId, tx.utid, status, tx.timestamp, acknowledgment);

        return ResponseEntity.ok(responseXml);
    }

    // === Helpers ===

    private String errorXml(String code, String message) {
        return String.format(
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<ErrorResponse>\n" +
                "  <ErrorCode>%s</ErrorCode>\n" +
                "  <ErrorMessage>%s</ErrorMessage>\n" +
                "</ErrorResponse>", code, message);
    }

    private String extractElement(String xml, String elementName) {
        String openTag = "<" + elementName + ">";
        String closeTag = "</" + elementName + ">";
        int start = xml.indexOf(openTag);
        int end = xml.indexOf(closeTag);
        if (start >= 0 && end > start) {
            return xml.substring(start + openTag.length(), end).trim();
        }
        return null;
    }

    private int countRecords(String xml) {
        int count = 0;
        int idx = 0;
        while ((idx = xml.indexOf("<RecordId>", idx)) >= 0) {
            count++;
            idx++;
        }
        return Math.max(count, 1);
    }

    private static class MockTransmission {
        String utid;
        String receiptId;
        String timestamp;
        String status;
        String xmlContent;
        long submittedAt;
    }
}

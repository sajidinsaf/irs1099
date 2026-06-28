package com.irs1099.service.iris;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Client for communicating with IRS IRIS A2A endpoints.
 * Supports both real IRS and mock service via configuration.
 */
@Service
@Slf4j
public class IrisSubmissionClient {

    private final WebClient webClient;
    private final String tokenEndpoint;
    private final String submitEndpoint;
    private final String statusEndpoint;
    private final boolean useMock;
    private final String mockBaseUrl;

    public IrisSubmissionClient(
            @Value("${app.iris.base-url:https://api.www4.irs.gov}") String baseUrl,
            @Value("${app.iris.test-base-url:https://api.alt.www4.irs.gov}") String testBaseUrl,
            @Value("${app.iris.token-endpoint:/auth/oauth/v2/token}") String tokenEndpoint,
            @Value("${app.iris.submit-endpoint:/IRIntakeAcceptanceA2A/1.0/irisa2a/v1/intake-acceptance}") String submitEndpoint,
            @Value("${app.iris.status-endpoint:/IRIntakeAcceptanceA2A/1.0/iris/transstatusorack}") String statusEndpoint,
            @Value("${app.iris.use-test-environment:true}") boolean useTestEnv,
            @Value("${app.iris.use-mock:true}") boolean useMock,
            @Value("${app.mail.base-url:http://localhost:8080}") String appBaseUrl) {

        String irsBaseUrl = useTestEnv ? testBaseUrl : baseUrl;
        this.useMock = useMock;
        this.mockBaseUrl = appBaseUrl.endsWith("/") ? appBaseUrl + "mock-irs" : appBaseUrl + "/mock-irs";
        this.tokenEndpoint = tokenEndpoint;
        this.submitEndpoint = submitEndpoint;
        this.statusEndpoint = statusEndpoint;

        this.webClient = WebClient.builder()
                .baseUrl(useMock ? this.mockBaseUrl : irsBaseUrl)
                .build();

        log.info("IRIS client initialized. useMock={}, baseUrl={}", useMock, useMock ? this.mockBaseUrl : irsBaseUrl);
    }

    /**
     * Get an OAuth2 access token from IRS (or mock).
     */
    public TokenResponse getAccessToken(String clientId, String clientJwt, String userJwt) {
        try {
            String body = "grant_type=urn:ietf:params:oauth:grant-type:jwt-bearer" +
                    "&assertion=" + userJwt +
                    "&client_assertion_type=urn:ietf:params:oauth:client-assertion-type:jwt-bearer" +
                    "&client_assertion=" + clientJwt;

            Map response = webClient.post()
                    .uri(tokenEndpoint)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (response == null) {
                return TokenResponse.error("No response from token endpoint");
            }

            return TokenResponse.success(
                    (String) response.get("access_token"),
                    (String) response.get("refresh_token"),
                    ((Number) response.get("expires_in")).intValue()
            );

        } catch (Exception e) {
            log.error("Failed to get access token", e);
            return TokenResponse.error("Token request failed: " + e.getMessage());
        }
    }

    /**
     * Submit an XML transmission to IRS (or mock).
     */
    public SubmitResponse submitTransmission(String accessToken, String xmlPayload) {
        try {
            byte[] xmlBytes = xmlPayload.getBytes(StandardCharsets.UTF_8);

            MultiValueMap<String, Object> formData = new LinkedMultiValueMap<>();
            formData.add("file", new ByteArrayResource(xmlBytes) {
                @Override
                public String getFilename() {
                    return "transmission.xml";
                }
            });

            String responseXml = webClient.post()
                    .uri(useMock ? "/irisa2a/v1/intake-acceptance" : submitEndpoint)
                    .header("Authorization", "Bearer " + accessToken)
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(BodyInserters.fromMultipartData(formData))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            if (responseXml == null) {
                return SubmitResponse.error("No response from submission endpoint");
            }

            // Parse response XML
            String receiptId = extractElement(responseXml, "ReceiptId");
            String utid = extractElement(responseXml, "UniqueTransmissionId");
            String timestamp = extractElement(responseXml, "Timestamp");

            if (receiptId != null) {
                return SubmitResponse.success(receiptId, utid, timestamp);
            }

            // Check for error
            String errorCode = extractElement(responseXml, "ErrorCode");
            String errorMsg = extractElement(responseXml, "ErrorMessage");
            return SubmitResponse.error(errorCode + ": " + errorMsg);

        } catch (Exception e) {
            log.error("Failed to submit transmission", e);
            return SubmitResponse.error("Submission failed: " + e.getMessage());
        }
    }

    /**
     * Get status/acknowledgment for a transmission.
     */
    public StatusResponse getStatus(String accessToken, String searchType, String searchValue) {
        try {
            String requestXml = String.format(
                    "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                    "<TransStatusOrAckRequest>\n" +
                    "  <SearchParameterTypeCd>%s</SearchParameterTypeCd>\n" +
                    "  <SearchParameterValueTxt>%s</SearchParameterValueTxt>\n" +
                    "</TransStatusOrAckRequest>",
                    searchType, searchValue);

            String responseXml = webClient.post()
                    .uri(useMock ? "/iris/transstatusorack" : statusEndpoint)
                    .header("Authorization", "Bearer " + accessToken)
                    .contentType(MediaType.APPLICATION_XML)
                    .bodyValue(requestXml)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            if (responseXml == null) {
                return StatusResponse.error("No response from status endpoint");
            }

            String status = extractElement(responseXml, "TransmissionStatusCd");
            String receiptId = extractElement(responseXml, "ReceiptId");
            String errorCode = extractElement(responseXml, "ErrorMessageCd");
            String errorMsg = extractElement(responseXml, "ErrorMessageTxt");
            String acceptedCount = extractElement(responseXml, "AcceptedRecordCnt");
            String rejectedCount = extractElement(responseXml, "RejectedRecordCnt");

            if (status != null) {
                return StatusResponse.success(status, receiptId, errorCode, errorMsg,
                        parseIntSafe(acceptedCount), parseIntSafe(rejectedCount));
            }

            return StatusResponse.error("Unknown response format");

        } catch (Exception e) {
            log.error("Failed to get status", e);
            return StatusResponse.error("Status request failed: " + e.getMessage());
        }
    }

    // === Response Types ===

    public static class TokenResponse {
        public final boolean success;
        public final String accessToken;
        public final String refreshToken;
        public final int expiresIn;
        public final String error;

        private TokenResponse(boolean success, String accessToken, String refreshToken, int expiresIn, String error) {
            this.success = success; this.accessToken = accessToken;
            this.refreshToken = refreshToken; this.expiresIn = expiresIn; this.error = error;
        }
        public static TokenResponse success(String access, String refresh, int expires) {
            return new TokenResponse(true, access, refresh, expires, null);
        }
        public static TokenResponse error(String error) {
            return new TokenResponse(false, null, null, 0, error);
        }
    }

    public static class SubmitResponse {
        public final boolean success;
        public final String receiptId;
        public final String utid;
        public final String timestamp;
        public final String error;

        private SubmitResponse(boolean success, String receiptId, String utid, String timestamp, String error) {
            this.success = success; this.receiptId = receiptId;
            this.utid = utid; this.timestamp = timestamp; this.error = error;
        }
        public static SubmitResponse success(String receiptId, String utid, String timestamp) {
            return new SubmitResponse(true, receiptId, utid, timestamp, null);
        }
        public static SubmitResponse error(String error) {
            return new SubmitResponse(false, null, null, null, error);
        }
    }

    public static class StatusResponse {
        public final boolean success;
        public final String status;
        public final String receiptId;
        public final String errorCode;
        public final String errorMessage;
        public final int acceptedCount;
        public final int rejectedCount;
        public final String error;

        private StatusResponse(boolean success, String status, String receiptId,
                               String errorCode, String errorMessage,
                               int acceptedCount, int rejectedCount, String error) {
            this.success = success; this.status = status; this.receiptId = receiptId;
            this.errorCode = errorCode; this.errorMessage = errorMessage;
            this.acceptedCount = acceptedCount; this.rejectedCount = rejectedCount; this.error = error;
        }
        public static StatusResponse success(String status, String receiptId,
                                              String errorCode, String errorMessage,
                                              int accepted, int rejected) {
            return new StatusResponse(true, status, receiptId, errorCode, errorMessage, accepted, rejected, null);
        }
        public static StatusResponse error(String error) {
            return new StatusResponse(false, null, null, null, null, 0, 0, error);
        }
    }

    // === Helpers ===

    private String extractElement(String xml, String name) {
        String open = "<" + name + ">";
        String close = "</" + name + ">";
        int start = xml.indexOf(open);
        int end = xml.indexOf(close);
        if (start >= 0 && end > start) return xml.substring(start + open.length(), end).trim();
        return null;
    }

    private int parseIntSafe(String val) {
        try { return val != null ? Integer.parseInt(val) : 0; } catch (NumberFormatException e) { return 0; }
    }
}

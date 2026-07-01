package com.irs1099.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/observability")
@Slf4j
public class ObservabilityController {

    @Value("${app.verops.rum-api-key:}")
    private String rumApiKey;

    @Value("${app.verops.rum-app-id:}")
    private String rumAppId;

    @Value("${app.verops.endpoint:https://ingest.verops.io/rum/v2/beacon}")
    private String veropsEndpoint;

    @Value("${app.verops.enabled:false}")
    private boolean veropsEnabled;

    private final RestTemplate restTemplate = new RestTemplate();

    @GetMapping("/rum-config")
    public ResponseEntity<Map<String, Object>> getRumConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("enabled", veropsEnabled);
        return ResponseEntity.ok(config);
    }

    /**
     * Proxy endpoint for frontend RUM data.
     * Frontend sends web vitals here (same-origin, no CORS),
     * backend forwards to Verops (server-to-server, no CORS).
     */
    @PostMapping("/rum/collect")
    public ResponseEntity<Map<String, String>> collectRumData(@RequestBody Map<String, Object> metrics) {
        if (!veropsEnabled || rumApiKey == null || rumApiKey.isEmpty()) {
            return ResponseEntity.ok(Map.of("status", "disabled"));
        }

        try {
            // Add app_id and api_key server-side (never exposed to frontend)
            metrics.put("app_id", rumAppId);
            metrics.put("api_key", rumApiKey);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(metrics, headers);
            restTemplate.postForEntity(veropsEndpoint, request, String.class);

            return ResponseEntity.ok(Map.of("status", "sent"));
        } catch (Exception e) {
            log.debug("RUM forward failed: {}", e.getMessage());
            return ResponseEntity.ok(Map.of("status", "error"));
        }
    }
}

package com.irs1099.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

/**
 * Verops RUM initialization.
 * Sends service startup beacon via REST API.
 * Frontend RUM (web vitals) is handled by the React app directly.
 */
@Configuration
@Slf4j
public class VeropsConfig {

    @Value("${app.verops.enabled:false}")
    private boolean enabled;

    @Value("${app.verops.rum-app-id:rum_d1e8a8c104114024}")
    private String appId;

    @Value("${app.verops.rum-api-key:}")
    private String apiKey;

    @Value("${app.verops.endpoint:https://ingest.verops.io/rum/v2/beacon}")
    private String endpoint;

    @Value("${app.verops.environment:dev}")
    private String environment;

    @Value("${app.verops.service-name:irs1099}")
    private String serviceName;

    @PostConstruct
    public void init() {
        if (!enabled || apiKey == null || apiKey.isEmpty()) {
            log.info("Verops disabled (enabled={}, apiKey={})", enabled, apiKey != null ? "set" : "empty");
            return;
        }

        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("app_id", appId);
            payload.put("event_type", "service_start");
            payload.put("service_name", serviceName);
            payload.put("environment", environment);
            payload.put("timestamp", System.currentTimeMillis());

            WebClient.create(endpoint)
                    .post()
                    .uri("?apiKey=" + apiKey)
                    .bodyValue(payload)
                    .retrieve()
                    .bodyToMono(String.class)
                    .subscribe(
                            r -> log.info("Verops initialized. appId={}, env={}, service={}", appId, environment, serviceName),
                            e -> log.warn("Verops startup beacon failed (non-fatal): {}", e.getMessage())
                    );
        } catch (Exception e) {
            log.warn("Failed to initialize Verops: {}", e.getMessage());
        }
    }
}

package com.irs1099.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/observability")
public class ObservabilityController {

    @Value("${app.verops.rum-api-key:}")
    private String rumApiKey;

    @Value("${app.verops.base-url:https://app.verops.io}")
    private String veropsBaseUrl;

    @Value("${app.verops.enabled:false}")
    private boolean veropsEnabled;

    @GetMapping("/rum-config")
    public ResponseEntity<Map<String, Object>> getRumConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("enabled", veropsEnabled && rumApiKey != null && !rumApiKey.isEmpty());
        config.put("apiKey", rumApiKey);
        config.put("endpoint", veropsBaseUrl + "/api/v1.0/rum/events");
        config.put("environment", "production");
        return ResponseEntity.ok(config);
    }
}

package com.irs1099.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

/**
 * Verops configuration.
 * Frontend RUM is handled by the official Verops JS SDK (loaded via CDN in index.html).
 * Backend telemetry is handled by TelemetryService.
 */
@Configuration
@Slf4j
public class VeropsConfig {

    @Value("${app.verops.enabled:false}")
    private boolean enabled;

    @Value("${app.verops.service-name:irs1099}")
    private String serviceName;

    @Value("${app.verops.environment:dev}")
    private String environment;

    @PostConstruct
    public void init() {
        log.info("Verops config: enabled={}, service={}, env={}", enabled, serviceName, environment);
        log.info("Verops RUM: handled by frontend JS SDK (verops-rum.min.js)");
    }
}

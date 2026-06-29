package com.irs1099.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Verifies Cloudflare Turnstile CAPTCHA tokens.
 * When disabled (dev), all tokens are accepted.
 */
@Service
@Slf4j
public class TurnstileService {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${app.turnstile.secret-key:}")
    private String secretKey;

    @Value("${app.turnstile.verify-url:https://challenges.cloudflare.com/turnstile/v0/siteverify}")
    private String verifyUrl;

    @Value("${app.turnstile.enabled:false}")
    private boolean enabled;

    public boolean verifyToken(String token) {
        if (!enabled) {
            log.debug("Turnstile disabled, accepting all tokens");
            return true;
        }

        if (token == null || token.isEmpty()) {
            log.warn("Turnstile token is empty");
            return false;
        }

        try {
            String responseBody = WebClient.create()
                    .post()
                    .uri(verifyUrl)
                    .body(BodyInserters.fromFormData("secret", secretKey)
                            .with("response", token))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            if (responseBody == null) {
                log.error("Turnstile verification returned null response");
                return false;
            }

            JsonNode json = objectMapper.readTree(responseBody);
            boolean success = json.path("success").asBoolean(false);

            if (!success) {
                log.warn("Turnstile verification failed: {}", responseBody);
            } else {
                log.debug("Turnstile verification succeeded");
            }

            return success;
        } catch (Exception e) {
            log.error("Turnstile verification error: {}", e.getMessage());
            return false;
        }
    }
}

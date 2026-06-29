package com.irs1099.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.Map;

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

    /**
     * Verify a Turnstile token with Cloudflare.
     * Returns true if valid, false if invalid.
     * When disabled, always returns true.
     */
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
            Map<String, String> formData = new HashMap<>();
            formData.put("secret", secretKey);
            formData.put("response", token);

            String responseBody = WebClient.create(verifyUrl)
                    .post()
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .bodyValue("secret=" + secretKey + "&response=" + token)
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
            }

            return success;
        } catch (Exception e) {
            log.error("Turnstile verification error", e);
            return false;
        }
    }
}

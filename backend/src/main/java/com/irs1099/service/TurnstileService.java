package com.irs1099.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

/**
 * Verifies Cloudflare Turnstile CAPTCHA tokens using RestTemplate.
 * When disabled (dev), all tokens are accepted.
 */
@Service
@Slf4j
public class TurnstileService {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestTemplate restTemplate = new RestTemplate();

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
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("secret", secretKey);
            body.add("response", token);

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(verifyUrl, request, String.class);

            if (response.getBody() == null) {
                log.error("Turnstile verification returned null response");
                return false;
            }

            JsonNode json = objectMapper.readTree(response.getBody());
            boolean success = json.path("success").asBoolean(false);

            if (!success) {
                log.warn("Turnstile verification failed: {}", response.getBody());
            } else {
                log.info("Turnstile verification succeeded");
            }

            return success;
        } catch (Exception e) {
            log.error("Turnstile verification error: {}", e.getMessage());
            return false;
        }
    }
}

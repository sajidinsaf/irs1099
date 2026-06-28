package com.irs1099.controller;

import com.irs1099.dto.response.ApiResponse;
import com.irs1099.entity.Payment;
import com.irs1099.entity.Subscription;
import com.irs1099.security.UserPrincipal;
import com.irs1099.service.payment.PaymentService;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final PaymentService paymentService;

    @Value("${app.stripe.webhook-secret:}")
    private String webhookSecret;

    @GetMapping("/config")
    public ResponseEntity<Map<String, String>> getConfig() {
        return ResponseEntity.ok(paymentService.getConfig());
    }

    @GetMapping("/plans")
    public ResponseEntity<Map<String, Object>> getPlans() {
        return ResponseEntity.ok(paymentService.getPricingPlans());
    }

    @PostMapping("/checkout/per-form")
    public ResponseEntity<Map<String, String>> createPerFormCheckout(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody Map<String, Integer> request) throws StripeException {
        int formCount = request.getOrDefault("formCount", 1);
        return ResponseEntity.ok(paymentService.createCheckoutSession(principal.getId(), formCount));
    }

    @PostMapping("/checkout/subscription")
    public ResponseEntity<Map<String, String>> createSubscriptionCheckout(
            @AuthenticationPrincipal UserPrincipal principal) throws StripeException {
        return ResponseEntity.ok(paymentService.createSubscriptionCheckout(principal.getId()));
    }

    @GetMapping("/history")
    public ResponseEntity<Page<Payment>> getPaymentHistory(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(
                paymentService.getPaymentHistory(principal.getId(), PageRequest.of(page, size)));
    }

    @GetMapping("/subscription")
    public ResponseEntity<Map<String, Object>> getSubscription(
            @AuthenticationPrincipal UserPrincipal principal) {
        Subscription sub = paymentService.getActiveSubscription(principal.getId());
        Map<String, Object> response = new HashMap<>();
        if (sub != null) {
            response.put("active", true);
            response.put("planType", sub.getPlanType().name());
            response.put("formsIncluded", sub.getFormsIncluded());
            response.put("formsUsed", sub.getFormsUsed());
            response.put("endDate", sub.getEndDate().toString());
        } else {
            response.put("active", false);
        }
        return ResponseEntity.ok(response);
    }

    /**
     * Stripe webhook handler for async payment events.
     */
    @PostMapping("/webhook")
    public ResponseEntity<ApiResponse> handleWebhook(
            @RequestBody String payload,
            @RequestHeader(value = "Stripe-Signature", required = false) String sigHeader) {
        try {
            if (webhookSecret == null || webhookSecret.isEmpty() || sigHeader == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.error("Webhook secret not configured"));
            }
            Event event = Webhook.constructEvent(payload, sigHeader, webhookSecret);

            if ("checkout.session.completed".equals(event.getType())) {
                Session session = (Session) event.getDataObjectDeserializer()
                        .getObject().orElse(null);
                if (session != null) {
                    paymentService.handlePaymentSuccess(session.getId());
                }
            }

            return ResponseEntity.ok(ApiResponse.success("Webhook processed"));
        } catch (SignatureVerificationException e) {
            log.error("Webhook signature verification failed", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Invalid signature"));
        } catch (Exception e) {
            log.error("Webhook processing failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Webhook processing failed"));
        }
    }
}

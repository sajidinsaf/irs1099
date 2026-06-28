package com.irs1099.service.payment;

import com.irs1099.dto.response.ApiResponse;
import com.irs1099.entity.Payment;
import com.irs1099.entity.Subscription;
import com.irs1099.entity.User;
import com.irs1099.exception.BadRequestException;
import com.irs1099.exception.ResourceNotFoundException;
import com.irs1099.repository.PaymentRepository;
import com.irs1099.repository.SubscriptionRepository;
import com.irs1099.repository.UserRepository;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.checkout.Session;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;

    @Value("${app.stripe.publishable-key:}")
    private String publishableKey;

    @Value("${app.stripe.prices.per-form:}")
    private String perFormPriceId;

    @Value("${app.stripe.prices.professional:}")
    private String professionalPriceId;

    @Value("${app.mail.base-url:http://localhost:5173}")
    private String baseUrl;

    public Map<String, String> getConfig() {
        Map<String, String> config = new HashMap<>();
        config.put("publishableKey", publishableKey);
        return config;
    }

    public Map<String, Object> getPricingPlans() {
        Map<String, Object> plans = new HashMap<>();

        Map<String, Object> perForm = new HashMap<>();
        perForm.put("name", "Pay Per Form");
        perForm.put("price", "2.99");
        perForm.put("unit", "per form");
        perForm.put("priceId", perFormPriceId);

        Map<String, Object> professional = new HashMap<>();
        professional.put("name", "Professional");
        professional.put("price", "149.00");
        professional.put("unit", "per year");
        professional.put("priceId", professionalPriceId);
        professional.put("formsIncluded", 500);

        plans.put("perForm", perForm);
        plans.put("professional", professional);
        return plans;
    }

    /**
     * Create a Stripe Checkout Session for pay-per-form (one-time payment).
     */
    public Map<String, String> createCheckoutSession(Long userId, int formCount) throws StripeException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        String customerId = getOrCreateStripeCustomer(user);

        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setCustomer(customerId)
                .setSuccessUrl(baseUrl + "/dashboard?payment=success")
                .setCancelUrl(baseUrl + "/dashboard?payment=cancelled")
                .addLineItem(SessionCreateParams.LineItem.builder()
                        .setPrice(perFormPriceId)
                        .setQuantity((long) formCount)
                        .build())
                .putMetadata("userId", userId.toString())
                .putMetadata("type", "per_form")
                .putMetadata("formCount", String.valueOf(formCount))
                .build();

        Session session = Session.create(params);

        Map<String, String> response = new HashMap<>();
        response.put("sessionId", session.getId());
        response.put("url", session.getUrl());
        return response;
    }

    /**
     * Create a Stripe Checkout Session for Professional subscription.
     */
    public Map<String, String> createSubscriptionCheckout(Long userId) throws StripeException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        String customerId = getOrCreateStripeCustomer(user);

        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.SUBSCRIPTION)
                .setCustomer(customerId)
                .setSuccessUrl(baseUrl + "/dashboard?payment=success")
                .setCancelUrl(baseUrl + "/dashboard?payment=cancelled")
                .addLineItem(SessionCreateParams.LineItem.builder()
                        .setPrice(professionalPriceId)
                        .setQuantity(1L)
                        .build())
                .putMetadata("userId", userId.toString())
                .putMetadata("type", "professional")
                .build();

        Session session = Session.create(params);

        Map<String, String> response = new HashMap<>();
        response.put("sessionId", session.getId());
        response.put("url", session.getUrl());
        return response;
    }

    /**
     * Handle successful payment from Stripe webhook or redirect.
     */
    @Transactional
    public void handlePaymentSuccess(String sessionId) throws StripeException {
        Session session = Session.retrieve(sessionId);

        String userId = session.getMetadata().get("userId");
        String type = session.getMetadata().get("type");

        if (userId == null) return;

        User user = userRepository.findById(Long.parseLong(userId)).orElse(null);
        if (user == null) return;

        if ("per_form".equals(type)) {
            String formCount = session.getMetadata().get("formCount");
            Payment payment = Payment.builder()
                    .user(user)
                    .stripePaymentIntentId(session.getPaymentIntent())
                    .amount(BigDecimal.valueOf(session.getAmountTotal() / 100.0))
                    .currency(session.getCurrency().toUpperCase())
                    .status(Payment.PaymentStatus.SUCCEEDED)
                    .description("Pay per form: " + formCount + " forms")
                    .build();
            paymentRepository.save(payment);
        } else if ("professional".equals(type)) {
            Subscription subscription = Subscription.builder()
                    .user(user)
                    .planType(Subscription.PlanType.PROFESSIONAL)
                    .stripeSubscriptionId(session.getSubscription())
                    .stripeCustomerId(session.getCustomer())
                    .status(Subscription.SubscriptionStatus.ACTIVE)
                    .startDate(LocalDate.now())
                    .endDate(LocalDate.now().plusYears(1))
                    .formsIncluded(500)
                    .formsUsed(0)
                    .build();
            subscriptionRepository.save(subscription);
        }
    }

    public Page<Payment> getPaymentHistory(Long userId, Pageable pageable) {
        return paymentRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    public Subscription getActiveSubscription(Long userId) {
        return subscriptionRepository.findByUserIdAndStatus(userId, Subscription.SubscriptionStatus.ACTIVE)
                .orElse(null);
    }

    private String getOrCreateStripeCustomer(User user) throws StripeException {
        // Check if user already has a Stripe customer via subscription
        Subscription sub = subscriptionRepository
                .findByUserIdAndStatus(user.getId(), Subscription.SubscriptionStatus.ACTIVE)
                .orElse(null);

        if (sub != null && sub.getStripeCustomerId() != null) {
            return sub.getStripeCustomerId();
        }

        // Create new customer
        CustomerCreateParams params = CustomerCreateParams.builder()
                .setEmail(user.getEmail())
                .setName(user.getFirstName() + " " + user.getLastName())
                .putMetadata("userId", user.getId().toString())
                .build();

        Customer customer = Customer.create(params);
        return customer.getId();
    }
}

package com.irs1099.service.notification;

import com.irs1099.entity.Notification;
import com.irs1099.entity.Submission;
import com.irs1099.entity.User;
import com.irs1099.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailNotificationService implements NotificationService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final NotificationRepository notificationRepository;

    @Value("${app.mail.from}")
    private String fromAddress;

    @Value("${app.mail.base-url}")
    private String baseUrl;

    @Override
    @Async
    public void sendEmailVerification(User user, String token) {
        String subject = "Verify your email - IRS 1099 Filing Platform";
        Context context = new Context();
        context.setVariable("name", user.getFirstName());
        context.setVariable("verificationUrl", baseUrl + "/verify-email?token=" + token);

        sendEmail(user, subject, "email/verification", context, Notification.NotificationType.EMAIL_VERIFICATION);
    }

    @Override
    @Async
    public void sendPasswordReset(User user, String token) {
        String subject = "Reset your password - IRS 1099 Filing Platform";
        Context context = new Context();
        context.setVariable("name", user.getFirstName());
        context.setVariable("resetUrl", baseUrl + "/reset-password?token=" + token);

        sendEmail(user, subject, "email/password-reset", context, Notification.NotificationType.PASSWORD_RESET);
    }

    @Override
    @Async
    public void sendPaymentConfirmation(User user, String amount, String description) {
        String subject = "Payment Confirmation - IRS 1099 Filing Platform";
        Context context = new Context();
        context.setVariable("name", user.getFirstName());
        context.setVariable("amount", amount);
        context.setVariable("description", description);

        sendEmail(user, subject, "email/payment-confirmation", context, Notification.NotificationType.PAYMENT_CONFIRMATION);
    }

    @Override
    @Async
    public void sendSubmissionStatus(User user, Submission submission) {
        String subject = String.format("Submission %s - %s", submission.getStatus(), submission.getFormType());
        Context context = new Context();
        context.setVariable("name", user.getFirstName());
        context.setVariable("formType", submission.getFormType());
        context.setVariable("status", submission.getStatus().name());
        context.setVariable("receiptId", submission.getReceiptId());
        context.setVariable("submittedAt", submission.getSubmittedAt());

        Notification.NotificationType type;
        switch (submission.getStatus()) {
            case ACCEPTED:
                type = Notification.NotificationType.SUBMISSION_ACCEPTED;
                break;
            case REJECTED:
                type = Notification.NotificationType.SUBMISSION_REJECTED;
                break;
            case ACCEPTED_WITH_ERRORS:
            case PARTIALLY_ACCEPTED:
                type = Notification.NotificationType.SUBMISSION_ERRORS;
                break;
            default:
                type = Notification.NotificationType.SUBMISSION_SUBMITTED;
        }

        sendEmail(user, subject, "email/submission-status", context, type);
    }

    @Override
    @Async
    public void sendDeadlineReminder(User user, String formType, String deadline) {
        String subject = String.format("Filing Deadline Reminder: %s due %s", formType, deadline);
        Context context = new Context();
        context.setVariable("name", user.getFirstName());
        context.setVariable("formType", formType);
        context.setVariable("deadline", deadline);

        sendEmail(user, subject, "email/deadline-reminder", context, Notification.NotificationType.DEADLINE_REMINDER);
    }

    @Override
    @Async
    public void sendSubscriptionRenewal(User user, String planName, String renewalDate) {
        String subject = "Subscription Renewal Reminder - IRS 1099 Filing Platform";
        Context context = new Context();
        context.setVariable("name", user.getFirstName());
        context.setVariable("planName", planName);
        context.setVariable("renewalDate", renewalDate);

        sendEmail(user, subject, "email/subscription-renewal", context, Notification.NotificationType.SUBSCRIPTION_RENEWAL);
    }

    private void sendEmail(User user, String subject, String template, Context context, Notification.NotificationType type) {
        try {
            String htmlContent = templateEngine.process(template, context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromAddress);
            helper.setTo(user.getEmail());
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            mailSender.send(message);

            saveNotification(user, type, subject, htmlContent, true);
            log.info("Email sent to {} - {}", user.getEmail(), subject);
        } catch (MessagingException e) {
            log.error("Failed to send email to {} - {}", user.getEmail(), subject, e);
            saveNotification(user, type, subject, "Failed to send: " + e.getMessage(), false);
        }
    }

    private void saveNotification(User user, Notification.NotificationType type, String subject, String body, boolean emailSent) {
        notificationRepository.save(Notification.builder()
                .user(user)
                .type(type)
                .subject(subject)
                .body(body)
                .emailSent(emailSent)
                .build());
    }
}

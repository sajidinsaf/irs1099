package com.irs1099.service.admin;

import com.irs1099.entity.*;
import com.irs1099.exception.BadRequestException;
import com.irs1099.exception.ResourceNotFoundException;
import com.irs1099.repository.*;
import com.irs1099.util.EncryptionUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminService {

    private final UserRepository userRepository;
    private final BusinessProfileRepository businessProfileRepository;
    private final SubmissionRepository submissionRepository;
    private final FormRecordRepository formRecordRepository;
    private final PaymentRepository paymentRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final NotificationRepository notificationRepository;
    private final AuditLogRepository auditLogRepository;
    private final PasswordEncoder passwordEncoder;
    private final EncryptionUtil encryptionUtil;

    // === Users ===

    public Page<User> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    public User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
    }

    @Transactional
    public User updateUserRole(Long userId, String role, Long adminId) {
        User user = getUser(userId);
        user.setRole(User.Role.valueOf(role));
        userRepository.save(user);
        logAction(adminId, "CHANGE_ROLE", "User", userId, "Role changed to " + role);
        return user;
    }

    @Transactional
    public User forceVerifyEmail(Long userId, Long adminId) {
        User user = getUser(userId);
        user.setEmailVerified(true);
        user.setEmailVerificationToken(null);
        user.setEmailVerificationExpiry(null);
        userRepository.save(user);
        logAction(adminId, "FORCE_VERIFY_EMAIL", "User", userId, "Email force-verified by admin");
        return user;
    }

    @Transactional
    public void resetUserPassword(Long userId, String newPassword, Long adminId) {
        User user = getUser(userId);
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setPasswordResetToken(null);
        user.setPasswordResetExpiry(null);
        userRepository.save(user);
        logAction(adminId, "RESET_PASSWORD", "User", userId, "Password reset by admin");
    }

    @Transactional
    public void deactivateUser(Long userId, Long adminId) {
        User user = getUser(userId);
        user.setEmailVerified(false); // Prevents login
        userRepository.save(user);
        logAction(adminId, "DEACTIVATE_USER", "User", userId, "User deactivated by admin");
    }

    // === Submissions ===

    public Page<Submission> getAllSubmissions(Pageable pageable) {
        return submissionRepository.findAll(pageable);
    }

    public Page<Submission> getSubmissionsByStatus(Submission.SubmissionStatus status, Pageable pageable) {
        return submissionRepository.findByStatus(status, pageable);
    }

    @Transactional
    public Submission updateSubmissionStatus(Long submissionId, String status, Long adminId) {
        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Submission", "id", submissionId));
        Submission.SubmissionStatus newStatus = Submission.SubmissionStatus.valueOf(status);
        submission.setStatus(newStatus);
        if (newStatus == Submission.SubmissionStatus.ACCEPTED || newStatus == Submission.SubmissionStatus.REJECTED) {
            submission.setAcknowledgedAt(LocalDateTime.now());
        }
        submissionRepository.save(submission);
        logAction(adminId, "UPDATE_SUBMISSION_STATUS", "Submission", submissionId, "Status changed to " + status);
        return submission;
    }

    // === Payments ===

    public Page<Payment> getAllPayments(Pageable pageable) {
        return paymentRepository.findAll(pageable);
    }

    public Page<Subscription> getAllSubscriptions(Pageable pageable) {
        return subscriptionRepository.findAll(pageable);
    }

    @Transactional
    public Subscription modifySubscription(Long subscriptionId, Map<String, Object> changes, Long adminId) {
        Subscription sub = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription", "id", subscriptionId));

        if (changes.containsKey("status")) {
            sub.setStatus(Subscription.SubscriptionStatus.valueOf((String) changes.get("status")));
        }
        if (changes.containsKey("formsIncluded")) {
            sub.setFormsIncluded((Integer) changes.get("formsIncluded"));
        }
        if (changes.containsKey("formsUsed")) {
            sub.setFormsUsed((Integer) changes.get("formsUsed"));
        }

        subscriptionRepository.save(sub);
        logAction(adminId, "MODIFY_SUBSCRIPTION", "Subscription", subscriptionId, "Modified: " + changes.keySet());
        return sub;
    }

    // === Audit Log ===

    public Page<AuditLog> getAuditLog(Pageable pageable) {
        return auditLogRepository.findAll(pageable);
    }

    // === Notifications ===

    public Page<Notification> getAllNotifications(Pageable pageable) {
        return notificationRepository.findAll(pageable);
    }

    @Transactional
    public void sendSystemNotification(String subject, String body, Long adminId) {
        for (User user : userRepository.findAll()) {
            Notification notification = Notification.builder()
                    .user(user)
                    .type(Notification.NotificationType.SYSTEM)
                    .subject(subject)
                    .body(body)
                    .build();
            notificationRepository.save(notification);
        }
        logAction(adminId, "SEND_SYSTEM_NOTIFICATION", "Notification", null, "Subject: " + subject);
    }

    // === Dashboard Stats ===

    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();

        stats.put("totalUsers", userRepository.count());
        stats.put("totalSubmissions", submissionRepository.count());
        stats.put("totalPayments", paymentRepository.count());

        // Submission status breakdown
        Map<String, Long> submissionStats = new HashMap<>();
        for (Submission.SubmissionStatus status : Submission.SubmissionStatus.values()) {
            long count = submissionRepository.findByStatus(status, Pageable.unpaged()).getTotalElements();
            submissionStats.put(status.name(), count);
        }
        stats.put("submissionsByStatus", submissionStats);

        // Subscription stats
        long activeSubscriptions = 0;
        try {
            activeSubscriptions = subscriptionRepository.findAll().stream()
                    .filter(s -> s.getStatus() == Subscription.SubscriptionStatus.ACTIVE)
                    .count();
        } catch (Exception ignored) {}
        stats.put("activeSubscriptions", activeSubscriptions);

        return stats;
    }

    // === Helpers ===

    private void logAction(Long adminId, String action, String entityType, Long entityId, String details) {
        AuditLog log = AuditLog.builder()
                .userId(adminId)
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .details(details)
                .timestamp(LocalDateTime.now())
                .build();
        auditLogRepository.save(log);
    }
}

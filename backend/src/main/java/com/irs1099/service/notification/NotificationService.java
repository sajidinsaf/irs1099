package com.irs1099.service.notification;

import com.irs1099.entity.Submission;
import com.irs1099.entity.User;

public interface NotificationService {
    void sendEmailVerification(User user, String token);
    void sendPasswordReset(User user, String token);
    void sendPaymentConfirmation(User user, String amount, String description);
    void sendSubmissionStatus(User user, Submission submission);
    void sendDeadlineReminder(User user, String formType, String deadline);
    void sendSubscriptionRenewal(User user, String planName, String renewalDate);
}

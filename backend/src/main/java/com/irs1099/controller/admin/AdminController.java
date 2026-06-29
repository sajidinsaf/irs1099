package com.irs1099.controller.admin;

import com.irs1099.dto.response.ApiResponse;
import com.irs1099.entity.*;
import com.irs1099.security.UserPrincipal;
import com.irs1099.service.admin.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    // === Dashboard ===

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getDashboardStats() {
        return ResponseEntity.ok(adminService.getDashboardStats());
    }

    // === Users ===

    @GetMapping("/users")
    public ResponseEntity<Page<User>> getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(adminService.getAllUsers(
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))));
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<User> getUser(@PathVariable Long userId) {
        return ResponseEntity.ok(adminService.getUser(userId));
    }

    @PostMapping("/users/{userId}/role")
    public ResponseEntity<User> updateUserRole(
            @PathVariable Long userId,
            @RequestBody Map<String, String> request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(adminService.updateUserRole(userId, request.get("role"), principal.getId()));
    }

    @PostMapping("/users/{userId}/verify-email")
    public ResponseEntity<User> forceVerifyEmail(
            @PathVariable Long userId,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(adminService.forceVerifyEmail(userId, principal.getId()));
    }

    @PostMapping("/users/{userId}/reset-password")
    public ResponseEntity<ApiResponse> resetPassword(
            @PathVariable Long userId,
            @RequestBody Map<String, String> request,
            @AuthenticationPrincipal UserPrincipal principal) {
        adminService.resetUserPassword(userId, request.get("password"), principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Password reset"));
    }

    @PostMapping("/users/{userId}/deactivate")
    public ResponseEntity<ApiResponse> deactivateUser(
            @PathVariable Long userId,
            @AuthenticationPrincipal UserPrincipal principal) {
        adminService.deactivateUser(userId, principal.getId());
        return ResponseEntity.ok(ApiResponse.success("User deactivated"));
    }

    // === Submissions ===

    @GetMapping("/submissions")
    public ResponseEntity<Page<Submission>> getSubmissions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        if (status != null && !status.isEmpty()) {
            return ResponseEntity.ok(adminService.getSubmissionsByStatus(
                    Submission.SubmissionStatus.valueOf(status), pageRequest));
        }
        return ResponseEntity.ok(adminService.getAllSubmissions(pageRequest));
    }

    @PostMapping("/submissions/{submissionId}/status")
    public ResponseEntity<Submission> updateSubmissionStatus(
            @PathVariable Long submissionId,
            @RequestBody Map<String, String> request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(adminService.updateSubmissionStatus(
                submissionId, request.get("status"), principal.getId()));
    }

    // === Payments ===

    @GetMapping("/payments")
    public ResponseEntity<Page<Payment>> getPayments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(adminService.getAllPayments(
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))));
    }

    // === Subscriptions ===

    @GetMapping("/subscriptions")
    public ResponseEntity<Page<Subscription>> getSubscriptions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(adminService.getAllSubscriptions(
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))));
    }

    @PostMapping("/subscriptions/{subscriptionId}/modify")
    public ResponseEntity<Subscription> modifySubscription(
            @PathVariable Long subscriptionId,
            @RequestBody Map<String, Object> changes,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(adminService.modifySubscription(subscriptionId, changes, principal.getId()));
    }

    // === Audit Log ===

    @GetMapping("/audit-log")
    public ResponseEntity<Page<AuditLog>> getAuditLog(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return ResponseEntity.ok(adminService.getAuditLog(
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "timestamp"))));
    }

    // === Notifications ===

    @GetMapping("/notifications")
    public ResponseEntity<Page<Notification>> getNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(adminService.getAllNotifications(
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))));
    }

    @PostMapping("/notifications/system")
    public ResponseEntity<ApiResponse> sendSystemNotification(
            @RequestBody Map<String, String> request,
            @AuthenticationPrincipal UserPrincipal principal) {
        adminService.sendSystemNotification(request.get("subject"), request.get("body"), principal.getId());
        return ResponseEntity.ok(ApiResponse.success("System notification sent to all users"));
    }
}

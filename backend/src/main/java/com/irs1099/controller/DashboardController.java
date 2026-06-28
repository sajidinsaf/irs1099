package com.irs1099.controller;

import com.irs1099.entity.Submission;
import com.irs1099.repository.NotificationRepository;
import com.irs1099.repository.SubmissionRepository;
import com.irs1099.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final SubmissionRepository submissionRepository;
    private final NotificationRepository notificationRepository;

    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getDashboardSummary(
            @AuthenticationPrincipal UserPrincipal principal) {
        Long userId = principal.getId();

        Page<Submission> recentSubmissions = submissionRepository
                .findByUserIdOrderByCreatedAtDesc(userId, PageRequest.of(0, 5));
        long unreadNotifications = notificationRepository
                .countByUserIdAndReadAtIsNull(userId);

        Map<String, Object> summary = new HashMap<>();
        summary.put("recentSubmissions", recentSubmissions.getContent());
        summary.put("totalSubmissions", recentSubmissions.getTotalElements());
        summary.put("unreadNotifications", unreadNotifications);

        return ResponseEntity.ok(summary);
    }
}

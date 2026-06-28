package com.irs1099.repository;

import com.irs1099.entity.Submission;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubmissionRepository extends JpaRepository<Submission, Long> {
    Page<Submission> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    List<Submission> findByUserIdAndTaxYear(Long userId, int taxYear);
    Optional<Submission> findByIdAndUserId(Long id, Long userId);
    Optional<Submission> findByReceiptId(String receiptId);
    List<Submission> findByStatus(Submission.SubmissionStatus status);
}

package com.irs1099.repository;

import com.irs1099.entity.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Page<Payment> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    Optional<Payment> findByStripePaymentIntentId(String paymentIntentId);
}

package com.irs1099.repository;

import com.irs1099.entity.FormRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FormRecordRepository extends JpaRepository<FormRecord, Long> {
    List<FormRecord> findBySubmissionId(Long submissionId);
    long countBySubmissionId(Long submissionId);
}

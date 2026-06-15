package com.jobflow.backend.application;

import com.jobflow.backend.domain.ApplicationStatus;
import com.jobflow.backend.domain.JobApplication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface ApplicationRepository {

    JobApplication save(JobApplication application);

    Optional<JobApplication> findById(UUID id);

    Optional<JobApplication> findByIdempotencyKey(String idempotencyKey);

    Page<JobApplication> search(ApplicationStatus status, Boolean remote, Pageable pageable);

    long count();

    long countByRemoteTrue();

    long countByStatus(ApplicationStatus status);
}

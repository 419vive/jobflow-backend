package com.jobflow.backend.infrastructure;

import com.jobflow.backend.domain.ApplicationStatus;
import com.jobflow.backend.domain.JobApplication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

interface JpaApplicationJpaRepository extends JpaRepository<JobApplication, UUID> {

    Optional<JobApplication> findByIdempotencyKey(String idempotencyKey);

    Page<JobApplication> findByStatus(ApplicationStatus status, Pageable pageable);

    Page<JobApplication> findByRemote(boolean remote, Pageable pageable);

    Page<JobApplication> findByStatusAndRemote(ApplicationStatus status, boolean remote, Pageable pageable);

    long countByRemoteTrue();

    long countByStatus(ApplicationStatus status);
}

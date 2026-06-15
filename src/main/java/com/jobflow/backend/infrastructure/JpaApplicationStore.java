package com.jobflow.backend.infrastructure;

import com.jobflow.backend.application.ApplicationRepository;
import com.jobflow.backend.domain.ApplicationStatus;
import com.jobflow.backend.domain.JobApplication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class JpaApplicationStore implements ApplicationRepository {

    private final JpaApplicationJpaRepository repository;

    public JpaApplicationStore(JpaApplicationJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public JobApplication save(JobApplication application) {
        return repository.save(application);
    }

    @Override
    public Optional<JobApplication> findById(UUID id) {
        return repository.findById(id);
    }

    @Override
    public Optional<JobApplication> findByIdempotencyKey(String idempotencyKey) {
        return repository.findByIdempotencyKey(idempotencyKey);
    }

    @Override
    public Page<JobApplication> search(ApplicationStatus status, Boolean remote, Pageable pageable) {
        if (status != null && remote != null) {
            return repository.findByStatusAndRemote(status, remote, pageable);
        }
        if (status != null) {
            return repository.findByStatus(status, pageable);
        }
        if (remote != null) {
            return repository.findByRemote(remote, pageable);
        }
        return repository.findAll(pageable);
    }

    @Override
    public long count() {
        return repository.count();
    }

    @Override
    public long countByRemoteTrue() {
        return repository.countByRemoteTrue();
    }

    @Override
    public long countByStatus(ApplicationStatus status) {
        return repository.countByStatus(status);
    }
}

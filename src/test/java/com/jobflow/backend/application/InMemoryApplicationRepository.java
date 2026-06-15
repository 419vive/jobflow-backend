package com.jobflow.backend.application;

import com.jobflow.backend.domain.ApplicationStatus;
import com.jobflow.backend.domain.JobApplication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

class InMemoryApplicationRepository implements ApplicationRepository {

    private final Map<UUID, JobApplication> applications = new LinkedHashMap<>();

    @Override
    public JobApplication save(JobApplication application) {
        applications.put(application.getId(), application);
        return application;
    }

    @Override
    public Optional<JobApplication> findById(UUID id) {
        return Optional.ofNullable(applications.get(id));
    }

    @Override
    public Optional<JobApplication> findByIdempotencyKey(String idempotencyKey) {
        return applications.values().stream()
                .filter(application -> idempotencyKey.equals(application.getIdempotencyKey()))
                .findFirst();
    }

    @Override
    public Page<JobApplication> search(ApplicationStatus status, Boolean remote, Pageable pageable) {
        List<JobApplication> filtered = applications.values().stream()
                .filter(application -> status == null || application.getStatus() == status)
                .filter(application -> remote == null || application.isRemote() == remote)
                .collect(Collectors.toList());
        return new PageImpl<>(filtered, pageable, filtered.size());
    }

    @Override
    public long count() {
        return applications.size();
    }

    @Override
    public long countByRemoteTrue() {
        return applications.values().stream().filter(JobApplication::isRemote).count();
    }

    @Override
    public long countByStatus(ApplicationStatus status) {
        return applications.values().stream()
                .filter(application -> application.getStatus() == status)
                .count();
    }
}

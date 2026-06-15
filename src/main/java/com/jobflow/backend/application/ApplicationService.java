package com.jobflow.backend.application;

import com.jobflow.backend.domain.ApplicationStatus;
import com.jobflow.backend.domain.JobApplication;
import com.jobflow.backend.web.CreateApplicationRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ApplicationService {

    private final ApplicationRepository repository;
    private final ApplicationStatusPolicy statusPolicy;
    private final ApplicationSummaryCache summaryCache;

    public ApplicationService(
            ApplicationRepository repository,
            ApplicationStatusPolicy statusPolicy,
            ApplicationSummaryCache summaryCache
    ) {
        this.repository = repository;
        this.statusPolicy = statusPolicy;
        this.summaryCache = summaryCache;
    }

    @Transactional
    public ApplicationView create(CreateApplicationRequest request, String idempotencyKey) {
        String normalizedKey = JobApplication.normalizeIdempotencyKey(idempotencyKey);
        if (normalizedKey != null) {
            return repository.findByIdempotencyKey(normalizedKey)
                    .map(ApplicationView::from)
                    .orElseGet(() -> createNew(request, normalizedKey));
        }
        return createNew(request, null);
    }

    @Transactional
    public ApplicationView changeStatus(UUID id, ApplicationStatus nextStatus) {
        JobApplication application = repository.findById(id)
                .orElseThrow(() -> new ApplicationNotFoundException(id));

        statusPolicy.requireAllowed(application.getStatus(), nextStatus);
        application.changeStatus(nextStatus);
        JobApplication saved = repository.save(application);
        summaryCache.evict();
        return ApplicationView.from(saved);
    }

    @Transactional(readOnly = true)
    public Page<ApplicationView> search(ApplicationStatus status, Boolean remote, Pageable pageable) {
        return repository.search(status, remote, pageable).map(ApplicationView::from);
    }

    @Transactional(readOnly = true)
    public ApplicationView get(UUID id) {
        return repository.findById(id)
                .map(ApplicationView::from)
                .orElseThrow(() -> new ApplicationNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public ApplicationSummary summary() {
        return summaryCache.get().orElseGet(() -> {
            ApplicationSummary summary = new ApplicationSummary(
                    repository.count(),
                    repository.countByRemoteTrue(),
                    repository.countByStatus(ApplicationStatus.SOURCED),
                    repository.countByStatus(ApplicationStatus.SUBMITTED),
                    repository.countByStatus(ApplicationStatus.INTERVIEWING),
                    repository.countByStatus(ApplicationStatus.OFFER)
            );
            summaryCache.put(summary);
            return summary;
        });
    }

    private ApplicationView createNew(CreateApplicationRequest request, String idempotencyKey) {
        JobApplication application = JobApplication.create(
                request.getCompany(),
                request.getTitle(),
                request.getLocation(),
                request.isRemote(),
                request.getNotes(),
                idempotencyKey
        );
        JobApplication saved = repository.save(application);
        summaryCache.evict();
        return ApplicationView.from(saved);
    }
}

package com.jobflow.backend.application;

import com.jobflow.backend.domain.ApplicationStatus;
import com.jobflow.backend.domain.JobApplication;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionOperations;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.UUID;

@Service
public class ApplicationService {

    private final ApplicationRepository repository;
    private final ApplicationStatusPolicy statusPolicy;
    private final ApplicationSummaryCache summaryCache;
    private final TransactionOperations transactions;

    public ApplicationService(
            ApplicationRepository repository,
            ApplicationStatusPolicy statusPolicy,
            ApplicationSummaryCache summaryCache,
            TransactionOperations transactions
    ) {
        this.repository = repository;
        this.statusPolicy = statusPolicy;
        this.summaryCache = summaryCache;
        this.transactions = transactions;
    }

    public ApplicationCreateResult create(CreateApplicationCommand command, String idempotencyKey) {
        String normalizedKey = JobApplication.normalizeIdempotencyKey(idempotencyKey);
        if (normalizedKey != null) {
            ApplicationCreateResult existing = transactions.execute(status ->
                    repository.findByIdempotencyKey(normalizedKey)
                            .map(ApplicationView::from)
                            .map(ApplicationCreateResult::reused)
                            .orElse(null)
            );
            if (existing != null) {
                return existing;
            }
        }

        try {
            return transactions.execute(status -> createNew(command, normalizedKey));
        } catch (DataIntegrityViolationException ex) {
            if (normalizedKey == null) {
                throw ex;
            }
            return transactions.execute(status ->
                    repository.findByIdempotencyKey(normalizedKey)
                            .map(ApplicationView::from)
                            .map(ApplicationCreateResult::reused)
                            .orElseThrow(() -> ex)
            );
        }
    }

    @Transactional
    public ApplicationView changeStatus(UUID id, ApplicationStatus nextStatus) {
        JobApplication application = repository.findById(id)
                .orElseThrow(() -> new ApplicationNotFoundException(id));

        statusPolicy.requireAllowed(application.getStatus(), nextStatus);
        application.changeStatus(nextStatus);
        JobApplication saved = repository.save(application);
        evictSummaryAfterCommit();
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

    private ApplicationCreateResult createNew(CreateApplicationCommand command, String idempotencyKey) {
        JobApplication application = JobApplication.create(
                command.getCompany(),
                command.getTitle(),
                command.getLocation(),
                command.isRemote(),
                command.getNotes(),
                idempotencyKey
        );
        JobApplication saved = repository.save(application);
        evictSummaryAfterCommit();
        return ApplicationCreateResult.created(ApplicationView.from(saved));
    }

    private void evictSummaryAfterCommit() {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            summaryCache.evict();
            return;
        }

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                summaryCache.evict();
            }
        });
    }
}

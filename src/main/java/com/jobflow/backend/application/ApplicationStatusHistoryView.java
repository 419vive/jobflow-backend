package com.jobflow.backend.application;

import com.jobflow.backend.domain.ApplicationStatus;
import com.jobflow.backend.domain.ApplicationStatusHistory;

import java.time.Instant;
import java.util.UUID;

public class ApplicationStatusHistoryView {

    private final UUID id;
    private final UUID applicationId;
    private final ApplicationStatus fromStatus;
    private final ApplicationStatus toStatus;
    private final String reason;
    private final Instant changedAt;

    private ApplicationStatusHistoryView(ApplicationStatusHistory history) {
        this.id = history.getId();
        this.applicationId = history.getApplicationId();
        this.fromStatus = history.getFromStatus();
        this.toStatus = history.getToStatus();
        this.reason = history.getReason();
        this.changedAt = history.getChangedAt();
    }

    public static ApplicationStatusHistoryView from(ApplicationStatusHistory history) {
        return new ApplicationStatusHistoryView(history);
    }

    public UUID getId() {
        return id;
    }

    public UUID getApplicationId() {
        return applicationId;
    }

    public ApplicationStatus getFromStatus() {
        return fromStatus;
    }

    public ApplicationStatus getToStatus() {
        return toStatus;
    }

    public String getReason() {
        return reason;
    }

    public Instant getChangedAt() {
        return changedAt;
    }
}

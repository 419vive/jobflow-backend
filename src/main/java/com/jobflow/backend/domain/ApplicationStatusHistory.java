package com.jobflow.backend.domain;

import org.hibernate.annotations.Type;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "application_status_history")
public class ApplicationStatusHistory {

    @Id
    @Type(type = "uuid-char")
    @Column(nullable = false, updatable = false, length = 36)
    private UUID id;

    @Type(type = "uuid-char")
    @Column(name = "application_id", nullable = false, updatable = false, length = 36)
    private UUID applicationId;

    @Enumerated(EnumType.STRING)
    @Column(name = "from_status", length = 32)
    private ApplicationStatus fromStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "to_status", nullable = false, length = 32)
    private ApplicationStatus toStatus;

    @Column(length = 500)
    private String reason;

    @Column(name = "changed_at", nullable = false, updatable = false)
    private Instant changedAt;

    protected ApplicationStatusHistory() {
    }

    private ApplicationStatusHistory(
            UUID id,
            UUID applicationId,
            ApplicationStatus fromStatus,
            ApplicationStatus toStatus,
            String reason,
            Instant changedAt
    ) {
        this.id = id;
        this.applicationId = applicationId;
        this.fromStatus = fromStatus;
        this.toStatus = toStatus;
        this.reason = normalizeReason(reason);
        this.changedAt = changedAt;
    }

    public static ApplicationStatusHistory created(UUID applicationId) {
        return new ApplicationStatusHistory(
                UUID.randomUUID(),
                applicationId,
                null,
                ApplicationStatus.SOURCED,
                null,
                Instant.now()
        );
    }

    public static ApplicationStatusHistory transition(
            UUID applicationId,
            ApplicationStatus fromStatus,
            ApplicationStatus toStatus,
            String reason
    ) {
        return new ApplicationStatusHistory(
                UUID.randomUUID(),
                applicationId,
                fromStatus,
                toStatus,
                reason,
                Instant.now()
        );
    }

    private static String normalizeReason(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
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

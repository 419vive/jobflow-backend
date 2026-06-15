package com.jobflow.backend.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import org.hibernate.annotations.Type;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "job_applications",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_job_applications_idempotency_key", columnNames = "idempotency_key")
        }
)
public class JobApplication {

    @Id
    @Type(type = "uuid-char")
    @Column(nullable = false, updatable = false, length = 36)
    private UUID id;

    @Column(nullable = false, length = 120)
    private String company;

    @Column(nullable = false, length = 160)
    private String title;

    @Column(nullable = false, length = 120)
    private String location;

    @Column(nullable = false)
    private boolean remote;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private ApplicationStatus status;

    @Column(length = 1000)
    private String notes;

    @Column(name = "idempotency_key", unique = true, length = 120)
    private String idempotencyKey;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    protected JobApplication() {
    }

    private JobApplication(
            UUID id,
            String company,
            String title,
            String location,
            boolean remote,
            String notes,
            String idempotencyKey
    ) {
        this.id = id;
        this.company = company;
        this.title = title;
        this.location = location;
        this.remote = remote;
        this.status = ApplicationStatus.SOURCED;
        this.notes = notes;
        this.idempotencyKey = idempotencyKey;
    }

    public static JobApplication create(
            String company,
            String title,
            String location,
            boolean remote,
            String notes,
            String idempotencyKey
    ) {
        return new JobApplication(
                UUID.randomUUID(),
                company.trim(),
                title.trim(),
                location.trim(),
                remote,
                notes == null ? null : notes.trim(),
                normalizeIdempotencyKey(idempotencyKey)
        );
    }

    public void changeStatus(ApplicationStatus nextStatus) {
        this.status = nextStatus;
    }

    @PrePersist
    void prePersist() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    void preUpdate() {
        this.updatedAt = Instant.now();
    }

    public static String normalizeIdempotencyKey(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }

    public UUID getId() {
        return id;
    }

    public String getCompany() {
        return company;
    }

    public String getTitle() {
        return title;
    }

    public String getLocation() {
        return location;
    }

    public boolean isRemote() {
        return remote;
    }

    public ApplicationStatus getStatus() {
        return status;
    }

    public String getNotes() {
        return notes;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}

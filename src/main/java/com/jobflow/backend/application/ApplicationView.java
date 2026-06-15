package com.jobflow.backend.application;

import com.jobflow.backend.domain.ApplicationStatus;
import com.jobflow.backend.domain.JobApplication;

import java.time.Instant;
import java.util.UUID;

public class ApplicationView {

    private final UUID id;
    private final String company;
    private final String title;
    private final String location;
    private final boolean remote;
    private final ApplicationStatus status;
    private final String notes;
    private final Instant createdAt;
    private final Instant updatedAt;

    private ApplicationView(JobApplication application) {
        this.id = application.getId();
        this.company = application.getCompany();
        this.title = application.getTitle();
        this.location = application.getLocation();
        this.remote = application.isRemote();
        this.status = application.getStatus();
        this.notes = application.getNotes();
        this.createdAt = application.getCreatedAt();
        this.updatedAt = application.getUpdatedAt();
    }

    public static ApplicationView from(JobApplication application) {
        return new ApplicationView(application);
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

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}

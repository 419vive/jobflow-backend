package com.jobflow.backend.web;

import com.jobflow.backend.domain.ApplicationStatus;

import javax.validation.constraints.NotNull;

public class ChangeStatusRequest {

    @NotNull
    private ApplicationStatus status;

    public ChangeStatusRequest() {
    }

    public ChangeStatusRequest(ApplicationStatus status) {
        this.status = status;
    }

    public ApplicationStatus getStatus() {
        return status;
    }

    public void setStatus(ApplicationStatus status) {
        this.status = status;
    }
}

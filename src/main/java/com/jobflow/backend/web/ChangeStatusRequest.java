package com.jobflow.backend.web;

import com.jobflow.backend.domain.ApplicationStatus;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class ChangeStatusRequest {

    @NotNull
    private ApplicationStatus status;

    @Size(max = 500)
    private String reason;

    public ChangeStatusRequest() {
    }

    public ChangeStatusRequest(ApplicationStatus status) {
        this.status = status;
    }

    public ChangeStatusRequest(ApplicationStatus status, String reason) {
        this.status = status;
        this.reason = reason;
    }

    public ApplicationStatus getStatus() {
        return status;
    }

    public void setStatus(ApplicationStatus status) {
        this.status = status;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}

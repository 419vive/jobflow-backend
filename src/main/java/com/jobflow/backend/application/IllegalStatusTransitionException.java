package com.jobflow.backend.application;

import com.jobflow.backend.domain.ApplicationStatus;

public class IllegalStatusTransitionException extends RuntimeException {

    public IllegalStatusTransitionException(ApplicationStatus current, ApplicationStatus next) {
        super("Illegal application status transition: " + current + " -> " + next);
    }
}

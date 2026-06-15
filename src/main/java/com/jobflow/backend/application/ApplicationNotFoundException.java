package com.jobflow.backend.application;

import java.util.UUID;

public class ApplicationNotFoundException extends RuntimeException {

    public ApplicationNotFoundException(UUID id) {
        super("Application not found: " + id);
    }
}

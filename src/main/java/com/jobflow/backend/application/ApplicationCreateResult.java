package com.jobflow.backend.application;

public class ApplicationCreateResult {

    private final ApplicationView application;
    private final boolean created;

    private ApplicationCreateResult(ApplicationView application, boolean created) {
        this.application = application;
        this.created = created;
    }

    public static ApplicationCreateResult created(ApplicationView application) {
        return new ApplicationCreateResult(application, true);
    }

    public static ApplicationCreateResult reused(ApplicationView application) {
        return new ApplicationCreateResult(application, false);
    }

    public ApplicationView getApplication() {
        return application;
    }

    public boolean isCreated() {
        return created;
    }
}

package com.jobflow.backend.application;

import java.util.Optional;

public interface ApplicationSummaryCache {

    Optional<ApplicationSummary> get();

    void put(ApplicationSummary summary);

    void evict();
}

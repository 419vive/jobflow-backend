package com.jobflow.backend.application;

import java.util.Optional;

class InMemorySummaryCache implements ApplicationSummaryCache {

    private ApplicationSummary summary;
    private boolean evicted;

    @Override
    public Optional<ApplicationSummary> get() {
        return Optional.ofNullable(summary);
    }

    @Override
    public void put(ApplicationSummary summary) {
        this.summary = summary;
        this.evicted = false;
    }

    @Override
    public void evict() {
        this.summary = null;
        this.evicted = true;
    }

    boolean wasEvicted() {
        return evicted;
    }
}

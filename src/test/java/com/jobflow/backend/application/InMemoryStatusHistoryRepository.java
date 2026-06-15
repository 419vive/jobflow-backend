package com.jobflow.backend.application;

import com.jobflow.backend.domain.ApplicationStatusHistory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

class InMemoryStatusHistoryRepository implements ApplicationStatusHistoryRepository {

    private final List<ApplicationStatusHistory> history = new ArrayList<>();

    @Override
    public ApplicationStatusHistory save(ApplicationStatusHistory entry) {
        history.add(entry);
        return entry;
    }

    @Override
    public List<ApplicationStatusHistory> findByApplicationId(UUID applicationId) {
        return history.stream()
                .filter(entry -> entry.getApplicationId().equals(applicationId))
                .sorted(Comparator.comparing(ApplicationStatusHistory::getChangedAt))
                .collect(Collectors.toList());
    }
}

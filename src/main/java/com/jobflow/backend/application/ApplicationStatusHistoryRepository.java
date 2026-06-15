package com.jobflow.backend.application;

import com.jobflow.backend.domain.ApplicationStatusHistory;

import java.util.List;
import java.util.UUID;

public interface ApplicationStatusHistoryRepository {

    ApplicationStatusHistory save(ApplicationStatusHistory entry);

    List<ApplicationStatusHistory> findByApplicationId(UUID applicationId);
}

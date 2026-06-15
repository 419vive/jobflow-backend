package com.jobflow.backend.infrastructure;

import com.jobflow.backend.application.ApplicationStatusHistoryRepository;
import com.jobflow.backend.domain.ApplicationStatusHistory;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public class JpaApplicationStatusHistoryStore implements ApplicationStatusHistoryRepository {

    private final JpaApplicationStatusHistoryJpaRepository repository;

    public JpaApplicationStatusHistoryStore(JpaApplicationStatusHistoryJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public ApplicationStatusHistory save(ApplicationStatusHistory entry) {
        return repository.save(entry);
    }

    @Override
    public List<ApplicationStatusHistory> findByApplicationId(UUID applicationId) {
        return repository.findByApplicationIdOrderByChangedAtAsc(applicationId);
    }
}

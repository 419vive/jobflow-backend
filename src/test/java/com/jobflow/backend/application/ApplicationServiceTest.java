package com.jobflow.backend.application;

import com.jobflow.backend.domain.ApplicationStatus;
import com.jobflow.backend.domain.JobApplication;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.support.SimpleTransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionOperations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ApplicationServiceTest {

    private static final TransactionOperations WITHOUT_TRANSACTION = new TransactionOperations() {
        @Override
        public <T> T execute(TransactionCallback<T> callback) {
            return callback.doInTransaction(new SimpleTransactionStatus());
        }
    };

    private final InMemoryApplicationRepository repository = new InMemoryApplicationRepository();
    private final InMemorySummaryCache cache = new InMemorySummaryCache();
    private final ApplicationService service = new ApplicationService(
            repository,
            new ApplicationStatusPolicy(),
            cache,
            WITHOUT_TRANSACTION
    );

    @Test
    void createsRemoteApplicationAndInvalidatesSummaryCache() {
        cache.put(new ApplicationSummary(99L, 99L, 99L, 99L, 99L, 99L));

        ApplicationCreateResult result = service.create(new CreateApplicationCommand(
                "Bruce HR",
                "Java Backend Engineer",
                "Taiwan",
                true,
                "Spring, Redis, SQL"
        ), "jd-2026-06-15");

        ApplicationView created = result.getApplication();

        assertThat(result.isCreated()).isTrue();
        assertThat(created.getCompany()).isEqualTo("Bruce HR");
        assertThat(created.getStatus()).isEqualTo(ApplicationStatus.SOURCED);
        assertThat(created.isRemote()).isTrue();
        assertThat(created.getCreatedAt()).isNotNull();
        assertThat(created.getUpdatedAt()).isNotNull();
        assertThat(cache.wasEvicted()).isTrue();
    }

    @Test
    void reusesExistingApplicationWhenIdempotencyKeyIsRepeated() {
        CreateApplicationCommand request = new CreateApplicationCommand(
                "Bruce HR",
                "Java Backend Engineer",
                "Taiwan",
                true,
                "First request"
        );

        ApplicationCreateResult first = service.create(request, "same-key");
        ApplicationCreateResult second = service.create(new CreateApplicationCommand(
                "Different Company",
                "Different Title",
                "Remote",
                true,
                "Should not create a duplicate"
        ), "same-key");

        assertThat(first.isCreated()).isTrue();
        assertThat(second.isCreated()).isFalse();
        assertThat(second.getApplication().getId()).isEqualTo(first.getApplication().getId());
        assertThat(repository.count()).isEqualTo(1);
    }

    @Test
    void reloadsExistingApplicationWhenConcurrentCreateHitsUniqueIdempotencyConstraint() {
        RaceLosingRepository repository = new RaceLosingRepository("same-key");
        ApplicationService service = new ApplicationService(
                repository,
                new ApplicationStatusPolicy(),
                new InMemorySummaryCache(),
                WITHOUT_TRANSACTION
        );

        ApplicationCreateResult result = service.create(new CreateApplicationCommand(
                "Race Loser",
                "Java Backend Engineer",
                "Remote",
                true,
                "Concurrent request"
        ), "same-key");

        assertThat(result.isCreated()).isFalse();
        assertThat(result.getApplication().getCompany()).isEqualTo("Race Winner");
        assertThat(repository.count()).isEqualTo(1);
    }

    @Test
    void rejectsInvalidStatusTransition() {
        ApplicationView created = service.create(new CreateApplicationCommand(
                "Bruce HR",
                "Java Backend Engineer",
                "Taiwan",
                true,
                "Pipeline"
        ), null).getApplication();

        assertThatThrownBy(() -> service.changeStatus(created.getId(), ApplicationStatus.OFFER))
                .isInstanceOf(IllegalStatusTransitionException.class);
    }

    private static class RaceLosingRepository extends InMemoryApplicationRepository {

        private final String idempotencyKey;
        private final JobApplication existing;
        private boolean duplicateVisible;

        private RaceLosingRepository(String idempotencyKey) {
            this.idempotencyKey = idempotencyKey;
            this.existing = JobApplication.create(
                    "Race Winner",
                    "Java Backend Engineer",
                    "Remote",
                    true,
                    "Already persisted",
                    idempotencyKey
            );
        }

        @Override
        public JobApplication save(JobApplication application) {
            duplicateVisible = true;
            super.save(existing);
            throw new DataIntegrityViolationException("duplicate idempotency key");
        }

        @Override
        public java.util.Optional<JobApplication> findByIdempotencyKey(String idempotencyKey) {
            if (duplicateVisible && this.idempotencyKey.equals(idempotencyKey)) {
                return java.util.Optional.of(existing);
            }
            return java.util.Optional.empty();
        }
    }
}

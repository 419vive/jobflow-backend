package com.jobflow.backend.application;

import com.jobflow.backend.domain.ApplicationStatus;
import com.jobflow.backend.web.CreateApplicationRequest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ApplicationServiceTest {

    private final InMemoryApplicationRepository repository = new InMemoryApplicationRepository();
    private final InMemorySummaryCache cache = new InMemorySummaryCache();
    private final ApplicationService service = new ApplicationService(
            repository,
            new ApplicationStatusPolicy(),
            cache
    );

    @Test
    void createsRemoteApplicationAndInvalidatesSummaryCache() {
        cache.put(new ApplicationSummary(99L, 99L, 99L, 99L, 99L, 99L));

        ApplicationView created = service.create(new CreateApplicationRequest(
                "Bruce HR",
                "Java Backend Engineer",
                "Taiwan",
                true,
                "Spring, Redis, SQL"
        ), "jd-2026-06-15");

        assertThat(created.getCompany()).isEqualTo("Bruce HR");
        assertThat(created.getStatus()).isEqualTo(ApplicationStatus.SOURCED);
        assertThat(created.isRemote()).isTrue();
        assertThat(cache.wasEvicted()).isTrue();
    }

    @Test
    void reusesExistingApplicationWhenIdempotencyKeyIsRepeated() {
        CreateApplicationRequest request = new CreateApplicationRequest(
                "Bruce HR",
                "Java Backend Engineer",
                "Taiwan",
                true,
                "First request"
        );

        ApplicationView first = service.create(request, "same-key");
        ApplicationView second = service.create(new CreateApplicationRequest(
                "Different Company",
                "Different Title",
                "Remote",
                true,
                "Should not create a duplicate"
        ), "same-key");

        assertThat(second.getId()).isEqualTo(first.getId());
        assertThat(repository.count()).isEqualTo(1);
    }

    @Test
    void rejectsInvalidStatusTransition() {
        ApplicationView created = service.create(new CreateApplicationRequest(
                "Bruce HR",
                "Java Backend Engineer",
                "Taiwan",
                true,
                "Pipeline"
        ), null);

        assertThatThrownBy(() -> service.changeStatus(created.getId(), ApplicationStatus.OFFER))
                .isInstanceOf(IllegalStatusTransitionException.class);
    }
}

package com.jobflow.backend.domain;

import org.junit.jupiter.api.Test;

import javax.persistence.Version;
import java.lang.reflect.Field;
import java.time.Instant;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

class JobApplicationTest {

    @Test
    void ownsTimestampsOutsideJpaLifecycleCallbacks() {
        JobApplication application = JobApplication.create(
                "Bruce HR",
                "Java Backend Engineer",
                "Remote",
                true,
                "Spring, Redis, SQL",
                "same-key"
        );

        assertThat(application.getCreatedAt()).isNotNull();
        assertThat(application.getUpdatedAt()).isNotNull();
        assertThat(application.getUpdatedAt()).isAfterOrEqualTo(application.getCreatedAt());

        Instant beforeStatusChange = application.getUpdatedAt();
        application.changeStatus(ApplicationStatus.SUBMITTED);

        assertThat(application.getUpdatedAt()).isAfterOrEqualTo(beforeStatusChange);
    }

    @Test
    void declaresOptimisticLockVersion() {
        boolean hasVersionField = Arrays.stream(JobApplication.class.getDeclaredFields())
                .map(Field::getAnnotations)
                .flatMap(Arrays::stream)
                .anyMatch(annotation -> annotation.annotationType().equals(Version.class));

        assertThat(hasVersionField).isTrue();
    }
}

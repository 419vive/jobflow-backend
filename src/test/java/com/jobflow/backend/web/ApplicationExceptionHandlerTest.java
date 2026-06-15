package com.jobflow.backend.web;

import com.jobflow.backend.domain.JobApplication;
import org.junit.jupiter.api.Test;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ApplicationExceptionHandlerTest {

    private final ApplicationExceptionHandler handler = new ApplicationExceptionHandler();

    @Test
    void returnsConflictForOptimisticLockFailure() {
        ApiError error = handler.optimisticLock(new ObjectOptimisticLockingFailureException(
                JobApplication.class,
                UUID.randomUUID()
        )).getBody();

        assertThat(error).isNotNull();
        assertThat(error.getError()).isEqualTo("write_conflict");
    }
}

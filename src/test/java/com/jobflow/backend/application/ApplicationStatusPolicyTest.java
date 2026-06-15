package com.jobflow.backend.application;

import com.jobflow.backend.domain.ApplicationStatus;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ApplicationStatusPolicyTest {

    private final ApplicationStatusPolicy policy = new ApplicationStatusPolicy();

    @Test
    void allowsForwardProgressThroughARealHiringPipeline() {
        assertThat(policy.canMove(ApplicationStatus.SOURCED, ApplicationStatus.APPLIED)).isTrue();
        assertThat(policy.canMove(ApplicationStatus.APPLIED, ApplicationStatus.INTERVIEWING)).isTrue();
        assertThat(policy.canMove(ApplicationStatus.INTERVIEWING, ApplicationStatus.OFFER)).isTrue();
    }

    @Test
    void rejectsBackwardStatusChangesThatWouldCorruptTheAuditTrail() {
        assertThat(policy.canMove(ApplicationStatus.SUBMITTED, ApplicationStatus.SOURCED)).isFalse();

        assertThatThrownBy(() -> policy.requireAllowed(ApplicationStatus.SUBMITTED, ApplicationStatus.SOURCED))
                .isInstanceOf(IllegalStatusTransitionException.class)
                .hasMessageContaining("SUBMITTED -> SOURCED");
    }
}

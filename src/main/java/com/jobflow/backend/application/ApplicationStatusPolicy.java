package com.jobflow.backend.application;

import com.jobflow.backend.domain.ApplicationStatus;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

public class ApplicationStatusPolicy {

    private final Map<ApplicationStatus, Set<ApplicationStatus>> allowed = new EnumMap<>(ApplicationStatus.class);

    public ApplicationStatusPolicy() {
        allowed.put(ApplicationStatus.SOURCED, EnumSet.of(ApplicationStatus.APPLIED, ApplicationStatus.SUBMITTED, ApplicationStatus.REJECTED, ApplicationStatus.WITHDRAWN));
        allowed.put(ApplicationStatus.APPLIED, EnumSet.of(ApplicationStatus.SUBMITTED, ApplicationStatus.INTERVIEWING, ApplicationStatus.REJECTED, ApplicationStatus.WITHDRAWN));
        allowed.put(ApplicationStatus.SUBMITTED, EnumSet.of(ApplicationStatus.INTERVIEWING, ApplicationStatus.REJECTED, ApplicationStatus.WITHDRAWN));
        allowed.put(ApplicationStatus.INTERVIEWING, EnumSet.of(ApplicationStatus.OFFER, ApplicationStatus.REJECTED, ApplicationStatus.WITHDRAWN));
        allowed.put(ApplicationStatus.OFFER, EnumSet.of(ApplicationStatus.REJECTED, ApplicationStatus.WITHDRAWN));
        allowed.put(ApplicationStatus.REJECTED, EnumSet.noneOf(ApplicationStatus.class));
        allowed.put(ApplicationStatus.WITHDRAWN, EnumSet.noneOf(ApplicationStatus.class));
    }

    public boolean canMove(ApplicationStatus current, ApplicationStatus next) {
        if (current == next) {
            return true;
        }
        return allowed.getOrDefault(current, EnumSet.noneOf(ApplicationStatus.class)).contains(next);
    }

    public void requireAllowed(ApplicationStatus current, ApplicationStatus next) {
        if (!canMove(current, next)) {
            throw new IllegalStatusTransitionException(current, next);
        }
    }
}

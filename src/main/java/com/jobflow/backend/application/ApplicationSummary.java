package com.jobflow.backend.application;

public class ApplicationSummary {

    private long total;
    private long remote;
    private long sourced;
    private long submitted;
    private long interviewing;
    private long offers;

    public ApplicationSummary() {
    }

    public ApplicationSummary(long total, long remote, long sourced, long submitted, long interviewing, long offers) {
        this.total = total;
        this.remote = remote;
        this.sourced = sourced;
        this.submitted = submitted;
        this.interviewing = interviewing;
        this.offers = offers;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public long getRemote() {
        return remote;
    }

    public void setRemote(long remote) {
        this.remote = remote;
    }

    public long getSourced() {
        return sourced;
    }

    public void setSourced(long sourced) {
        this.sourced = sourced;
    }

    public long getSubmitted() {
        return submitted;
    }

    public void setSubmitted(long submitted) {
        this.submitted = submitted;
    }

    public long getInterviewing() {
        return interviewing;
    }

    public void setInterviewing(long interviewing) {
        this.interviewing = interviewing;
    }

    public long getOffers() {
        return offers;
    }

    public void setOffers(long offers) {
        this.offers = offers;
    }
}

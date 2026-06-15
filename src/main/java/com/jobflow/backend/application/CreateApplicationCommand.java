package com.jobflow.backend.application;

public class CreateApplicationCommand {

    private final String company;
    private final String title;
    private final String location;
    private final boolean remote;
    private final String notes;

    public CreateApplicationCommand(String company, String title, String location, boolean remote, String notes) {
        this.company = company;
        this.title = title;
        this.location = location;
        this.remote = remote;
        this.notes = notes;
    }

    public String getCompany() {
        return company;
    }

    public String getTitle() {
        return title;
    }

    public String getLocation() {
        return location;
    }

    public boolean isRemote() {
        return remote;
    }

    public String getNotes() {
        return notes;
    }
}

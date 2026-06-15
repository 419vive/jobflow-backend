package com.jobflow.backend.web;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public class CreateApplicationRequest {

    @NotBlank
    @Size(max = 120)
    private String company;

    @NotBlank
    @Size(max = 160)
    private String title;

    @NotBlank
    @Size(max = 120)
    private String location;

    private boolean remote;

    @Size(max = 1000)
    private String notes;

    public CreateApplicationRequest() {
    }

    public CreateApplicationRequest(String company, String title, String location, boolean remote, String notes) {
        this.company = company;
        this.title = title;
        this.location = location;
        this.remote = remote;
        this.notes = notes;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public boolean isRemote() {
        return remote;
    }

    public void setRemote(boolean remote) {
        this.remote = remote;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}

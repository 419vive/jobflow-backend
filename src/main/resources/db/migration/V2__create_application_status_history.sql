CREATE TABLE application_status_history (
    id VARCHAR(36) PRIMARY KEY,
    application_id VARCHAR(36) NOT NULL,
    from_status VARCHAR(32),
    to_status VARCHAR(32) NOT NULL,
    reason VARCHAR(500),
    changed_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_application_status_history_application
        FOREIGN KEY (application_id) REFERENCES job_applications(id)
);

CREATE INDEX idx_application_status_history_application_changed_at
    ON application_status_history(application_id, changed_at);

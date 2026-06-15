CREATE TABLE job_applications (
    id VARCHAR(36) PRIMARY KEY,
    company VARCHAR(120) NOT NULL,
    title VARCHAR(160) NOT NULL,
    location VARCHAR(120) NOT NULL,
    remote BOOLEAN NOT NULL,
    status VARCHAR(32) NOT NULL,
    notes VARCHAR(1000),
    idempotency_key VARCHAR(120),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT uk_job_applications_idempotency_key UNIQUE (idempotency_key)
);

CREATE INDEX idx_job_applications_status ON job_applications(status);
CREATE INDEX idx_job_applications_remote ON job_applications(remote);

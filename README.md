# JobFlow Backend

Java Spring Boot backend API for tracking remote job applications and hiring pipeline status.

This project was built as a practical backend portfolio piece for a Java backend role requiring API development, tests, Redis, SQL, deployment awareness, and code review readiness.

## What It Demonstrates

| JD requirement | Project proof |
| --- | --- |
| Java and Spring | Spring Boot REST API with service, domain, repository, and controller layers |
| Redis | `RedisApplicationSummaryCache` caches dashboard summary data with TTL and graceful fallback |
| SQL | Flyway migration creates the `job_applications` table, indexes, optimistic-lock version, and idempotency constraint |
| API development | CRUD-style application tracking endpoints plus status transitions and filtering |
| Testing | Unit tests, MockMvc API tests, and Testcontainers coverage for Postgres plus Redis |
| Deployment | Multi-stage Dockerfile, health-checked Docker Compose, and GitHub Actions CI image build |
| Code review | Layered commands/DTOs, validation, error responses, concurrency-safe idempotency handling |

## API Endpoints

| Method | Path | Purpose |
| --- | --- | --- |
| `POST` | `/api/applications` | Create a tracked job application |
| `GET` | `/api/applications` | List applications, optionally filtered by `status` or `remote` |
| `GET` | `/api/applications/{id}` | Fetch one application |
| `PATCH` | `/api/applications/{id}/status` | Move an application through the hiring pipeline |
| `GET` | `/api/applications/summary` | Get cached pipeline counts |
| `GET` | `/docs` | Swagger UI |

## Status Flow

```text
SOURCED -> APPLIED -> SUBMITTED -> INTERVIEWING -> OFFER
       \          \             \                \-> REJECTED/WITHDRAWN
        \          \-> INTERVIEWING
         \-> SUBMITTED
```

Backward moves are rejected with `409 Conflict` so the audit trail stays trustworthy.

## Production Hardening

- Repeated `POST /api/applications` requests with the same `Idempotency-Key` return the same application. A true insert returns `201 Created`; a replay returns `200 OK`.
- Duplicate idempotency races are handled by the database unique constraint, a flushed write, and a retry read of the winning row.
- Status updates use optimistic locking through a JPA `@Version` column.
- Summary-cache eviction is registered after commit so rolled-back writes do not invalidate Redis.
- Redis failures are logged and fall back to the SQL database as the source of truth.
- Default listing order is newest-first for stable review and paging behavior.

## Run Locally

Requirements:

- Java 11
- Docker, optional for Postgres and Redis

Run tests:

```bash
./mvnw test
```

Package:

```bash
./mvnw package
```

Start with the default H2 database:

```bash
./mvnw spring-boot:run
```

Start with Postgres and Redis:

```bash
./mvnw package
docker compose up --build
```

The Testcontainers integration test is skipped when Docker is not running locally; GitHub Actions runs it on a Docker-capable runner.

## Example Requests

Create an application:

```bash
curl -X POST http://localhost:8080/api/applications \
  -H "Content-Type: application/json" \
  -H "Idempotency-Key: bruce-java-2026-06-15" \
  -d '{
    "company": "Bruce HR",
    "title": "Java Backend Engineer",
    "location": "Remote",
    "remote": true,
    "notes": "Spring, Redis, SQL, API testing"
  }'
```

List remote applications:

```bash
curl "http://localhost:8080/api/applications?remote=true"
```

Change status:

```bash
curl -X PATCH http://localhost:8080/api/applications/{id}/status \
  -H "Content-Type: application/json" \
  -d '{"status":"SUBMITTED"}'
```

## Architecture

```text
web/            REST controllers, request DTOs, error responses
application/    use cases, status policy, repository/cache ports
domain/         JPA entity and application status model
infrastructure/ JPA adapter and Redis cache adapter
db/migration/   Flyway SQL migration
```

The database is authoritative. Redis is used as a fast cache for summary counts and is allowed to fail without blocking core writes.

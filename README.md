# JobFlow Backend

Java Spring Boot backend API for tracking remote job applications and hiring pipeline status.

This project was built as a practical backend portfolio piece for a Java backend role requiring API development, tests, Redis, SQL, deployment awareness, and code review readiness.

## What It Demonstrates

| JD requirement | Project proof |
| --- | --- |
| Java and Spring | Spring Boot REST API with service, domain, repository, and controller layers |
| Redis | `RedisApplicationSummaryCache` caches dashboard summary data with TTL and graceful fallback |
| SQL | Flyway migration creates the `job_applications` table, indexes, and idempotency constraint |
| API development | CRUD-style application tracking endpoints plus status transitions and filtering |
| Testing | Unit tests for status rules and service behavior, plus MockMvc API tests |
| Deployment | Dockerfile, Docker Compose for API/Postgres/Redis, and GitHub Actions CI |
| Code review | Clear package boundaries, validation, error responses, idempotency key support |

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

# Customer API

A small, production-minded REST API for managing customers. Built with Spring Boot 3 and Java 21 to demonstrate clear layering, validation, error handling, and automated tests—suitable for technical interview discussion.

## Prerequisites

- **JDK 21+**
- **Apache Maven 3.9+**
- An HTTP client that supports `.http` files (IntelliJ HTTP Client, VS Code REST Client, or similar) for the demo script

## Build and run

```bash
mvn clean package
mvn spring-boot:run
```

The API listens on `http://localhost:8080`.

## Run tests

```bash
mvn test
```

Tests use an in-memory store and MockMvc. They do not require a running server or external services.

## Demonstrate the API (`.http` file)

1. Start the application (`mvn spring-boot:run`).
2. Open `api-demo.http`.
3. Execute the requests in order.

The create request is named `createCustomer`. Later requests reuse `{{customerId}}` from that response (`@customerId = {{createCustomer.response.body.id}}`). If your client does not support response capture, copy the `id` from the create response into subsequent URLs manually.

## Endpoints

| Method | Path | Success | Notes |
|--------|------|---------|--------|
| `POST` | `/api/customers` | `201 Created` | Generates a UUID `id`. Duplicate email → `409 Conflict`. |
| `GET` | `/api/customers/{id}` | `200 OK` | Unknown id → `404 Not Found`. |
| `GET` | `/api/customers` | `200 OK` | Empty list returns `[]`. |
| `PUT` | `/api/customers/{id}` | `200 OK` | Full replacement of mutable fields. Unknown id → `404`. |
| `DELETE` | `/api/customers/{id}` | `204 No Content` | Unknown id → `404` (does not silently succeed). |

### Request body (create / update)

```json
{
  "firstName": "Ada",
  "lastName": "Lovelace",
  "email": "ada.lovelace@example.com",
  "status": "ACTIVE"
}
```

### Validation rules

- `firstName`, `lastName`, `email`: required, non-blank
- `email`: must be a valid email format; stored trimmed and lowercased
- `status`: required; one of `ACTIVE`, `INACTIVE`, `PENDING`
- Invalid or malformed JSON / unknown status → `400 Bad Request`
- Duplicate email (case-insensitive) → `409 Conflict`
- A customer may keep their own email on update

### Error response shape

```json
{
  "timestamp": "2026-10-24T07:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/customers",
  "details": ["firstName: firstName must not be blank"]
}
```

Unexpected failures return `500` with a generic message (no stack traces in the response body).

## Design decisions and assumptions

| Decision | Why | Alternatives | Trade-offs |
|----------|-----|--------------|------------|
| Spring Boot 3 + Maven | Widely used, interview-familiar, strong validation/testing support | Quarkus, Micronaut, plain Jakarta EE | Slightly heavier than a minimal HTTP library |
| Layered architecture (controller / service / repository) | Clear responsibilities without over-engineering | Hexagonal / CQRS | Extra classes vs a single controller for a tiny demo |
| In-memory `ConcurrentHashMap` repository | Fastest path to a working, testable CRUD API | H2 + Spring Data JPA | Data lost on restart; no SQL; fine for this scope |
| Separate request/response DTOs | Keeps API contract independent of the domain model | Expose entity directly | More mapping code; worth it for clarity |
| Case-insensitive unique email | Reasonable uniqueness policy for customer identity | Allow duplicates; case-sensitive uniqueness | Rejects `Ada@x.com` vs `ada@x.com` as the same address |
| UUID identifiers | No shared sequence / race issues in memory store | Long / sequential IDs | Slightly less human-friendly in demos |
| `@RestControllerAdvice` for errors | One place for consistent 4xx/5xx payloads | Per-controller try/catch | Central handler must stay lean |

## Project structure

```
src/main/java/com/example/customerapi/
  controller/     HTTP mapping and status codes
  service/        Business rules (email uniqueness, mapping)
  repository/     In-memory persistence
  model/          Customer entity and status enum
  dto/            Request, response, and error payloads
  exception/      Domain exceptions and global handler
```

## Known limitations

- No authentication or authorization
- No persistence across process restarts
- No pagination, filtering, or sorting on list
- Email uniqueness is enforced in-process only (not suitable for multi-instance deployment without a shared store)

## Interview notes

Prefer explaining the layering, HTTP semantics, validation policy, and test strategy over adding frameworks. The in-memory repository is intentional simplicity; migrating to JPA would mainly replace `CustomerRepository` and introduce an `@Entity` while keeping controller/service contracts stable.

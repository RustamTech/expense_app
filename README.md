# Coolproject — Expense Tracker API

A REST backend for tracking personal expenses, built with Spring Boot 3.4 and Java 21. Users register,
log in with a JWT and manage their own expenses; every expense and statistic is scoped to its owner.
Administrators manage the shared category catalog and user accounts. All errors follow RFC 9457
(`application/problem+json`).

📘 **[Usage guide](docs/USAGE.md):** Docker, Gradle, configuration, every endpoint with curl examples,
working with the database, changing the code and troubleshooting.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 (Gradle toolchain) |
| Framework | Spring Boot 3.4.5 |
| Security | Spring Security, stateless JWT (jjwt 0.12), BCrypt |
| Database | PostgreSQL 16 |
| Persistence | Spring Data JPA / Hibernate |
| Mapping | MapStruct |
| Validation | Jakarta Bean Validation |
| API docs | SpringDoc OpenAPI (Swagger UI with Bearer auth) |
| Testing | JUnit 5, Mockito, AssertJ, MockMvc, spring-security-test |
| Quality | Spotless (google-java-format), JaCoCo coverage gate (80% lines) |
| Build | Gradle 9 (wrapper) |
| Boilerplate | Lombok |

---

## Getting Started

**Prerequisites:** JDK 21, Docker.

```bash
# 1. Start PostgreSQL
docker compose up -d postgres

# 2. Run the app (uses the `dev` profile automatically)
./gradlew bootRun
```

Open Swagger UI at http://localhost:8080/swagger-ui, click **Authorize** and paste the token from
`/auth/login` (without the `Bearer ` prefix).

The `dev` profile (`application-dev.yaml`) is meant for a developer machine only. It provides:

- the docker-compose DB password (`postgres`),
- a development JWT secret,
- a bootstrap administrator `admin@local.dev` / `admin12345`.

On Windows PowerShell, use `.\gradlew.bat` instead of `./gradlew`.

### Configuration

Outside the `dev` profile, the app reads its secrets from environment variables. Nothing secret has
a default in `application.yaml`.

| Variable | Required | Default | Purpose |
|---|---|---|---|
| `JWT_SECRET` | **yes** | — | Base64 HMAC key, at least 256 bits. The app refuses to start without it or with a shorter key. |
| `DB_PASSWORD` | **yes** | — | PostgreSQL password |
| `DB_URL` | no | `jdbc:postgresql://localhost:5432/expensedb` | JDBC URL |
| `DB_USERNAME` | no | `postgres` | DB user |
| `JWT_EXPIRATION` | no | `86400000` (24 h) | Token lifetime, in milliseconds |
| `ADMIN_EMAIL` / `ADMIN_PASSWORD` | no | empty | Bootstrap administrator, created once on startup if both are set |
| `JPA_DDL_AUTO` | no | `update` | Hibernate schema mode |
| `SPRING_PROFILES_ACTIVE` | no | `dev` for `bootRun` | Active Spring profile |

---

## Development Commands

| Task | Command |
|---|---|
| Format code | `./gradlew spotlessApply` |
| Full check (format + all tests + 80% coverage) | `./gradlew check` |
| Unit tests only, no DB needed | `./gradlew test -PunitOnly` |
| Run one test class | `./gradlew test --tests '*ExpenseServiceImplTest'` |
| Coverage report | `build/reports/jacoco/test/html/index.html` |

`./gradlew check` runs `CoolprojectApplicationTests`, which needs the local PostgreSQL to be running.
The coverage gate leaves out DTO, entity, enum, config and mapper packages. The compiler runs with
`-Xlint:unchecked,deprecation`.

---

## API

Base path: `/api/notes`. Every endpoint except `/auth/**` needs `Authorization: Bearer <token>`.

### Authentication — public

| Method | Path | Description |
|---|---|---|
| POST | `/auth/register` | Register a new user (always role `USER`). Returns a JWT. |
| POST | `/auth/login` | Log in. Returns a JWT plus email, name and role. |

Emails are trimmed and lower-cased before they are stored or compared.

### Profile — any authenticated user

| Method | Path | Description |
|---|---|---|
| GET | `/users/me` | Current user's profile |
| PUT | `/users/me` | Update first name, last name and phone |

### Expenses — owner only

Every query is filtered by the authenticated user. Another user's expense answers **404**, never
403, so the API doesn't reveal that it exists. Lists are sorted newest first.

| Method | Path | Description |
|---|---|---|
| GET | `/expenses` | List own expenses |
| GET | `/expenses/{id}` | Get one own expense |
| GET | `/expenses/status/{status}` | Filter by status: `PENDING`, `PLANNED`, `PAID` |
| GET | `/expenses/category/{categoryId}` | Filter by category |
| POST | `/expenses` | Create an expense |
| PUT | `/expenses/{id}` | Update an own expense |
| DELETE | `/expenses/{id}` | Delete an own expense |

### Categories — read: any user · write: ADMIN

Categories are a shared catalog. Names are unique, ignoring case.

| Method | Path | Access | Description |
|---|---|---|---|
| GET | `/categories` | user | List categories |
| GET | `/categories/{id}` | user | Get a category |
| POST | `/categories` | ADMIN | Create (409 if the name is taken) |
| PUT | `/categories/{id}` | ADMIN | Update or rename (409 if the name is taken) |
| DELETE | `/categories/{id}` | ADMIN | Delete (409 `CATEGORY_IN_USE` if expenses reference it) |

### Statistics — owner only

| Method | Path | Description |
|---|---|---|
| GET | `/statistics/summary` | Summary of the current user's expenses |

Amounts in different currencies are never added together. The response groups them by currency:

```json
{
  "totalCount": 3,
  "totalByCurrency": { "AZN": 62.50, "USD": 10.00 },
  "amountByCategory": { "AZN": { "Food": 12.50, "Transport": 50.00 }, "USD": { "Food": 10.00 } },
  "countByStatus": { "PAID": 2, "PENDING": 1 }
}
```

### Admin: users — ADMIN only

| Method | Path | Description |
|---|---|---|
| GET | `/admin/users?page=0&size=20&sort=id` | Paged list of users |
| GET | `/admin/users/{id}` | Get a user |
| PATCH | `/admin/users/{id}/role` | Change role: `{"role": "ADMIN"}` |
| PATCH | `/admin/users/{id}/status` | Block or unblock: `{"enabled": false}` |
| DELETE | `/admin/users/{id}` | Delete a user and all their expenses |

An administrator can't change the role of, block or delete their own account (409
`OPERATION_NOT_ALLOWED`). A blocked or deleted user loses access right away, even with a token
issued earlier.

Paged responses look like `{ "content": [...], "page": 0, "size": 20, "totalElements": 42, "totalPages": 3 }`.

---

## Error Handling

Every error, including 401 and 403 from the security filter chain, is returned as an RFC 9457
`ProblemDetail` with two extra fields: a stable `code` and a `timestamp`. Validation errors also list
each failing field.

```json
{
  "type": "about:blank",
  "title": "Bad Request",
  "status": 400,
  "detail": "Request validation failed",
  "instance": "/api/notes/expenses",
  "code": "VALIDATION_FAILED",
  "timestamp": "2026-11-20T10:15:30Z",
  "errors": [{ "field": "amount", "message": "must be greater than 0" }]
}
```

| Status | Codes |
|---|---|
| 400 | `VALIDATION_FAILED`, `MALFORMED_JSON`, `INVALID_PARAMETER`, `MISSING_PARAMETER` |
| 401 | `AUTHENTICATION_REQUIRED`, `INVALID_TOKEN` (expired or tampered), `INVALID_CREDENTIALS` |
| 403 | `ACCESS_DENIED` (wrong role) |
| 404 | `EXPENSE_NOT_FOUND`, `CATEGORY_NOT_FOUND`, `USER_NOT_FOUND`, `RESOURCE_NOT_FOUND` (unknown URL) |
| 405 / 406 / 415 | `METHOD_NOT_ALLOWED`, `NOT_ACCEPTABLE`, `UNSUPPORTED_MEDIA_TYPE` |
| 409 | `RESOURCE_ALREADY_EXISTS`, `CATEGORY_IN_USE`, `OPERATION_NOT_ALLOWED`, `DATA_CONFLICT` |
| 500 | `INTERNAL_SERVER_ERROR` (details are logged, never returned) |

Malformed JSON, unknown enum values (the error lists the allowed ones), wrong JSON types and invalid
dates each get a specific message. Database constraint violations return 409 without SQL details.

---

## Validation Rules

| Field | Rule |
|---|---|
| Email | valid format, at most 254 chars, unique |
| Password | 8 to 72 chars (the BCrypt input limit) |
| First / last name | not blank, at most 50 chars |
| Phone | 7 to 15 digits with an optional leading `+`, for example `+994501234567` |
| Expense amount | greater than 0, at most 2 decimal places |
| Expense description / note | at most 255 / 1000 chars |
| Category name / icon / color | not blank, at most 50 / 50 / 30 chars |

---

## Domain Model

- **UserEntity**: `firstName`, `lastName`, `email` (unique), `phoneNumber`, `password` (BCrypt),
  `userRole` (`USER` | `ADMIN`), `enabled`. Implements Spring Security `UserDetails`.
- **CategoryEntity**: `name` (unique), `icon`, `color`, `categoryType` (`INCOME` | `EXPENSE`), `defaultCategory`.
- **ExpenseEntity**: `amount` (numeric 19,2), `description`, `note`, `date`, `statusType`
  (`PENDING` | `PLANNED` | `PAID`), `currencyType` (`AZN` | `USD` | `EUR`). It belongs to one user
  and one category.

All entities extend `BaseEntity` (`id`, `createdAt`, `updatedAt`).

---

## Project Structure

```
src/main/java/com/example/notes/application/coolproject/
├── config/        Endpoints (all API paths), SecurityConfig, OpenApiConfig, AdminProperties
├── controller/    Auth, User, Expense, Category, Statistics, AdminUser (no business logic)
├── dto/           Request/response records per domain, PageResponse, shared validators
├── entity/        JPA entities
├── enums/         CategoryType, CurrencyType, StatusType, UserRole
├── exception/     BaseException, ErrorCode, GlobalExceptionHandler, custom/ exceptions
├── mapper/        MapStruct mappers
├── repository/    Spring Data repositories (owner-scoped queries)
├── security/      JwtService, JwtAuthFilter, JwtProperties, UserDetailsServiceImpl, EmailNormalizer
└── service/       admin/, auth/, category/, expense/, statistics/, user/ (interface + Impl)
```

Requests flow `controller → service → repository → entity`. Only DTOs leave the API, never
entities. Each service method runs in its own transaction, and read methods use `readOnly = true`.

---

## Security

- Stateless JWT (HMAC SHA-256). The key is decoded once and must be at least 256 bits. There is no
  default secret.
- Passwords are hashed with BCrypt. Tokens and passwords are never logged.
- Two roles: `USER` (assigned at registration) and `ADMIN` (created at startup from
  `ADMIN_EMAIL`/`ADMIN_PASSWORD`, or promoted by another admin).
- Access rules: `/admin/**` and category writes need `ADMIN`; every other endpoint needs an
  authenticated user. 401 means not authenticated, 403 means the wrong role.
- Users can only reach their own data. Access is enforced in repository queries, so guessing IDs
  doesn't expose other users' records.
- HTTP Basic, form login and server-side sessions are disabled. The error page never shows messages
  or stack traces.

---

## Infrastructure (`docker-compose.yaml`)

| Service | Image | Port | Status |
|---|---|---|---|
| PostgreSQL | postgres:16 | 5432 | used |
| Redis | redis:7 | 6379 | placeholder |
| Keycloak | keycloak:24.0 | 8180 | placeholder |
| MinIO | minio/minio | 9000 / 9001 | placeholder |

---

## Roadmap

**Done**

- JWT authentication and user profile
- Owner-scoped expenses with filtering, plus per-currency statistics
- Role-based access (`USER` / `ADMIN`), admin user management and a bootstrap admin
- Admin-managed category catalog with unique names and in-use protection
- Unified RFC 9457 error handling
- Validation at the API boundary
- Unit and MockMvc tests, Spotless and an 80% coverage gate

**Planned**

- Refresh tokens and logout or token revocation
- Schema migrations with Flyway or Liquibase (instead of `ddl-auto: update`)
- Pagination and date-range filters for expenses
- Redis caching, Keycloak SSO, receipt uploads to MinIO
- Email verification

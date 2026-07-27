# Coolproject — Expense Tracker API

A RESTful expense tracking backend built with Spring Boot 3 and Java 21. Users register, log in with
JWT, and manage their own expenses and categories — every resource is scoped to its owner, with
aggregated statistics available on demand.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.4.5 |
| Security | Spring Security + JWT (jjwt 0.12.5) |
| Database | PostgreSQL 16 |
| ORM | Spring Data JPA / Hibernate |
| Mapping | MapStruct 1.5.5 |
| Validation | Jakarta Bean Validation |
| API Docs | SpringDoc OpenAPI (Swagger UI, with Bearer-token auth) |
| Testing | JUnit 5, Mockito, Spring MockMvc |
| Build | Gradle 9.4 |
| Boilerplate | Lombok |

---

## Project Structure

```
src/main/java/com/example/notes/application/coolproject/
├── config/
│   ├── Endpoints.java          # Centralized API path constants
│   ├── OpenApiConfig.java      # Swagger / OpenAPI + Bearer-auth scheme
│   └── SecurityConfig.java     # Spring Security filter chain, JWT filter wiring
├── controller/
│   ├── AuthController.java         # POST /register, /login
│   ├── UserController.java         # GET/PUT current user profile
│   ├── CategoryController.java     # CRUD for categories
│   ├── ExpenseController.java      # CRUD + filtering for expenses (owner-scoped)
│   └── StatisticsController.java   # Aggregated spending summary (owner-scoped)
├── dto/
│   ├── auth/                   # RegisterRequest, LoginRequest, AuthResponse
│   ├── userDto/                # UserResponse, UserUpdateRequest
│   ├── categoryDto/            # CategoryCreateRequest, UpdateRequest, Response
│   ├── expenseDto/             # ExpenseCreateRequest, UpdateRequest, Response
│   └── statisticsDto/          # StatisticsSummaryResponse
├── entity/
│   ├── BaseEntity.java         # id, createdAt, updatedAt (MappedSuperclass)
│   ├── UserEntity.java         # UserDetails implementation
│   ├── CategoryEntity.java
│   └── ExpenseEntity.java      # owned by a UserEntity, linked to a CategoryEntity
├── enums/
│   ├── CategoryType.java       # INCOME | EXPENSE
│   ├── CurrencyType.java       # AZN | USD | EUR
│   ├── StatusType.java         # PENDING | PLANNED | PAID
│   └── UserRole.java           # USER | ADMIN
├── exception/
│   ├── BaseException.java
│   ├── ErrorCode.java
│   ├── ErrorResponse.java
│   ├── GlobalExceptionHandler.java   # Centralized @RestControllerAdvice
│   └── custom/                 # CategoryNotFoundException, ExpenseNotFoundException, UserNotFoundException, ...
├── mapper/
│   ├── CategoryMapper.java
│   ├── ExpenseMapper.java
│   └── UserMapper.java         # MapStruct mappers (generated at compile time)
├── repository/
│   ├── CategoryRepository.java
│   ├── ExpenseRepository.java  # owner-scoped query methods
│   └── UserRepository.java
└── service/
    ├── category/               # CategoryService + CategoryServiceImpl
    ├── expense/                # ExpenseService + ExpenseServiceImpl
    ├── user/                   # UserService + UserServiceImpl
    ├── statistics/             # StatisticsService + StatisticsServiceImpl
    └── security/                # AuthService, JwtService, JwtAuthFilter, UserDetailsServiceImpl
```

---

## API Endpoints

Base path: `/api/notes`

### Authentication

| Method | Path | Description | Auth required |
|---|---|---|---|
| POST | `/auth/register` | Register a new user, returns JWT | No |
| POST | `/auth/login` | Login, returns JWT | No |

### Users

| Method | Path | Description | Auth required |
|---|---|---|---|
| GET | `/users/me` | Get current user's profile | Yes |
| PUT | `/users/me` | Update current user's profile | Yes |

### Categories

| Method | Path | Description | Auth required |
|---|---|---|---|
| GET | `/categories` | List all categories | Yes |
| GET | `/categories/{id}` | Get category by ID | Yes |
| POST | `/categories` | Create category | Yes |
| PUT | `/categories/{id}` | Update category | Yes |
| DELETE | `/categories/{id}` | Delete category | Yes |

### Expenses

All expense endpoints only ever return or mutate expenses owned by the authenticated user.

| Method | Path | Description | Auth required |
|---|---|---|---|
| GET | `/expenses` | List the current user's expenses | Yes |
| GET | `/expenses/{id}` | Get one of the current user's expenses by ID | Yes |
| POST | `/expenses` | Create an expense owned by the current user | Yes |
| PUT | `/expenses/{id}` | Update one of the current user's expenses | Yes |
| DELETE | `/expenses/{id}` | Delete one of the current user's expenses | Yes |
| GET | `/expenses/status/{status}` | Filter own expenses by status (`PENDING`, `PLANNED`, `PAID`) | Yes |
| GET | `/expenses/category/{categoryId}` | Filter own expenses by category | Yes |

### Statistics

| Method | Path | Description | Auth required |
|---|---|---|---|
| GET | `/statistics/summary` | Aggregated spending summary for the current user (total, by category, by status, by currency) | Yes |

Swagger UI: `http://localhost:8080/swagger-ui/index.html` — click **Authorize** and paste your JWT (no `Bearer ` prefix needed) to try protected endpoints directly from the docs.

---

## Domain Model

**UserEntity** — stores user credentials; implements Spring Security `UserDetails`. Fields: `firstName`, `lastName`, `email` (unique), `phoneNumber`, `password`, `userRole`, `enabled`.

**CategoryEntity** — a shared expense category. Fields: `name`, `icon`, `color`, `categoryType` (`INCOME`/`EXPENSE`), `defaultCategory`.

**ExpenseEntity** — a single expense record, owned by exactly one user. Fields: `amount`, `description`, `note`, `date`, `statusType`, `currencyType`, linked to one `CategoryEntity` and one `UserEntity`.

---

## Security

- Stateless JWT authentication (HS256) — no server-side sessions.
- Passwords hashed with BCrypt.
- Every expense/statistics query is scoped to the authenticated user at the repository level, preventing one user from accessing another's data by guessing IDs.
- Centralized exception handling returns consistent JSON error bodies (`{message, code, timeStamp, status}`) for validation errors, not-found resources, and authentication failures (bad login returns a clean `401`, not a `500`).

---

## Infrastructure (docker-compose.yaml)

| Service | Image | Port |
|---|---|---|
| PostgreSQL | postgres:16 | 5432 |
| Redis | redis:7 | 6379 |
| Keycloak | keycloak:24.0 | 8180 |
| MinIO | minio/minio | 9000 / 9001 |

> Redis, Keycloak, and MinIO are included in the compose file but not yet wired into the application code — they are infrastructure placeholders for planned features.

---

## Current State

### Done
- JWT-based authentication (register / login) with proper 401 handling on bad credentials
- User profile endpoints (`/users/me`)
- Full CRUD for Categories and Expenses
- Per-user data isolation for expenses and statistics (no IDOR)
- Filtering expenses by status and category
- Aggregated spending statistics per user
- Structured exception handling with `ErrorCode` enum
- MapStruct mappers for all entities
- Swagger UI with Bearer-token authorization
- Unit and MockMvc controller tests
- Docker Compose with PostgreSQL

### Not yet implemented / TODO
- Role-based access control (an `ADMIN` role exists but nothing enforces it yet)
- Refresh tokens / logout invalidation (JWTs are stateless with a fixed 24h expiry)
- Schema migrations via Flyway/Liquibase (currently relies on `ddl-auto: update`)
- Redis integration (caching / sessions)
- Keycloak integration (SSO / OAuth2)
- MinIO integration (file/receipt uploads)
- Email verification flow

---

## Running Locally

**1. Start the database**
```bash
docker-compose up -d postgres
```

**2. Set required environment variables** (or rely on the local defaults in `application.yaml`)
```bash
export JWT_SECRET=<your-base64-secret>
export DB_URL=jdbc:postgresql://localhost:5432/expensedb
export DB_USERNAME=postgres
export DB_PASSWORD=postgres
```

**3. Build and run**
```bash
./gradlew bootRun
```

Swagger UI: `http://localhost:8080/swagger-ui/index.html`

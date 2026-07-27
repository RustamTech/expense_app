# Coolproject — Expense Tracker API

A RESTful expense tracking backend built with Spring Boot 3 and Java 21.

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
| API Docs | SpringDoc OpenAPI (Swagger UI) |
| Build | Gradle 9.4 |
| Boilerplate | Lombok |

---

## Project Structure

```
src/main/java/com/example/notes/application/coolproject/
├── config/
│   ├── Endpoints.java          # Centralized API path constants
│   └── OpenApiConfig.java      # Swagger / OpenAPI configuration
├── controller/
│   ├── AuthController.java     # POST /register, /login
│   └── CategoryController.java # CRUD for categories
├── dto/
│   ├── auth/                   # RegisterRequest, LoginRequest, AuthResponse
│   ├── categoryDto/            # CategoryCreateRequest, UpdateRequest, Response
│   └── expenseDto/             # ExpenseCreateRequest, UpdateRequest, Response
├── entity/
│   ├── BaseEntity.java         # id, createdAt, updatedAt (MappedSuperclass)
│   ├── UserEntity.java         # UserDetails implementation
│   ├── CategoryEntity.java
│   └── ExpenseEntity.java
├── enums/
│   ├── CategoryType.java       # INCOME | EXPENSE
│   ├── CurrencyType.java       # AZN | USD | EUR
│   ├── StatusType.java         # PENDING | PLANNED | PAID
│   └── UserRole.java
├── exception/
│   ├── BaseException.java
│   ├── ErrorCode.java
│   ├── ErrorResponse.java
│   └── custom/                 # CategoryNotFoundException, ExpenseNotFoundException, ...
├── mapper/
│   ├── CategoryMapper.java
│   └── ExpenseMapper.java      # MapStruct mappers (generated at compile time)
├── repository/
│   ├── CategoryRepository.java
│   ├── ExpenseRepository.java
│   └── UserRepository.java
└── service/
    ├── category/               # CategoryService + CategoryServiceImpl
    ├── expense/                # ExpenseService + ExpenseServiceImpl
    └── security/               # AuthService, JwtService, JwtAuthFilter, UserDetailsServiceImpl
```

---

## API Endpoints

Base path: `/api/notes`

### Authentication

| Method | Path | Description | Auth required |
|---|---|---|---|
| POST | `/auth/register` | Register a new user | No |
| POST | `/auth/login` | Login and receive JWT | No |

### Categories

| Method | Path | Description | Auth required |
|---|---|---|---|
| GET | `/categories` | List all categories | Yes |
| GET | `/categories/{id}` | Get category by ID | Yes |
| POST | `/categories` | Create category | Yes |
| PUT | `/categories/{id}` | Update category | Yes |
| DELETE | `/categories/{id}` | Delete category | Yes |

### Expenses

| Method | Path | Description | Auth required |
|---|---|---|---|
| GET | `/expenses` | List all expenses | Yes |
| GET | `/expenses/{id}` | Get expense by ID | Yes |
| POST | `/expenses` | Create expense | Yes |
| PUT | `/expenses/{id}` | Update expense | Yes |
| DELETE | `/expenses/{id}` | Delete expense | Yes |
| GET | `/expenses/status/{status}` | Filter by status (`PENDING`, `PLANNED`, `PAID`) | Yes |
| GET | `/expenses/category/{categoryId}` | Filter by category | Yes |

Swagger UI available at: `http://localhost:8080/swagger-ui.html`

---

## Domain Model

**UserEntity** — stores user credentials; implements Spring Security `UserDetails`. Fields: `firstName`, `lastName`, `email` (unique), `phoneNumber`, `password`, `userRole`, `enabled`.

**CategoryEntity** — expense category. Fields: `name`, `icon`, `color`, `categoryType` (`INCOME`/`EXPENSE`), `defaultCategory`.

**ExpenseEntity** — a single expense record. Fields: `amount`, `description`, `note`, `date`, `statusType`, `currencyType`, linked to one `CategoryEntity`.

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
- JWT-based authentication (register / login)
- Full CRUD for Categories and Expenses
- Filtering expenses by status and category
- Structured exception handling with `ErrorCode` enum
- MapStruct mappers for all entities
- Swagger UI integration
- Docker Compose with PostgreSQL

### Not yet implemented / TODO
- Expense controller is missing (service and repository exist, but no `ExpenseController` class)
- User account management endpoints
- Redis integration (caching / sessions)
- Keycloak integration (SSO / OAuth2)
- MinIO integration (file/receipt uploads)
- Role-based access control enforcement beyond basic auth
- Email verification (`enabled` flag exists on `UserEntity` but activation flow is not implemented)
- Tests beyond the default Spring Boot smoke test

---

## Running Locally

**1. Start the database**
```bash
docker-compose up -d postgres
```

**2. Configure `application.properties` / `application.yml`**
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/expensedb
spring.datasource.username=postgres
spring.datasource.password=postgres
spring.jpa.hibernate.ddl-auto=update
```

**3. Build and run**
```bash
./gradlew bootRun
```
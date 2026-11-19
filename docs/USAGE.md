# Usage Guide — Expense Tracker API

A step-by-step guide to running, using and changing the application. The [README](../README.md)
gives the short overview; this file walks through every workflow.

**Contents**

1. [Tools you need](#1-tools-you-need)
2. [Docker: the infrastructure](#2-docker-the-infrastructure)
3. [Gradle: build, run, test, format](#3-gradle-build-run-test-format)
4. [Configuration and profiles](#4-configuration-and-profiles)
5. [Using the API](#5-using-the-api)
6. [Endpoint reference with examples](#6-endpoint-reference-with-examples)
7. [Working with the database](#7-working-with-the-database)
8. [Changing the code](#8-changing-the-code)
9. [Troubleshooting](#9-troubleshooting)

---

## 1. Tools you need

| Tool | Version | What it's for |
|---|---|---|
| JDK | 21 | Compiling and running the app (Gradle uses a Java 21 toolchain) |
| Docker Desktop | any recent | Running PostgreSQL and the other infrastructure containers |
| Git | any | Version control |
| curl, Postman or a browser | — | Calling the API. Swagger UI runs in the browser. |

Gradle itself doesn't need to be installed. The project ships the wrapper (`gradlew` / `gradlew.bat`),
which downloads the correct Gradle version on first use.

> **Windows:** in PowerShell, write `.\gradlew.bat` wherever this guide says `./gradlew`.
> In PowerShell, `curl` is an alias for `Invoke-WebRequest`, so use `curl.exe` for the examples
> below, or run them from Git Bash.

---

## 2. Docker: the infrastructure

`docker-compose.yaml` describes four containers. The application itself is **not** containerized:
it runs on your machine through Gradle and connects to these containers.

| Service | Container | Port(s) | Login | Used by the app? |
|---|---|---|---|---|
| `postgres` | `expense_postgres` | 5432 | `postgres` / `postgres`, DB `expensedb` | **Yes**, required |
| `redis` | `expense_redis` | 6379 | none | Not yet |
| `keycloak` | `expense_keycloak` | 8180 → UI at http://localhost:8180 | `admin` / `password` | Not yet |
| `minio` | `expense_minio` | 9000 (API), 9001 (console at http://localhost:9001) | `minioadmin` / `minioadmin` | Not yet |

Only `postgres` is needed to run the app. Start the others only if you're experimenting with them.

### Start

```bash
docker compose up -d postgres          # only the database (recommended)
docker compose up -d                   # all four services
```

`-d` runs the containers in the background. On the first run, Docker downloads the images.

### Check status and logs

```bash
docker compose ps                      # which containers are running, and their ports
docker compose logs -f postgres        # follow the database logs (Ctrl+C to stop following)
```

### Stop

```bash
docker compose stop                    # stop containers, keep them and their data
docker compose start                   # start the stopped containers again
docker compose down                    # stop and remove the containers; data volumes are KEPT
```

### Delete data (full reset)

```bash
docker compose down -v                 # removes the containers AND the volumes (postgres_data, minio_data)
docker compose up -d postgres          # fresh, empty database
```

> ⚠️ `down -v` permanently deletes every user, expense and category in the local database.
> On the next start, the app recreates the tables and the bootstrap admin (see §4).

### Change a service

1. Edit `docker-compose.yaml` (for example, change a port mapping like `"5433:5432"`, or a password).
2. Apply the change: `docker compose up -d <service>`. Compose recreates only the containers that changed.
3. If you changed the Postgres port or password, update `DB_URL` / `DB_PASSWORD` for the app too (§4).

> Changing `POSTGRES_PASSWORD` has **no effect** on an existing volume. Postgres reads it only when
> it first creates the database. Run `docker compose down -v` to apply a new password.

### Update the images

```bash
docker compose pull                    # download newer images
docker compose up -d                   # recreate the containers with them
```

---

## 3. Gradle: build, run, test, format

| Goal | Command | Notes |
|---|---|---|
| Run the app | `./gradlew bootRun` | Uses the `dev` profile. Stop with Ctrl+C. |
| Compile only | `./gradlew compileJava` | Fast check that the code compiles |
| Build the jar | `./gradlew bootJar` | Output: `build/libs/coolproject-0.0.1-SNAPSHOT.jar` |
| Run the jar | `java -jar build/libs/coolproject-0.0.1-SNAPSHOT.jar` | Needs the env vars from §4 (no `dev` profile by default) |
| Format code | `./gradlew spotlessApply` | google-java-format |
| Check formatting | `./gradlew spotlessCheck` | Fails if any file isn't formatted |
| Unit tests (no DB) | `./gradlew test -PunitOnly` | Skips `CoolprojectApplicationTests` |
| All tests | `./gradlew test` | Needs Postgres running |
| Full quality gate | `./gradlew check` | Formatting, all tests and ≥ 80% line coverage |
| One test class | `./gradlew test --tests '*ExpenseServiceImplTest'` | |
| One test method | `./gradlew test --tests '*ExpenseServiceImplTest.deletesExpenseOnlyForOwner'` | |
| Clean build output | `./gradlew clean` | Deletes `build/` |

Reports after a test run:

- Tests: `build/reports/tests/test/index.html`
- Coverage: `build/reports/jacoco/test/html/index.html`

To run the jar with the dev settings, pass the profile:
`java -jar build/libs/coolproject-0.0.1-SNAPSHOT.jar --spring.profiles.active=dev`.

---

## 4. Configuration and profiles

| Profile | Activated by | Purpose |
|---|---|---|
| `dev` | `./gradlew bootRun` (default) | Local work. Has a DB password, a dev JWT secret and a bootstrap admin. |
| `test` | the test suite | Settings for automated tests |
| *(none)* | `java -jar …` without a profile | Production-like. Every secret must come from the environment. |

The **dev profile** gives you these defaults:

| Setting | Value |
|---|---|
| DB | `jdbc:postgresql://localhost:5432/expensedb`, `postgres` / `postgres` |
| JWT secret | a fixed development key (never use it elsewhere) |
| Bootstrap admin | `admin@local.dev` / `admin12345` |

**Environment variables** override any profile value:

| Variable | Meaning |
|---|---|
| `JWT_SECRET` | Base64 key of at least 32 bytes (256 bits). Required without `dev`. |
| `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` | Database connection |
| `JWT_EXPIRATION` | Token lifetime in ms (default `86400000` = 24 h) |
| `ADMIN_EMAIL`, `ADMIN_PASSWORD` | Admin account created at startup if it doesn't exist yet |
| `JPA_DDL_AUTO` | `update` (default), `validate`, `create`, `create-drop` |
| `SPRING_PROFILES_ACTIVE` | Profile used by `bootRun` |

Generate a secret:

```bash
openssl rand -base64 32
```

Set the variables for one session, then run:

```bash
# Git Bash / Linux / macOS
export JWT_SECRET="$(openssl rand -base64 32)"
export DB_PASSWORD=postgres
./gradlew bootRun
```

```powershell
# PowerShell
$env:JWT_SECRET = "<your-base64-secret>"
$env:DB_PASSWORD = "postgres"
.\gradlew.bat bootRun
```

---

## 5. Using the API

- **Base URL:** `http://localhost:8080/api/notes`
- **Swagger UI:** http://localhost:8080/swagger-ui
- **OpenAPI JSON:** http://localhost:8080/api-docs

### The basic flow

1. **Register** (`POST /auth/register`) or **log in** (`POST /auth/login`). The response contains a `token`.
2. Send that token on every other request: `Authorization: Bearer <token>`.
3. The token expires after 24 hours by default. When it does, log in again.

### In Swagger UI

1. Open http://localhost:8080/swagger-ui.
2. Run `POST /auth/login` with `{"email": "admin@local.dev", "password": "admin12345"}`.
3. Copy the `token` value, click **Authorize** (top right), paste it and confirm. Leave out the `Bearer ` prefix.
4. Every call you make in Swagger now carries the token.

### With curl

```bash
BASE=http://localhost:8080/api/notes

TOKEN=$(curl -s -X POST $BASE/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@local.dev","password":"admin12345"}' | sed -E 's/.*"token":"([^"]+)".*/\1/')

curl -s $BASE/users/me -H "Authorization: Bearer $TOKEN"
```

### Roles

| Role | How you get it | What it can do |
|---|---|---|
| `USER` | Every new registration | Own profile, own expenses, own statistics, read categories |
| `ADMIN` | The bootstrap admin, or promotion by another admin | Everything a USER can, plus create, update and delete categories and manage users |

A new user can't record expenses until at least one category exists, and only an admin can create
categories. So on a fresh database, **log in as the admin and create categories first**.

---

## 6. Endpoint reference with examples

The examples assume the `BASE` and `TOKEN` variables from §5. Responses are shortened.

### 6.1 Authentication (public)

**Register** creates a new account with role `USER` and logs it in.

```bash
curl -s -X POST $BASE/auth/register -H "Content-Type: application/json" -d '{
  "firstName": "Rustam",
  "lastName": "Ismayilov",
  "email": "user@example.com",
  "phoneNumber": "+994501234567",
  "password": "Str0ngPassw0rd"
}'
```

```json
{ "token": "eyJhbGciOi...", "email": "user@example.com", "firstName": "Rustam", "lastName": "Ismayilov", "role": "USER" }
```

Rules: names are at most 50 chars; the phone has 7–15 digits with an optional leading `+`; the
password is 8–72 chars; the email must be unique. A duplicate email returns 409.

**Log in** returns a fresh token.

```bash
curl -s -X POST $BASE/auth/login -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","password":"Str0ngPassw0rd"}'
```

A wrong email or password returns 401 `INVALID_CREDENTIALS`. Emails are case-insensitive.

### 6.2 Profile (`/users/me`)

| Purpose | Request |
|---|---|
| See who you are | `GET /users/me` |
| Change name or phone | `PUT /users/me` with all three fields |

```bash
curl -s -X PUT $BASE/users/me -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d '{"firstName":"Rustam","lastName":"Ismayilov","phoneNumber":"+994551112233"}'
```

You can't change your email, password or role through this endpoint.

### 6.3 Categories

Any logged-in user can read categories. Creating, updating and deleting them needs `ADMIN`; a
USER gets 403.

| Purpose | Request | Access |
|---|---|---|
| List all categories | `GET /categories` | user |
| Get one category | `GET /categories/{id}` | user |
| Create a category | `POST /categories` | admin |
| Change a category (partial) | `PUT /categories/{id}` | admin |
| Delete a category | `DELETE /categories/{id}` | admin |

```bash
# create (admin token)
curl -s -X POST $BASE/categories -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d '{"name":"Food","icon":"🍔","color":"#FF5733","categoryType":"EXPENSE"}'

# rename only: fields you leave out stay unchanged
curl -s -X PUT $BASE/categories/1 -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d '{"name":"Groceries"}'

# delete
curl -s -X DELETE $BASE/categories/1 -H "Authorization: Bearer $TOKEN"
```

- `categoryType` is `INCOME` or `EXPENSE`.
- Names are unique ignoring case, so `food` conflicts with `Food` (409 `RESOURCE_ALREADY_EXISTS`).
- You can't delete a category that any expense uses (409 `CATEGORY_IN_USE`). Move or delete those expenses first.
- In a partial update, a field can be left out, but it can't be sent as blank `""` (400).

### 6.4 Expenses (own data only)

Every expense belongs to the user who created it. You never see other users' expenses; asking for
one returns 404.

| Purpose | Request |
|---|---|
| List all my expenses (newest first) | `GET /expenses` |
| Get one expense | `GET /expenses/{id}` |
| Filter by status | `GET /expenses/status/{PENDING\|PLANNED\|PAID}` |
| Filter by category | `GET /expenses/category/{categoryId}` |
| Record an expense | `POST /expenses` |
| Change an expense (partial) | `PUT /expenses/{id}` |
| Delete an expense | `DELETE /expenses/{id}` |

```bash
# create
curl -s -X POST $BASE/expenses -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" -d '{
  "amount": 12.50,
  "description": "Lunch",
  "note": "With my sister",
  "currencyType": "AZN",
  "date": "2026-11-20",
  "categoryId": 1,
  "statusType": "PAID"
}'

# mark as paid: only the fields you send change
curl -s -X PUT $BASE/expenses/5 -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d '{"statusType":"PAID"}'

# filters
curl -s $BASE/expenses/status/PENDING -H "Authorization: Bearer $TOKEN"
curl -s $BASE/expenses/category/1     -H "Authorization: Bearer $TOKEN"

# delete
curl -s -X DELETE $BASE/expenses/5 -H "Authorization: Bearer $TOKEN"
```

Field rules:

| Field | Values |
|---|---|
| `amount` | > 0, at most 2 decimals |
| `description` | required on create, at most 255 chars |
| `note` | optional, at most 1000 chars |
| `currencyType` | `AZN`, `USD`, `EUR` |
| `statusType` | `PENDING` (to pay), `PLANNED` (future), `PAID` |
| `date` | `YYYY-MM-DD` |
| `categoryId` | id of an existing category (404 if it doesn't exist) |

### 6.5 Statistics

`GET /statistics/summary` gives an overview of **your** expenses.

```json
{
  "totalCount": 3,
  "totalByCurrency": { "AZN": 62.50, "USD": 10.00 },
  "amountByCategory": { "AZN": { "Food": 12.50, "Transport": 50.00 }, "USD": { "Food": 10.00 } },
  "countByStatus": { "PAID": 2, "PENDING": 1 }
}
```

Amounts are grouped by currency, so AZN and USD are never added together.

### 6.6 Admin: user management

All of these need `ADMIN`.

| Purpose | Request |
|---|---|
| Browse users, paged | `GET /admin/users?page=0&size=20&sort=id,asc` |
| Look at one user | `GET /admin/users/{id}` |
| Promote or demote | `PATCH /admin/users/{id}/role` with `{"role":"ADMIN"}` or `{"role":"USER"}` |
| Block or unblock | `PATCH /admin/users/{id}/status` with `{"enabled":false}` or `{"enabled":true}` |
| Delete a user and all their expenses | `DELETE /admin/users/{id}` |

```bash
curl -s "$BASE/admin/users?page=0&size=10&sort=email,asc" -H "Authorization: Bearer $TOKEN"

curl -s -X PATCH $BASE/admin/users/7/status -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" -d '{"enabled":false}'
```

- A blocked or deleted user is locked out **immediately**, even if they still hold a valid token.
- An admin can't change their own role, block themselves or delete themselves (409
  `OPERATION_NOT_ALLOWED`). This keeps the system from losing its last admin by accident.
- Paged response: `{ "content": [...], "page": 0, "size": 10, "totalElements": 42, "totalPages": 5 }`.

### 6.7 Reading errors

Every error has the same shape (RFC 9457). Check `code` to tell errors apart; `detail` is a
human-readable message.

```json
{
  "status": 400,
  "title": "Bad Request",
  "detail": "Request validation failed",
  "instance": "/api/notes/expenses",
  "code": "VALIDATION_FAILED",
  "timestamp": "2026-11-20T10:15:30Z",
  "errors": [{ "field": "amount", "message": "must be greater than 0" }]
}
```

| You get | It usually means |
|---|---|
| 400 | Invalid body, missing field, wrong enum value or bad date format. Read `errors`. |
| 401 | No token, an expired or invalid token, or a wrong password |
| 403 | Logged in, but the action needs `ADMIN` |
| 404 | The resource doesn't exist or belongs to someone else, or the URL is wrong |
| 409 | Duplicate email or category name, category in use, or an admin acting on their own account |

---

## 7. Working with the database

The schema is created and updated automatically from the JPA entities (`ddl-auto: update`).
Tables: `users`, `categories`, `expenses`.

**Open a SQL shell** inside the container:

```bash
docker exec -it expense_postgres psql -U postgres -d expensedb
```

Useful commands in `psql`:

```sql
\dt                                   -- list tables
\d expenses                           -- table structure
SELECT id, email, user_role, enabled FROM users;
SELECT * FROM categories;
\q                                    -- quit
```

To connect from an IDE (IntelliJ Database, DBeaver), use host `localhost`, port `5432`, database
`expensedb` and user/password `postgres` / `postgres`.

**Back up and restore:**

```bash
docker exec expense_postgres pg_dump -U postgres expensedb > backup.sql
docker exec -i expense_postgres psql -U postgres -d expensedb < backup.sql
```

**Reset:** run `docker compose down -v`, then `docker compose up -d postgres` (see §2).

> `ddl-auto: update` adds new tables and columns, but it never drops or renames them. After you
> rename a field or tighten a constraint, reset the local DB. Schema migrations (Flyway) are on the
> roadmap.

---

## 8. Changing the code

### Where things live

```
src/main/java/com/example/notes/application/coolproject/
config/      Endpoints.java (ALL API paths), SecurityConfig (who can access what)
controller/  HTTP layer: validation + calling the service, no logic
service/     business logic (interface + Impl)
repository/  database queries (always filtered by owner)
entity/      JPA tables
dto/         request/response records + validation annotations
exception/   ErrorCode, GlobalExceptionHandler, custom/ exceptions
mapper/      MapStruct entity ↔ DTO conversion
src/main/resources/application*.yaml   configuration
```

### Typical changes

| I want to… | Touch these |
|---|---|
| Add an endpoint | Path constant in `Endpoints` → service interface + impl (with Javadoc) → controller method → tests |
| Change who may call an endpoint | `SecurityConfig.securityFilterChain` → add a `@WebMvcTest` for 401 and 403 |
| Add a field to expenses | `ExpenseEntity` → request/response DTOs (+ validation) → `ExpenseMapper` (if names differ) → tests → reset the local DB |
| Add a new error | New `ErrorCode` → exception in `exception/custom` extending `BaseException` → throw it from the service |
| Add a currency or status | `CurrencyType` / `StatusType` enum |
| Change a config value | `application.yaml` (with an `${ENV_VAR:default}` placeholder) and, for local defaults, `application-dev.yaml` |

### Workflow for every change

```bash
git switch -c EXP-<short-description>   # new branch, never commit to main
# … write a test first, then the code …
./gradlew spotlessApply                 # format
./gradlew check                         # format check + all tests + 80% coverage (Postgres must be up)
git add -A
git commit -m "EXP#<next-number> feat: <summary>"
git push -u origin EXP-<short-description>
# open a pull request into main on GitHub
```

Commit types: `feat`, `fix`, `refactor`, `test`, `docs`, `chore`, `perf`, `ci`. Never commit
`build/`, `.idea/`, `.env` or secrets.

---

## 9. Troubleshooting

| Symptom | Cause and fix |
|---|---|
| `Connection to localhost:5432 refused` | Postgres isn't running: `docker compose up -d postgres`, then check `docker compose ps`. |
| `password authentication failed for user "postgres"` | The volume was created with a different password. Use that password, or reset with `docker compose down -v`. |
| `port is already allocated` on `docker compose up` | Another program (often a local Postgres) uses the port. Stop it, or change the left side of the port mapping in `docker-compose.yaml` and update `DB_URL`. |
| App fails at startup with a `jwt.secret` binding error | `JWT_SECRET` is missing or shorter than 256 bits, and you're not on the `dev` profile. See §4. |
| `Port 8080 was already in use` | Another app instance is running. Stop it, or run with `--server.port=8081`. |
| Always 401 | The token is missing, expired (24 h) or copied with extra spaces, or the user was blocked. Log in again. |
| 403 on category or admin calls | Your user is `USER`. Use the admin account or have an admin promote you. |
| `./gradlew check` fails on formatting | Run `./gradlew spotlessApply` and commit the result. |
| `./gradlew check` fails on coverage | Add tests for the new code. Don't lower the threshold. |
| `CoolprojectApplicationTests` fails | It needs the local Postgres. Start it, or use `./gradlew test -PunitOnly` for a quick run. |
| Docker command not found / daemon not running | Start Docker Desktop and wait until it shows "running". |

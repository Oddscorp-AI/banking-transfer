# Banking Transfer

This project provides a minimal Spring Boot service demonstrating online registration for a banking application. It now includes an endpoint for tellers to create new saving accounts.

## Database

The service uses MySQL with schema managed by Flyway. Update
`src/main/resources/application.properties` with your credentials or run a local
instance on port `3306` with a database named `bankingdb`. When the application
starts, Flyway runs the scripts under `src/main/resources/db/migration` to
create the necessary tables.

## Building

```bash
mvn clean package
```

## Running tests

```bash
mvn test
```

The build generates a JaCoCo coverage report under
`target/site/jacoco/index.html` so code coverage can be inspected after tests
run.

## Testing strategy

Unit tests rely on **JUnit&nbsp;5** together with **Mockito** to verify service
logic and validation rules. Integration tests use **Testcontainers** with a
temporary **MySQL** instance via `@SpringBootTest` to check transaction
isolation and idempotency. Contract tests are written with **REST-Assured**
and Spring Cloud Contract to validate JSON schemas and status codes.

Tests run in parallel. JUnit is configured for concurrent execution and the
Surefire plugin forks `2.5C` JVMs with `reuseForks=true` so the suite is fast
and not flaky.

## Running the application

```bash
mvn spring-boot:run
```

## Docker

To build and run the service with MySQL locally using Docker Compose:

```bash
docker compose up --build
```

This command starts both a MySQL container and the Spring Boot application. The
application waits for MySQL to accept connections before starting so the
service initializes reliably.

## Security

Authentication uses JSON Web Tokens (JWT) signed with an RSA key pair
(RS256). Clients first obtain a token by calling:

```
POST /auth/login          # customers
POST /auth/teller/login   # tellers
```

Both endpoints accept a JSON body with `email` and `password`. The response
contains a `token` field which should be sent in the `Authorization: Bearer`
header for subsequent requests.

Endpoints still enforce role-based access via Spring Security annotations and a
helper bean ensuring customers only access their own accounts.

Each registered user also has a six-digit **PIN**. The PIN is hashed with
BCrypt and stored in the `pin_hash` column, separate from the login password.
It must be supplied when performing money transfers or requesting bank
statements so these operations use step-up authentication.

### OWASP Top 10 controls

To address common vulnerabilities the application adds several protections:

- A `SecurityHeadersFilter` sets `Content-Security-Policy`, `X-Content-Type-Options`,
  `X-Frame-Options`, and `X-XSS-Protection` headers on every response.
- JWT tokens are sent in the `Authorization` header so no cookies are used. If
  cookies are added later they must be marked `HttpOnly`.
- Request DTOs use Bean Validation to sanitize and validate input.
- A simple `RateLimitFilter` returns HTTP `429` when a client exceeds 100
  requests per minute.

## Design considerations

The domain model uses enumerations for user roles and transaction metadata.
This avoids fragile string comparisons and simplifies future extension with additional roles or transaction types.
Transactions are indexed on `(account_id, timestamp)` for efficient statement queries.
Table partitioning is disabled for compatibility with older MySQL versions.
Mappings between DTOs and entities are implemented using **MapStruct** so the
service code stays concise and type-safe.

## Creating accounts

Only authenticated **tellers** can create new accounts. Send a POST request to `/api/accounts` with JSON like:

```json
{
  "citizenId": "1234567890123",
  "thaiName": "Thai Name",
  "englishName": "English Name",
  "initialDeposit": 100.0
}
```

The response includes a generated `accountNumber` consisting of seven digits.

## Depositing money

Only authenticated tellers can deposit money into an existing account using HTTP Basic authentication.
Send a POST request to `/api/accounts/{accountNumber}/deposit` with JSON like:

```json
{
  "amount": 50.0
}
```

The amount must be at least 1 THB.

## Viewing account information

Registered **customers** can view their own account details. Send a GET request to
`/api/accounts/{accountNumber}` using HTTP Basic authentication with the email
and password from registration. The endpoint returns the account data only if
the logged-in user owns the account.

## Transferring money

Only authenticated customers can transfer money from their own account. Send a POST request to
`/api/accounts/{fromAccount}/transfer` using HTTP Basic authentication. Provide the
destination account number, amount (at least 1 THB), and your six-digit PIN:

```json
{
  "toAccount": "7654321",
  "amount": 50.0,
  "pin": "123456"
}
```

The response returns the updated source account balance.

Transfers are limited per account each day. The current limit is stored in the
`settings` table (`DAILY_TRANSFER_LIMIT`) so it can be adjusted without code
changes. Any attempt to exceed the configured limit results in a `400 Bad
Request` error.

Both deposit and transfer operations lock the account records to avoid duplicate
transaction postings when requests occur concurrently. These methods run with
**SERIALIZABLE** isolation using `SELECT ... FOR UPDATE` and an optimistic
`@Version` field to guarantee each posting happens exactly once.

## Retrieving a bank statement

Authenticated **customers** can request a bank statement for a particular month. Send a POST request to
`/api/accounts/{accountNumber}/statement` with the month (formatted as `YYYY-MM`) and your six-digit PIN:

```json
{
  "month": "2025-05",
  "pin": "123456"
}
```

The response is a list of transactions ordered from oldest to newest for that month.
Each item provides fields similar to the table below:

| Date | Time | Code | Channel | Debit/Credit | Balance | Remark |
|-----|-----|-----|-----|-----|-----|-----|
|15/12/2023|10:30|A0|OTC|100|100|Deposit Terminal 0101|
|15/12/2023|10:35|A1|OTC|-50|50|Withdraw Terminal 0102|
|16/12/2023|09:30|A0|OTC|725.5|775.5|Deposit Terminal 0103|
|16/12/2023|17:24|A1|ATS|-40.33|735.17|Transfer to X9732 Mr. John Doe|
|17/3/2024|08:37|A1|ATS|-10|725.17|Transfer to X1234 Mr. Michael Done|
|18/3/2024|14:22|A3|ATS|500|1225.17|Receive from X7777 Mrs. Love Everything|
|2/9/2023|21:55|A1|ATS|-233.55|991.62|Transfer to X7900 Mrs. Janes Does|

## API documentation

After running the application, Swagger UI is available at:

```
http://localhost:8080/swagger-ui.html
```
The documentation lists all available endpoints including `/api/accounts` for creating accounts.

## Error handling

When a request fails validation or a resource cannot be found the service returns a JSON error message. For example:

```json
{"error": "Account not found"}
```

The HTTP status code matches the problem (e.g. `400 Bad Request` for invalid input or `404 Not Found` for missing data).

## Metrics and observability

The application exposes Prometheus metrics via Spring Boot Actuator. When running
the service you can scrape metrics from `http://localhost:8080/actuator/prometheus`.

Each HTTP request is tagged with a `requestUid` which is written to the MDC so
log entries can be correlated with a particular request. The generated request
identifier is also returned in the `X-Request-Id` response header.

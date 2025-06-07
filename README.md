# Banking Transfer

This project provides a minimal Spring Boot service demonstrating online registration for a banking application. It now includes an endpoint for tellers to create new saving accounts.

## Database

The service now uses MySQL. Update `src/main/resources/application.properties` with your MySQL credentials or run a local instance on port `3306` with a database named `bankingdb`.

## Building

```bash
mvn clean package
```

## Running tests

```bash
mvn test
```

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

## Creating accounts

Tellers can create new accounts by sending a POST request to `/api/accounts` with JSON like:

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

Tellers can deposit money into an existing account by sending a POST request to
`/api/accounts/{accountNumber}/deposit` with JSON like:

```json
{
  "amount": 50.0
}
```

The amount must be at least 1 THB.

## Viewing account information

Registered customers can view their own account details. Send a GET request to
`/api/accounts/{accountNumber}` using HTTP Basic authentication with the email
and password from registration. The endpoint returns the account data only if
the logged-in user owns the account.

## API documentation

After running the application, Swagger UI is available at:

```
http://localhost:8080/swagger-ui.html
```
The documentation lists all available endpoints including `/api/accounts` for creating accounts.
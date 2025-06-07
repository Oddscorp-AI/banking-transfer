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

## API documentation

After running the application, Swagger UI is available at:

```
http://localhost:8080/swagger-ui.html
```
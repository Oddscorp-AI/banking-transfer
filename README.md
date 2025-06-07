# Banking Transfer

This project provides a minimal Spring Boot service demonstrating online registration for a banking application.

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

This command starts both a MySQL container and the Spring Boot application.

## API documentation

After running the application, Swagger UI is available at:

```
http://localhost:8080/swagger-ui.html
```

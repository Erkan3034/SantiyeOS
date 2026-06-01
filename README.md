# SantiyeOS API

SantiyeOS is a multi-tenant construction site management API. It provides authentication and operational endpoints for managing companies, projects, work orders, materials, payments, reporting, and more.
# FRONTEND REPO : (https://github.com/Erkan3034/santiyeos-frontend)

## Features

- JWT-based authentication and authorization
- Company, user, and contractor management
- Project and work order management (including notes and reports)
- Subscription and plan management
- Progress payment (hakediş) and payment tracking
- Material catalog, categories, and stock movements
- Notifications and reporting endpoints
- OpenAPI/Swagger documentation (dev profile)

## Tech Stack

- Java 21
- Spring Boot 3.5
- Spring Security + JWT (jjwt)
- Spring JDBC
- MySQL
- Springdoc OpenAPI (Swagger UI)

## Requirements

- JDK 21
- MySQL 8+

## Configuration

The API is configured via `application.properties` with environment overrides.

| Variable | Default | Description |
| --- | --- | --- |
| `DB_URL` | `jdbc:mysql://localhost:3306/santiyeos?useSSL=false&serverTimezone=Europe/Istanbul&allowPublicKeyRetrieval=true` | JDBC URL |
| `DB_USERNAME` | `admin` | Database username |
| `DB_PASSWORD` | *(empty)* | Database password |
| `JWT_SECRET` | `santiyeos-development-secret-key-change-this-before-production-2026` | JWT signing secret |
| `JWT_EXPIRATION_MINUTES` | `120` | JWT expiration in minutes |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:5173,http://127.0.0.1:5173` | CORS allow-list |
| `APP_LOG_LEVEL` | `INFO` | Application log level |

## Running Locally

1. Create a MySQL database named `santiyeos`.
2. Set environment variables as needed.
3. Run the application:

```bash
bash mvnw spring-boot:run
```

The API starts on `http://localhost:8081`.

### Swagger UI

Swagger UI is enabled by default in the `dev` profile:

- `http://localhost:8081/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8081/v3/api-docs`

In the `prod` profile, Swagger endpoints are disabled. To switch profiles:

```bash
SPRING_PROFILES_ACTIVE=prod bash mvnw spring-boot:run
```

## Testing

```bash
bash mvnw test
```


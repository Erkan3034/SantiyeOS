# SantiyeOS API

Multi-tenant REST API for construction site operations management. SantiyeOS lets a construction
company manage its projects, subcontractors, work orders, materials, stock, progress payments
(hakediş) and payments from a single system, with strict data isolation between tenant companies.

This repository contains the backend service. The web client lives in a separate repository:
[santiyeos-frontend](https://github.com/Erkan3034/santiyeos-frontend).

## Overview

The platform is built around two account contexts:

- **Platform administration (SUPER_ADMIN):** manages tenant companies, subscription plans and
  company subscriptions.
- **Company workspace:** each company manages its own users, projects, subcontractors, work orders,
  materials, stock movements, progress payments, payments, reports and notifications. A company can
  never read or write another company's data.

Every request is authenticated with a JWT and authorized against a role hierarchy. Tenant scoping is
enforced on the server for every company-owned resource, not just on the client.

## Tech Stack

| Layer | Technology |
| --- | --- |
| Language | Java 21 |
| Framework | Spring Boot 3.5 |
| Security | Spring Security, JWT (jjwt 0.12) |
| Data access | Spring JDBC (JdbcTemplate) over MySQL 8 |
| Validation | Jakarta Bean Validation |
| API docs | springdoc OpenAPI / Swagger UI |
| Monitoring | Spring Boot Actuator |
| Build | Maven |

## Architecture

The codebase follows a conventional layered architecture with clear separation of concerns:

```
controller   REST endpoints, request/response mapping, method-level authorization
service      business rules, tenant scoping, subscription limit enforcement
repository   data access (Spring JDBC), SQL execution
model        domain entities
dto          request/response payloads (request/ and response/)
security     JWT handling, role definitions, current-user context
filter       JWT authentication filter
config       security, CORS, OpenAPI configuration
exception    centralized error handling
```

Data access is intentionally implemented with Spring JDBC instead of an ORM. The schema is owned by
a dedicated SQL layer (tables, views, functions, stored procedures, triggers and scheduled events),
and the application calls into it directly. This keeps query behavior explicit and pushes
data-integrity rules down to the database.

### Roles

Authorization is enforced with `@PreAuthorize` at the controller layer. The defined roles are:

| Role | Scope |
| --- | --- |
| `SUPER_ADMIN` | Platform-wide: companies, plans, subscriptions |
| `FIRMA_ADMIN` | Full access within the company |
| `PROJE_YONETICISI` | Project-level management |
| `SAHA_PERSONELI` | Field operations (work orders, stock) |
| `TASERON_TEMSILCI` | Subcontractor-scoped access |

## Modules

- Authentication and authorization (JWT)
- Companies, subscription plans and subscriptions (with plan limit enforcement)
- Users and project-user assignments
- Subcontractors and subcontractor performance reporting
- Projects and work orders (with notes and reports)
- Progress payments (hakediş) and payments
- Material catalog, material categories and stock movements
- Dashboard summaries, operational reports and notifications

## Database

The SQL layer is maintained separately from the application code under
[`SQL_DOSYALARI/`](./SQL_DOSYALARI), with a documented run order in
[`SQL_DOSYALARI/00_RUN_ORDER.md`](./SQL_DOSYALARI/00_RUN_ORDER.md):

```
01_tablolar      table definitions (7 functional groups)
02_fonksiyonlar  stored functions
03_prosedurler   stored procedures
04_triggers      triggers
05_events        scheduled events
06_migrations    incremental migrations
```

## Requirements

- JDK 21
- MySQL 8+
- Maven (or the bundled `mvnw` wrapper)

## Configuration

Configuration is provided through `application.properties` with environment-variable overrides. See
[`.env.example`](./.env.example) for a complete template.

| Variable | Default | Description |
| --- | --- | --- |
| `SERVER_PORT` | `8081` | HTTP port |
| `DB_URL` | `jdbc:mysql://localhost:3306/santiyeos` | JDBC connection URL |
| `DB_USERNAME` | `admin` | Database username |
| `DB_PASSWORD` | *(empty)* | Database password |
| `JWT_SECRET` | development secret | JWT signing secret (set a strong value in production) |
| `JWT_EXPIRATION_MINUTES` | `120` | Access token lifetime |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:5173` | Comma-separated CORS allow-list |
| `APP_LOG_LEVEL` | `INFO` | Application log level |
| `SPRING_PROFILES_ACTIVE` | `dev` | Active profile (`dev` / `prod`) |

## Running Locally

1. Create a MySQL database named `santiyeos` and apply the SQL layer in the documented run order.
2. Set the required environment variables (see the table above).
3. Start the application:

```bash
./mvnw spring-boot:run
```

The API starts on `http://localhost:8081`.

### API Documentation

Swagger UI is enabled in the `dev` profile and disabled in `prod`:

- Swagger UI: `http://localhost:8081/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8081/v3/api-docs`

To run with the production profile:

```bash
SPRING_PROFILES_ACTIVE=prod ./mvnw spring-boot:run
```

## Testing

```bash
./mvnw test
```

## Deployment

A `Dockerfile` is provided for containerized deployment. Deployment notes are documented in
[`DEPLOYMENT.md`](./DEPLOYMENT.md).

## Project Structure

```
api/
  src/main/java/com/santiyeos/api/   application source
  src/main/resources/                configuration
  SQL_DOSYALARI/                     database schema and objects
  postman/                           Postman collection
  Dockerfile                         container build
  DEPLOYMENT.md                      deployment notes
```

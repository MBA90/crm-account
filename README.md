# CRM ACCOUNT Service

A Spring Boot microservice for managing CRM ACCOUNT data. It exposes a REST API
for CRUD operations over ACCOUNTs, backed by a PostgreSQL database with schema
managed by Liquibase.

## Tech Stack

- Java 21
- Spring Boot 3.5.15 (Web, Data JPA, Validation)
- PostgreSQL (`postgresql` JDBC driver)
- Liquibase (schema migrations)
- MapStruct 1.5.5 (entity/DTO mapping)
- Lombok
- Maven

## Getting Started

### Prerequisites

- Java 21+
- Maven 3.9+ (or use the included Maven Wrapper)
- Docker & Docker Compose (for the PostgreSQL database)

### Configuration

Connection and server settings live in `src/main/resources/application.yaml`.
Defaults:

| Setting        | Default                                |
|----------------|----------------------------------------|
| Server port    | `8202`                                 |
| Datasource URL | `jdbc:postgresql://localhost:5432/crm` |
| Username       | `crm_ACCOUNT`                          |
| Schema         | `crm_ACCOUNT`                          |

Liquibase runs on startup using `classpath:db/changelog/master.xml` to create the
`ACCOUNTS` table, sequence, and indexes. JPA `ddl-auto` is `none` — the schema is
owned entirely by Liquibase.

### Database Setup

A `docker-compose.yml` at the project root starts a PostgreSQL 16 instance
(container `postgres_db`) with a superuser `admin` / `admin` and a `crm`
database, exposed on port `5432`:

```bash
docker compose up -d
```

The compose file only creates the `admin` superuser and `crm` database. The
application connects as a dedicated `crm_ACCOUNT` role that owns its own schema,
so after the container is up, create the role and schema once:

```sql

CREATE USER crm_ACCOUNT
  WITH PASSWORD 'admin';

CREATE SCHEMA crm_ACCOUNT
  AUTHORIZATION crm_ACCOUNT;

```
Run them against the `crm` database.

With the role and schema in place, Liquibase can create its tracking tables and
apply the changelog on the next application start.

### Build & Run

```bash
# Run the app
./mvnw spring-boot:run

# Build a jar
./mvnw clean package

# Run tests
./mvnw test
```

The service starts on http://localhost:8202.

## API

Base path: `/api/ACCOUNTs`

| Method   | Path                      | Description                | Success |
|----------|---------------------------|----------------------------|---------|
| `GET`    | `/api/ACCOUNTs`          | List all ACCOUNTs         | 200     |
| `GET`    | `/api/ACCOUNTs/{refNo}`  | Get a ACCOUNT by ref. no. | 200     |
| `POST`   | `/api/ACCOUNTs`          | Create a ACCOUNT          | 201     |
| `PUT`    | `/api/ACCOUNTs/{refNo}`  | Update a ACCOUNT          | 200     |
| `DELETE` | `/api/ACCOUNTs/{refNo}`  | Delete a ACCOUNT          | 204     |

## Docker

A `Dockerfile` is provided that packages the built JAR on top of an
`eclipse-temurin:21-jre-alpine` base image. Build the JAR first, then the image:

```bash
./mvnw clean package
docker build -t crm-ACCOUNT .
```

Run the container, mapping the service's port (8202):

```bash
docker run --rm -p 8202:8202 crm-ACCOUNT
```

## CI/CD

A `Jenkinsfile` defines a declarative pipeline that:

- **Build** — runs `mvn clean package -DskipTests` and archives the resulting JAR.
- **Test** — runs `mvn test` and publishes the JUnit surefire reports.
- **Docker Build & Push** — logs in to Docker Hub, builds the image tagged with
  the Jenkins `BUILD_NUMBER`, and pushes it, then removes the local image
  afterward.

The pipeline requires JDK 21 (`jdk-21`) and Maven (`maven-3.9`) tool
installations configured in Jenkins, plus a `docker-hub-credentials`
username/password credential. Update the `DOCKER_HUB_USER` build parameter (or
its default) in the `Jenkinsfile` to your own Docker Hub account.

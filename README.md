# Simple System GmbH Test Project: Done by Hryhorii Yershov

### todo-service:1.0.0

Test task for Java candidate:

[Test task](doc/test_task.pdf)

Contract-first Spring Boot 4 backend implementing a Todo service with an in-memory H2 database.

### Contract-first approach

- **Source of truth**: All REST contracts are defined in `doc/API.yml` (OpenAPI 3.0.3).
- **Generation**: `openapi-generator-maven-plugin` generates Spring MVC interfaces and DTO models under `target/generated-sources/openapi`.
- **Implementation**:
  - Controllers (e.g. `TodosController`) implement the generated `TodosApi` interface.
  - No manual `@RequestMapping` annotations are written in controllers; mappings come from the generated interface.
  - Controllers work only with generated DTOs; a mapper layer converts between DTOs and internal JPA entities.

### Build and test

- **Build**:

```bash
mvn clean package
```

- **Run tests only**:

```bash
mvn test
```

The OpenAPI interfaces and models are generated automatically during the Maven build in the `generate-sources` phase.

### Running the application

```bash
mvn spring-boot:run
```

The service listens on port `8080`.

### Scheduler configuration

- A scheduler periodically marks `NOT_DONE` items with `dueAt < now` as `PAST_DUE`.
- Configuration is in `src/main/resources/application.yml`:
  - `todo.scheduler.enabled` (default `true`)
  - `todo.scheduler.fixedDelay` (default `60000` ms)
- In tests, the scheduler is disabled via `todo.scheduler.enabled=false`.

### Docker

Build the Docker image (multi-stage build):

```bash
docker build -t todo-service:1.0.0 .
```

Run the container:

```bash
docker run --rm -p 8080:8080 todo-service:1.0.0
```
### Assumptions

- No authentication or authorization is required.
- Time-based operations rely on the JVM default timezone (configurable via the `Clock` bean if needed).
- H2 is used as an in-memory store and is not intended for production persistence.


# Hello World Spring Boot

A simple Spring Boot application that displays "Hello World".

## Running the application

### Using Docker

Build and run with Docker:

```bash
docker build -t hello-world-spring .
docker run -p 8080:8080 hello-world-spring
```

Or use Docker Compose:

```bash
docker-compose up
```

Then visit http://localhost:8080/

### Using Maven

```bash
./mvnw spring-boot:run
```

Then visit http://localhost:8080/
 — Coding platform (MVP)

A lightweight Spring Boot backend for a coding website where users can register, log in, and read/write coding tutorials. This repository contains the basic MVP features so you can start building the more advanced pieces (code execution, contests, points/coins, chatbox, etc.) on top of it.

I implemented a minimal but functional backend that you can run locally and extend.

---

## Implemented (basic) features

- User registration and login
  - `POST /api/auth/register` — register new user
  - `POST /api/auth/login` — authenticate and receive JWT access token
  - `GET /api/auth/current-user` — get current authenticated user info
- JWT-based stateless authentication (Bearer token)
- Basic tutorial functionality
  - `GET /api/tutorials` — list published tutorials
  - `GET /api/tutorials/{id}` — view single tutorial (published or accessible to its author/admin)
  - `POST /api/tutorials` — create a tutorial (authenticated users)
  - `PUT /api/tutorials/{id}`, `DELETE /api/tutorials/{id}` — author/admin only
- Persistence with Spring Data JPA / Hibernate
- Development database: H2 in-memory (pre-configured)
- `DataInitializer` that seeds:
  - an admin user: `admin` / `admin123` (development convenience — change in production)
  - a sample tutorial
- Basic validation, DTOs, and service layers

Key code locations you will want to look at:

- Security: `com.example.daj2ee.config.SecurityConfig`, `com.example.daj2ee.security.JwtTokenProvider`
- Authentication controller: `com.example.daj2ee.controller.AuthController`
- Tutorials: `com.example.daj2ee.controller.TutorialController`, `com.example.daj2ee.service.TutorialService`
- JPA entities: `com.example.daj2ee.entity.User`, `com.example.daj2ee.entity.Tutorial`
- DB initializer: `com.example.daj2ee.config.DataInitializer`

---

## Tech stack

- Java (set to the project's `java.version`)
- Spring Boot (starter web, security, data-jpa)
- Spring Security (JWT)
- Spring Data JPA + Hibernate
- H2 (development DB; switch to MySQL/Postgres for production)
- Maven build (`./mvnw`)

---

## Quick start (local)

Prerequisites:

- Java (JDK 17+; the project pom sets `java.version`)
- Maven (or use the provided Maven wrapper `./mvnw`)

Run the application in dev mode:

```daj2ee/mvnw#L1-1
./mvnw spring-boot:run
```

Or build and run the jar:

```daj2ee/mvnw#L1-1
./mvnw -DskipTests package
java -jar target/daj2ee-0.0.1-SNAPSHOT.jar
```

Default server: `http://localhost:8080`

---

## Configuration

Important properties in `src/main/resources/application.properties`:

```src/main/resources/application.properties#L1-40
# DB (development)
spring.datasource.url=jdbc:h2:mem:daj2ee;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console

# JWT settings
app.jwtSecret=changeitsecretkeydontuseinprod
app.jwtExpirationMs=3600000
```

To switch to MySQL, add the MySQL driver to `pom.xml` and set:

```src/main/resources/application.properties#L1-40
spring.datasource.url=jdbc:mysql://localhost:3306/daj2ee
spring.datasource.username=your_user
spring.datasource.password=your_password
spring.jpa.hibernate.ddl-auto=update
```

For PostgreSQL, swap the URL/driver and credentials accordingly.

> Note: `app.jwtSecret` must be set to a secure value in production (environment variable or secret manager). The default is for development/testing only.

---

## How to use the API (examples)

Register a user:

```daj2ee/README.md#L1-40
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"alice","email":"alice@example.com","password":"Password123"}'
```

Login and get a token:

```daj2ee/README.md#L1-40
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"alice","password":"Password123"}'
# Response JSON example: { "accessToken": "<JWT>", "username":"alice", "roles":["ROLE_USER"], "expiresIn":3600000 }
```

Use the token to create a tutorial:

```daj2ee/README.md#L1-60
curl -X POST http://localhost:8080/api/tutorials \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <ACCESS_TOKEN>" \
  -d '{"title":"My First Tutorial","description":"Short intro","content":"...code and text...","language":"Java","published":true}'
```

Public endpoints:

- `GET /api/tutorials`
- `GET /api/tutorials/{id}`

Protected endpoints require `Authorization: Bearer <token>`.

H2 Console:

- URL: `http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:mem:daj2ee`
- User: `sa` / (no password by default)

---

## Notes & security

- Tokens are signed with `app.jwtSecret`. Keep it secret and rotate it in production.
- The default admin account is created by the initializer with credentials `admin` / `admin123` — this is for initial development only; please change or remove for production.
- The application is intentionally minimal to make it easier to iterate on features like code execution, points/coins, leaderboards, comments, contests, search, notifications, and chat support.

---

## Next steps (areas to implement)

When you're ready to add the "complicated" features, here's a suggested order:

1. Password reset (email integration)
2. Code execution and grading (integrate an execution engine — e.g., Judge0 or a sandboxed runner)
3. Points / coin system, daily check-in bonuses, and rewards
4. Comments, discussions, and solution sharing
5. Notifications and real-time chat suggestions (WebSocket or an external chat service)
6. Leaderboards, contest scheduled jobs, and ranking by runtime/score
7. Add integration tests and E2E tests, CI (GitHub Actions), and Docker / docker-compose for DB and runner

---

## Contributing

- Create a feature branch: `feat/<short-description>`
- Open a pull request against `main`
- Add tests for new features and update the README with usage notes

---

If you want, I can:

- Add Docker Compose (DB + app) and a `Dockerfile`
- Implement JWT refresh tokens and token revocation
- Start integrating a sandboxed code execution API (Judge0) and a simple submission model

Tell me which of the above you'd like me to implement next and I’ll start a new branch and open a PR with the initial changes.

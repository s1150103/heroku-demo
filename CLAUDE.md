# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

```bash
# Build
./mvnw clean package

# Run tests (uses H2 in-memory DB automatically)
./mvnw test

# Run a single test class or method
./mvnw test -Dtest=DemoApplicationTests
./mvnw test -Dtest=DemoApplicationTests#contextLoads

# Run locally with H2 (no PostgreSQL needed)
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

## Architecture

**Tech stack:** Spring Boot 3.5.12, Java 21, Maven, Spring Data JPA, Spring Security

**Database:**
- Production: PostgreSQL (configured via `DATABASE_URL` env var)
- Local dev: H2 in-memory DB via `application-local.properties` (profile `local`)
- Tests: H2 in-memory (no special config needed)
- H2 console available at `/h2-console` when running with `local` profile

**Deployment:** Heroku (via `Procfile`, runs the packaged JAR)

## Member Polymorphism System

The core domain model uses JPA SINGLE_TABLE inheritance. `Member` is an abstract `@Entity` with a `dtype` discriminator column in the `members` table. Three concrete subclasses:

- `Employee` (dtype=`EMPLOYEE`) — has `position` field; permissions: 閲覧・編集
- `PartTimer` (dtype=`PART_TIMER`) — has `weeklyHours` (int); permissions: 閲覧のみ
- `Admin` (dtype=`ADMIN`) — has `adminLevel` field; permissions: 閲覧・編集・削除・管理

All subclasses implement `getRoleLabel()` and `getPermissions()` from the abstract parent. `MemberService.findAllAsDTO()` calls these polymorphically to produce `MemberDTO` list responses.

`MemberRepository` uses a custom JPQL query (`TYPE(m) = :type`) to filter by subclass because `dtype` is a DB-internal column, not a JPA field.

**API:** `MemberController` at `/api/members` (GET / POST / DELETE /{id})

**MemberRequest.type** field accepts: `EMPLOYEE`, `PART_TIMER`, `ADMIN`. The `extra` field maps to position/weeklyHours/adminLevel depending on type.

## Spring Security

`SecurityConfig` defines:
- In-memory users: `admin`/`admin123` (ADMIN role), `user`/`user123` (USER role)
- GET `/api/members/**` → USER or ADMIN
- POST/DELETE `/api/members/**` → ADMIN only
- Basic Auth is used (no CSRF, no session)

Use `curl -u admin:admin123` or `curl -u user:user123` when testing endpoints manually.

## Legacy Domain

`Person`/`PersonRepository`/`PersonService`/`PersonController` and `Message`/`MessageRepository` are older domain classes that predate the Member system. `HelloController` serves the root endpoint.

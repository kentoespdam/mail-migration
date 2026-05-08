# Claude Instructions

Refer to `AGENTS.md` for tools and `memory.md` for full project specs.

## Build & Test

```bash
./gradlew clean build                            # Build
./gradlew test                                   # All tests
./gradlew test --tests <ClassName>               # Specific test
./gradlew bootRun                                # Run app
```

## Quick Reference
- **Stack**: Spring Boot 4.0.4, Java 25, JOOQ, JPA, MariaDB, Redis.
- **CQRS**: Write (`CommandService`) / Read (`QueryService`).
- **Auth**: AppWrite JWT + `UserTask` access validation.

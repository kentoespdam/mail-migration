# Claude Instructions

Read `AGENTS.md` for issue tracking, shell rules, session completion, and context discovery workflow.

## Build & Test

```bash
./gradlew clean build                            # Build
./gradlew test                                   # All tests
./gradlew test --tests AttachmentCommandServiceTest  # Specific test
```

## Architecture

**CQRS-lite**: Command (JPA) / Query (JOOQ) separation.
File storage via **AttachmentFileStorageService** → `mail/{yyyyMM}/`.
Access validated through **UserTaskQueryService**.

## Conventions

- **IDs**: Sqids for external (String) in Controller/DTO → decode to internal Long/Integer in Service
- **CQRS**: Write → `CommandService`, Read → `QueryService`
- **Soft Delete**: `status = 2` or `DELETED` string with `@SQLRestriction`
- **Caching**: Redis cache for frequently accessed data (e.g. attachment detail)
- **Storage**: New files → `mail/` folder (not `publik/`)
- **Validation**: Always validate mail access via UserTask before attachment operations

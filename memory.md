# 🧠 Memory — Mail Service (Spring Boot 4.0.4)

> **Package**: `id.perumdamts.mail` · **Stack**: Spring Boot 4.0.4 · Java 25 · GraalVM · JPA · Flyway · JOOQ · MariaDB 11.4 · Redis 7.4

---

## Stack & Integrations

| Component | Details |
|-----------|---------|
| **HR Service** | `http://192.168.1.214:8080` via OpenFeign · `userId == pegawaiId` |
| **Auth** | AppWrite v1.3.4 self-hosted `http://192.168.230.254:82` · project `65cd62cc3385d8434a53` · validation via `GET /v1/account` |
| **DB** | MariaDB `192.168.230.84:3307` · db `smartoffice_mail` · Flyway migrations |
| **Cache** | Redis `localhost:6379` · `hrEmployee` 60m · `mailFolder` 10m · `tenantConfig` 6h · `mailStats` 5m · `appwrite-tokens` 5m |
| **Tenant** | Single-instance · `TenantConfig` via `app.tenant.*` |
| **Storage** | `/data/attachments` · configurable via `storage.base-path` |

---

## Architecture Principles

- **CQRS-lite**: `CommandService` (JPA write) + `QueryService` (JOOQ read)
- **Layered**: Controller → Service → Repository · Domain Events via `@TransactionalEventListener` + `@Async`
- **Soft Delete**: `@SQLRestriction("status != 'DELETED'")` on all entities
- **Security**: `AppWriteAuthFilter extends OncePerRequestFilter` — JWT validation per request
- **Tenant**: Replace `CLIENT_CODE if-else` with `TenantConfig`
- **Virtual Threads**: `spring.threads.virtual.enabled: true` — avoid `synchronized` (use `ReentrantLock`)

---

## Enums

```java
enum RecordStatus       { ACTIVE, INACTIVE, DELETED }
enum MailStatus         { DRAFT(0), SENT(1) }
enum ArchiveStatus      { DRAFT(1), ARCHIVED(2), DELETED(3) }
enum PublicationStatus  { DRAFT, PUBLISHED, DELETED }
enum SystemFolder       { ROOT(1), INBOX(2), DRAFT(3), READ(4), SENT(5), DELETED(6), PERSONAL_ROOT(10), PURGED(-1) }
enum CirculationType    { DISPOSISI(1), MEMO_MANDIRI(2), MEMO(3), CC(4), REPLY(5), FORWARD(6) }
enum ReadStatus         { UNREAD(0), READ(1) }
enum AttachmentRefType  { MAIL, ARCHIVE, PUBLICATION }
enum CategoryStatus     { ACTIVE, INACTIVE }
```

---

## Package Structure

```
id.perumdamts.mail/
├── MailServiceApplication.java
├── config/                    # 11 configuration classes
│   ├── AppWriteProperties.java
│   ├── AppWritePropertiesConfig.java
│   ├── AsyncConfig.java
│   ├── CacheConfig.java
│   ├── JacksonConfig.java
│   ├── JooqConfig.java
│   ├── OpenApiConfig.java
│   ├── SecurityConfig.java
│   ├── StorageProperties.java
│   ├── TenantConfig.java
│   └── WebClientConfig.java
├── controller/
│   ├── core/                  # 6 REST controllers
│   │   ├── MailController.java
│   │   ├── MailFolderController.java
│   │   ├── MailRecipientController.java
│   │   ├── MailArchiveController.java
│   │   ├── AttachmentController.java
│   │   └── PublicationController.java
│   ├── master/                # 5 REST controllers
│   │   ├── MailTypeController.java
│   │   ├── MailCategoryController.java
│   │   ├── QuickMessageController.java
│   │   ├── DocumentTypeController.java
│   │   └── AllowedFileTypeController.java
│   └── GlobalExceptionHandler.java
├── dto/
│   ├── common/                # 6 pagination/sort DTOs
│   │   ├── PageRequest.java
│   │   ├── PagedRequest.java
│   │   ├── JpaPageRequest.java
│   │   ├── SortParam.java
│   │   ├── PagedResponse.java
│   │   └── JpaSearchRequest.java
│   ├── core/
│   │   ├── mail/              # Mail DTOs + Mapper
│   │   ├── archive/           # Archive DTOs + Mapper
│   │   ├── folder/            # Folder DTOs + Mapper
│   │   ├── recipient/         # Recipient DTOs + Mapper
│   │   ├── publication/       # Publication DTOs + Mapper
│   │   └── attachment/        # Attachment DTOs + Mapper
│   └── master/                # 15 DTOs/mappers
├── entity/
│   ├── core/                  # 11 JPA entities
│   │   ├── Mail.java
│   │   ├── UserTask.java
│   │   ├── MailRecipient.java
│   │   ├── MailArchive.java
│   │   ├── MailArchiveAccess.java
│   │   ├── ArchiveLocation.java
│   │   ├── Attachment.java
│   │   ├── AttachmentDownloadHistory.java
│   │   ├── PersonalFolder.java
│   │   ├── Publication.java
│   │   └── PrintLog.java
│   └── master/                # 5 JPA entities
│       ├── MailType.java
│       ├── MailCategory.java
│       ├── QuickMessage.java
│       ├── DocumentType.java
│       └── AllowedFileType.java
├── enums/                     # 9 enum types
├── event/                     # 10 event classes/listeners
│   ├── MailSentEvent.java
│   ├── ArchivePublishedEvent.java
│   ├── PublicationPublishedEvent.java
│   ├── MailNotificationListener.java
│   ├── MailStatisticListener.java
│   ├── MailResponseTimeListener.java
│   └── ArchivePublishedEventListener.java
├── integration/
│   └── hr/                    # 12 HR Service Feign client classes
├── repository/
│   ├── core/
│   │   ├── jpa/               # 10 JPA repositories
│   │   └── jooq/              # 5 JOOQ query repositories
│   └── master/
│       └── jpa/               # 5 JPA repositories
├── security/                  # 8 security classes
│   ├── AppWriteAuthFilter.java
│   ├── AppWriteTokenValidator.java
│   ├── MailPrincipal.java
│   ├── AppWriteUser.java
│   ├── CachedUserInfo.java
│   ├── AppWritePrefs.java
│   ├── AppWriteRole.java
│   └── UnauthorizedException.java
├── service/
│   ├── core/
│   │   ├── mail/              # CQRS split + numbering strategy
│   │   ├── archive/           # CQRS split + numbering
│   │   ├── folder/            # MailFolderService
│   │   ├── recipient/         # MailRecipientService
│   │   ├── attachment/        # AttachmentService
│   │   └── publication/       # PublicationCommand/QueryService
│   └── master/                # 3 master data services
└── util/                      # 2 utility classes
```

---

## Modules & Tables

| # | Module | Tables | Priority |
|---|--------|--------|----------|
| 1 | MailType | `mail_type` | F2 🔴 |
| 2 | MailCategory | `mail_category` | F2 🔴 |
| 3 | QuickMessage | `pesan_singkat` | F4 🟡 |
| 4 | MailFolder | `mail_folder`, `sys_user_task` | F3 🔴 |
| 5 | Mail Core | `mail`, `sys_user_task` | F3 🔴 |
| 6 | MailRecipient | `mail_recipient` | F3 🔴 |
| 7 | Attachment | `attachments`, `attachment_download_history` | F4 🟡 |
| 8 | MailArchive | `mail_archive`, `mail_archive_access`, `archive_location` | F5 🟡 |
| 9 | Archive Notif | `mail_archive_notif`, `mail_archive_notif_log` | F5 🟡 |
| 10 | Publication | `publication`, `document_type`, `allowed_file_type` | F5 🟡 |
| 11–14 | Auxiliary | `mail_respontime`, `print_log`, `*_statistic` | F6 🟢 |

---

## Flyway Versions

| Ver | File | Status |
|-----|------|--------|
| V1 | `V1__baseline_schema.sql` | Baseline schema (384 lines) |
| V2 | `V2__master_data_migration.sql` | Master data + sys_reference + charset utf8mb4 |
| V3 | `V3__add_missing_columns.sql` | Missing columns for archive/mail |
| V4 | `V4__archive_enhancements.sql` | Archive feature additions |
| V5 | `V5__mail_type_schema_improvements.sql` | Mail type improvements |
| V6 | `V6__pesan_singkat_improvements.sql` | Quick message improvements |
| V7 | `V7__mail_recipient_constraints.sql` | Recipient constraints |
| V8 | `V8__publication_schema.sql` | Publication module |
| V99 | `V99__data_migration.sql` | Idempotent data fixes (root_id, typos, trial exclusion) |

---

## REST API

### Mail Endpoints
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/mails` | Create draft |
| POST | `/api/v1/mails/send` | Create and send |
| PUT | `/api/v1/mails/{id}` | Update draft |
| POST | `/api/v1/mails/{id}/send` | Send draft |
| DELETE | `/api/v1/mails/{id}` | Soft delete |
| POST | `/api/v1/mails/{id}/restore` | Restore from trash |
| POST | `/api/v1/mails/{id}/read` | Mark as read |
| GET | `/api/v1/mails/{id}/thread` | Get thread |
| GET | `/api/v1/mails/search` | Global search |
| GET | `/api/v1/mails/report` | Statistics report |

### Folder Endpoints
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/mail/folders` | Get folder tree |
| GET | `/api/v1/mail/folders/counters` | Get badge counters |
| GET | `/api/v1/mail/folders/{id}/mails` | List mails in folder |
| POST | `/api/v1/mail/folders` | Create personal folder |
| PUT | `/api/v1/mail/folders/{id}` | Rename folder |
| DELETE | `/api/v1/mail/folders/{id}` | Delete folder |
| PUT | `/api/v1/mail/folders/move` | Move mails |
| POST | `/api/v1/mail/folders/empty-trash` | Empty trash |

### Recipient Endpoints
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/mails/{id}/recipients` | List recipients |
| POST | `/api/v1/mails/{id}/recipients` | Add recipient |
| DELETE | `/api/v1/mails/{id}/recipients/{rid}` | Remove recipient |
| POST | `/api/v1/mails/{id}/recipients/batch` | Batch add recipients |
| PATCH | `/api/v1/mails/{id}/recipients/{rid}` | Update recipient |
| POST | `/api/v1/mails/{id}/recipients/copy-from/{refId}` | Copy from reference |

### Archive Endpoints
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/archives` | Create draft archive |
| PUT | `/api/v1/archives/{id}` | Update draft |
| POST | `/api/v1/archives/{id}/publish` | Publish (DRAFT→ARCHIVED) |
| DELETE | `/api/v1/archives/{id}` | Delete archive |
| PUT | `/api/v1/archives/{id}/access` | Set ACL |
| GET | `/api/v1/archives/{id}/access` | Get ACL |
| GET | `/api/v1/archives/search` | ACL-based search |
| GET | `/api/v1/archives/report` | Archive report |

### Attachment Endpoints
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/attachments` | Upload attachment |
| GET | `/api/v1/attachments/{id}` | Get attachment info |
| GET | `/api/v1/attachments/{id}/download` | Download file |
| DELETE | `/api/v1/attachments/{id}` | Soft delete |

### Publication Endpoints
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/publications` | Create publication |
| PUT | `/api/v1/publications/{id}` | Update publication |
| POST | `/api/v1/publications/{id}/publish` | Publish |
| DELETE | `/api/v1/publications/{id}` | Delete |
| GET | `/api/v1/publications/{id}` | Get by ID |
| GET | `/api/v1/publications/search` | Search publications |

### Master Data Endpoints
| Resource | CRUD Endpoints |
|----------|----------------|
| MailType | `GET\|POST\|PUT\|DELETE /api/v1/mail-types` |
| MailCategory | `GET\|POST\|PUT\|DELETE /api/v1/mail-categories` |
| QuickMessage | `GET\|POST\|PUT\|DELETE /api/v1/quick-messages` |
| DocumentType | `GET\|POST\|PUT\|DELETE /api/v1/document-types` |
| AllowedFileType | `GET\|POST\|PUT\|DELETE /api/v1/allowed-file-types` |

---

## Auth — AppWrite JWT

```java
// Filter: validate every request
AppWriteUser user = webClient.get().uri("/v1/account")
    .header("X-Appwrite-Project", projectId)
    .header("X-Appwrite-JWT", token).retrieve()
    .bodyToMono(AppWriteUser.class).block();

// User record — roles in prefs.roles (NOT labels)
record AppWriteUser(@JsonProperty("$id") String id, String name, String email, AppWritePrefs prefs) {}
record AppWritePrefs(List<String> roles)  // ["USER","ADMIN","SYSTEM"]

// Cache: Redis "appwrite-tokens", key = token.substring(0,20), TTL 5m
```

---

## Tenant Config

```yaml
app:
  tenant:
    code: PERUMDAM_MTS
    display-name: "PDAM Musi Timur Selatan"
    office-code: MTS
    mail-number-format-ref: mail_number_format_mts
    archive-number-format-ref: ma_number_format_mts
    inbox-sort-ascending: false
    default-mail-type-id: 1
    default-mail-category-id: 1
```

---

## HR Service — Endpoints Used

| Endpoint | Purpose |
|----------|---------|
| `GET /pegawai?nama=...` | Recipient picker search |
| `GET /pegawai/{id}` | Lookup by userId login |
| `GET /pegawai/{id}/ringkasan` | Recipient card display |
| `GET /pegawai/{nipam}/nipam` | Lookup by NIPAM |
| `POST /pegawai/batch-by-ids` | Batch lookup nama recipient |
| `GET /master/jabatan` | Filter jabatan |
| `GET /master/organisasi/list` | Dropdown unit kerja |

> `emp_name`/`pos_name` in `mail_recipient` kept as fallback if HR Service unavailable.

---

## Service Layer (CQRS Pattern)

### Mail Services
| Service | Type | Responsibility |
|---------|------|----------------|
| `MailCommandService` | Write | Create/update draft, send, delete, restore |
| `MailQueryService` | Read | Search, thread, reports |
| `MailSendService` | Write | Send orchestration with 10 side-effects |
| `MailThreadService` | Read | Thread tree building |
| `MailTrackService` | Write | Read tracking |
| `MailSignatureService` | Read | Digital signature verification |

### Mail Numbering Strategy
| Class | Purpose |
|-------|---------|
| `MailNumberGenerator` | Strategy interface |
| `AbstractMailNumberGenerator` | Base template |
| `DefaultMailNumberGenerator` | Default format |
| `BmsMailNumberGenerator` | BMS tenant format |
| `SmdMailNumberGenerator` | SMD tenant format |
| `BpnMailNumberGenerator` | BPN tenant format |
| `MailNumberGeneratorDelegator` | Strategy router |

### Archive Services
| Service | Type |
|---------|------|
| `MailArchiveCommandService` | Write |
| `MailArchiveQueryService` | Read |

### Archive Numbering
| Class | Purpose |
|-------|---------|
| `ArchiveNumberGenerator` | Strategy interface |
| `DefaultArchiveNumberGenerator` | Default implementation |

### Other Core Services
| Service | Package | Purpose |
|---------|---------|---------|
| `MailFolderService` | `service/core/folder` | Folder CRUD, move mails |
| `MailRecipientService` | `service/core/recipient` | Batch recipient management |
| `AttachmentService` | `service/core/attachment` | File upload/download |
| `PublicationCommandService` | `service/core/publication` | Publication CRUD |
| `PublicationQueryService` | `service/core/publication` | Publication queries |
| `AllowedFileTypeService` | `service/core/publication` | File type validation |

### Master Services
| Service | Purpose |
|---------|---------|
| `MailTypeService` | Mail type CRUD |
| `MailCategoryService` | Mail category CRUD |
| `QuickMessageService` | Quick message CRUD |

---

## Cache Strategy (Redis)

| Cache Name | Key Pattern | TTL |
|------------|-------------|-----|
| `hrEmployee` | `hrEmployee::emp:{id}` | 60 min |
| `mailFolder` | `mailFolder::tree:{userId}` | 10 min |
| `tenantConfig` | `tenantConfig::tenant:{code}` | 6 hours |
| `mailStats` | `mailStats::user:{userId}` | 5 min |
| `appwrite-tokens` | `appwrite-tokens::{tokenPrefix}` | 5 min |

---

## Event-Driven Architecture

| Event | Listener | Purpose |
|-------|----------|---------|
| `MailSentEvent` | `MailSentEventListener` | Mail sent notification |
| `ArchivePublishedEvent` | `ArchivePublishedEventListener` | Archive publish notification |
| `PublicationPublishedEvent` | `PublicationPublishedEventListener` | Publication notification |
| N/A | `MailNotificationListener` | Email/push notifications |
| N/A | `MailStatisticListener` | Update mail statistics |
| N/A | `MailResponseTimeListener` | Track response time SLA |

All listeners use `@TransactionalEventListener(phase = AFTER_COMMIT)` + `@Async`

---

## Critical Issues (Must Fix During Implementation)

| # | Issue | Solution |
|---|-------|----------|
| B1 | SQL Injection in dynamic filters | JOOQ parameterized queries |
| B2 | `send()` non-atomic race condition | `@Transactional` + `SELECT FOR UPDATE` |
| B3 | Temp ID collision for drafts | UUID session-scoped IDs |
| B4 | `restore_folder_id` from client | Fetch from DB |
| B5 | `mark_notified()` without WHERE | Scoped update |
| B6 | `set_access()` non-atomic | `@Transactional` |
| B7 | `save()` archive multi-op non-atomic | `@Transactional` |
| B8 | SQL Injection in date fields (archive) | JOOQ parameterized |
| B9 | N+1 query problem | JOOQ window functions |
| B10 | No auth/ownership check (archive) | `@PreAuthorize` + office_code validation |

---

## sys_reference — Decisions

| code | Decision |
|------|----------|
| `sirkulasi` | ✅ Java Enum `CirculationType` |
| `emp_flag` | ➡️ From HR Service |
| `mail_number_format_*` | ✅ `TenantConfig` + table |
| Others | ⏭️ Skip (out of scope) |

---

## Implementation Tips

- `MailCategory`: `@Formula codeName` · unique(code+type)
- `QuickMessage`: `@Cacheable("tenantConfig")` Redis
- `MailFolder`: JOOQ for `FolderCounterRepository`, 2-level soft delete (trash first, then purge)
- `MailRecipient`: Remove denormalization `emp_name`/`pos_name` (but keep as fallback)
- `MailArchive`: `@Embedded ArchiveLocation`
- Domain events: statistics + notifications async via `@TransactionalEventListener` + `@Async`
- Mail numbering: Strategy pattern per tenant (`MailNumberGenerator`)
- Virtual threads: Avoid `synchronized` blocks (causes thread pinning) — use `ReentrantLock`

---

## Build & Run

```bash
# Navigate to mail-service directory
cd mail-service

# Start infrastructure (MariaDB, Redis, Mailhog, Adminer)
docker compose up -d

# Build the project
./gradlew clean build

# Run the application
./gradlew bootRun
```

### Access Points
| Service | URL |
|---------|-----|
| Application | `http://localhost:8081` |
| Adminer (DB GUI) | `http://localhost:8181` |
| Redis Commander | `http://localhost:8282` |
| Mailhog (SMTP catcher) | `http://localhost:8025` |

---

## Testing

```bash
# Run all tests
./gradlew test

# Run specific test class
./gradlew test --tests MailSendServiceTest

# Run with coverage
./gradlew clean build jacocoTestReport
```

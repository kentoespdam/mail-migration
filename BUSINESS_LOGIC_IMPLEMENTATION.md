# Mail Core Business Logic Implementation

Dokumentasi implementasi business logic Mail Core berdasarkan [planuml/index-mail-core.md](../../../docs-v2/business-logic/planuml/index-mail-core.md).

## Overview

Implementasi ini merupakan migrasi dari modul persuratan e-Office PHP (`mail.php` + `mailmodel.php`) ke Spring Boot dengan arsitektur yang lebih modular dan maintainable.

## Architecture Principles

1. **Separation of Concerns**: Command vs Query services (CQRS pattern)
2. **Event-Driven Architecture**: Domain events untuk side-effects async
3. **Strategy Pattern**: Tenant-specific implementations
4. **Transaction Safety**: `@Transactional` untuk core operations
5. **Async Processing**: `@Async` + `@TransactionalEventListener` untuk non-blocking operations

## Implemented Services

### 1. Mail Code Generator (Strategy Pattern)

**Location**: `service/mail/numbering/`

**Components**:
- `MailNumberGenerator` - Strategy interface
- `AbstractMailNumberGenerator` - Base class dengan common logic
- `BmsMailNumberGenerator` - Tenant BMS implementation
- `SmdMailNumberGenerator` - Tenant SMD implementation  
- `BpnMailNumberGenerator` - Tenant BPN implementation
- `DefaultMailNumberGenerator` - Fallback implementation
- `MailNumberGeneratorDelegator` - Delegator berdasarkan tenant config

**Bug Fix**: 
⚠️ Source PHP memiliki bug di mana blok SMD tidak menggunakan `else if`, sehingga di-override oleh BPN. 
Fix: Menggunakan Strategy Pattern dengan explicit `supports()` method.

**Transaction Safety**:
- `SELECT FOR UPDATE` untuk race condition prevention
- Sequence numbering unik per kombinasi mail_code + category + tahun

### 2. Mail Thread Service (Tree Building)

**Location**: `service/mail/MailThreadService.java`

**Algorithm**:
- In-memory tree building dari flat data
- 2-level fallback strategy untuk parent finding
- O(n) complexity dengan HashMap lookup

**Bug Fix**:
⚠️ Source PHP `find_node()` tidak explicit return FALSE.
Fix: Explicit return null jika parent tidak ditemukan.

**DTO Update**:
- `MailSummaryResponse` ditambahkan field `rootMailId` dan `parentMailId`

### 3. Mail Signature Service (Print Verification)

**Location**: `service/mail/MailSignatureService.java`

**Features**:
- Generate unique verification code (UUID instead of uniqid())
- Print log dengan IP address dan user agent tracking
- JSON response (bukan HTML seperti source PHP)
- Verification URL untuk QR code

**Entities**:
- `PrintLog` - Entity untuk print tracking
- `PrintLogRepository` - JPA repository
- `MailSignatureVerificationResponse` - DTO untuk verification response

### 4. Mail Track Service (Circulation Tracking)

**Location**: `service/mail/MailTrackService.java`

**Features**:
- Track mail circulation berdasarkan m_root_id
- Flat list (chronological) atau tree structure
- Content preview trimming utility

**Repository Update**:
- `MailQueryRepository.findThread()` updated untuk include rootMailId dan parentMailId

### 5. Mail Send Service (Event-Driven)

**Location**: `service/mail/MailSendService.java`

**10 Side-Effects**:
1. ✅ Validasi recipient
2. ✅ Generate nomor surat
3. ✅ Update status mail (DRAFT → SENT)
4. ✅ Create inbox per recipient (batch INSERT)
5. 🔄 Kirim email notification (async - TODO)
6. 🔄 Update statistik (async - TODO)
7. ✅ Move draft ke sent
8. ✅ Mark parent sebagai read (jika reply)
9. 🔄 Track response time (async - TODO)
10. ✅ Build toStr (recipient list)

**Event Listeners**:
- `MailNotificationListener` - Async email notification
- `MailStatisticListener` - Async statistic update
- `MailResponseTimeListener` - Async response time tracking

**Refactoring**:
- `MailCommandService.send()` di-refactor untuk delegate ke `MailSendService`
- Domain event `MailSentEvent` dipublish untuk async processing

### 5. Attachment Implementation (CQRS-lite)

**Location**: `service/core/attachment/`

**Components**:
- `AttachmentFileStorageService` - Storage handler using `mail/{yyyyMM}/` structure.
- `AttachmentCommandService` - Write operations (upload, delete) with access validation.
- `AttachmentQueryService` - Read operations (list, detail, download) with caching.
- `AttachmentQueryRepository` - JOOQ optimized queries.

**Key Logic**:
- **Storage Structure**: Files are stored in monthly folders (e.g., `mail/202604/`) with normalized filenames and numeric suffixes for collisions.
- **Access Validation**: Every operation validates that the user has access to the associated `Mail` via `UserTask`.
- **Soft Delete**: Attachments are soft-deleted by setting `status = 2` (Deleted).
- **Caching**: Attachment details are cached with key `attachments::detail:{userId}:{attachmentId}:v1` for 10 minutes.

**Endpoints**:
- `POST /api/v1/mails/{mailId}/attachments` - Upload
- `GET /api/v1/mails/{mailId}/attachments` - List
- `GET /api/v1/mails/{mailId}/attachments/{id}` - Detail
- `GET /api/v1/mails/{mailId}/attachments/{id}/download` - Download
- `DELETE /api/v1/mails/{mailId}/attachments/{id}` - Delete

## Migration Notes

### From PHP Monolith to Spring Boot Microservice

| PHP Function | Spring Boot Service | Status |
|-------------|---------------------|--------|
| `generate_code()` | `MailNumberGenerator.generate()` | ✅ Complete |
| `send()` | `MailSendService.send()` | ✅ Complete |
| `readFolder()` | `MailFolderService.getMailsInFolder()` | ✅ Existing |
| `make_nlevel_threaded()` | `MailThreadService.buildTree()` | ✅ Complete |
| `find()` / `find_mail()` | `MailQueryService.search()` | ✅ Existing |
| `directArsip()` | `MailArchiveService` | 📝 Separate module |
| `signMe()` + `checkSign()` | `MailSignatureService` | ✅ Complete |
| `trackMail()` / `track_mail()` | `MailTrackService.trackMail()` | ✅ Complete |
| `move()` + `restore()` + `empty_trash()` | `MailFolderService` | ✅ Existing |

### TODO Items

1. **Email Notification** (`MailNotificationListener`)
   - Integrate dengan HR service untuk mendapatkan email recipient
   - Implement SMTP / mail service integration
   - Add notification template

2. **Statistic Update** (`MailStatisticListener`)
   - Update mail category statistics
   - Update organization statistics
   - Implement aggregation (daily/weekly/monthly)

3. **Response Time Tracking** (`MailResponseTimeListener`)
   - Track mail read timestamp
   - Calculate response time metrics
   - SLA breach alerting

4. **Archive Service** (`MailArchiveService`)
   - 3-gate validation (role permission, duplicate check, attachment check)
   - Attachment copy functionality
   - See: `docs-v2/business-logic/planuml/index-mail-core.md#directarsip--arsip-langsung-dari-surat`

## Testing Recommendations

1. **Unit Tests**
   - Test each generator strategy independently
   - Test tree building algorithm dengan various scenarios
   - Test signature verification flow

2. **Integration Tests**
   - Test transaction rollback pada send() failure
   - Test async event listeners
   - Test race condition pada sequence generation

3. **Performance Tests**
   - Batch INSERT untuk inbox creation
   - Tree building untuk large thread (100+ mails)
   - Cache invalidation pada event listeners

## Configuration

### Tenant Configuration

```yaml
app:
  tenant:
    code: BMS  # atau SMD, BPN
    display-name: "Bandung Metro Selatan"
    office-code: BMS
    mail-number-format-ref: BMS_MAIL_NUMBER_FORMAT
```

### Async Configuration

Pastikan async enabled di application:

```java
@EnableAsync
public class MailServiceApplication {
    // ...
}
```

### Cache Configuration

Cache eviction sudah dikonfigurasi di `MailSentEventListener`:
- `mailStats` cache untuk statistics
- `mailFolder` cache untuk folder counters

## Future Enhancements

1. **Full-Text Search**: Integrate Elasticsearch untuk search performance
2. **Recursive CTE**: Alternative untuk thread tree building di database level
3. **QR Code**: Generate QR code untuk verification URL
4. **Batch Operations**: Optimize bulk mail operations
5. **Circuit Breaker**: Add resilience untuk external service calls (HR service, SMTP)

## References

- [Original PHP Source Analysis](../../../docs-v2/business-logic/planuml/index-mail-core.md)
- [PlantUML Diagrams](../../../docs-v2/business-logic/planuml/puml/)
- [Domain Model](../domain/entity/)

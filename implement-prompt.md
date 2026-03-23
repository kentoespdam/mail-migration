# Implement Prompts — Mail Service per Fase

> **Cara pakai**: Copy prompt fase yang sesuai, lampirkan file context yang disebutkan di bagian `ATTACH`, lalu kirim ke AI.

---

## 🔴 FASE 1 — Foundation & Setup

```
ROLE: Lead Engineer Spring Boot 4.0.x Microservice

CONTEXT (lihat memory.md):
- Package: id.perumdamts.mail · Stack: Spring Boot 4.0.x, Java 25, JPA, Flyway, JOOQ, MariaDB, Redis
- Auth: AppWrite v1.3.4 via REST (tidak ada Java SDK) — lihat planning-auth.md
- Cache: Redis RedisCacheManager — lihat planning-arch.md §Redis Caching Strategy
- Tenant: Single-instance, TenantConfig via application.yml

TASK: Implementasikan foundation proyek:
1. Buat struktur project Maven/Gradle sesuai package structure di planning-arch.md
2. Buat SecurityConfig + AppWriteAuthFilter (OncePerRequestFilter) + AppWriteProperties
3. Buat TenantConfig (@ConfigurationProperties record) + application.yml contoh
4. Buat CacheConfig (RedisCacheManager, TTL per cache) + AsyncConfig
5. Buat global exception handler (@RestControllerAdvice) dengan ApiError response standard
6. Buat HrServiceClient (OpenFeign) dengan @FeignClient + fallback dummy

OUTPUT: Kode lengkap semua file tersebut. Gunakan Java record untuk DTOs, @ConfigurationProperties record untuk config.

CONSTRAINT: Tidak boleh ada magic string. Semua konstanta di enum atau @ConfigurationProperties.
```

**ATTACH**: `memory.md` · `planning-arch.md` · `planning-auth.md`

---

## 🔴 FASE 2 — Master Data (MailType, MailCategory)

```
ROLE: Lead Engineer Spring Boot 4.0.x Microservice

CONTEXT (lihat memory.md):
- Package: id.perumdamts.mail · CQRS-lite: CommandService (JPA) + QueryService (JOOQ)
- Soft delete: @SQLRestriction("status != 'DELETED'") di semua entity
- RecordStatus enum: ACTIVE, INACTIVE, DELETED

TASK: Implementasikan modul Master Data:

1. MailType
   - Entity: id, name, description, RecordStatus status, @CreatedDate/@LastModifiedDate
   - @SQLRestriction("status != 'DELETED'")
   - Soft-delete: cek referential integrity ke mail_category sebelum delete
   - REST: GET /api/v1/mail-types, GET /api/v1/mail-types/{id}, POST, PUT, DELETE
   - GET /api/v1/mail-types/lookup → hanya ACTIVE, tanpa paging, untuk dropdown

2. MailCategory
   - Entity: id, code (unique per mailType), name, @Formula codeName ("code - name")
   - Relasi: @ManyToOne MailType
   - Unique constraint: (code, mail_type_id)
   - REST: GET /api/v1/mail-categories?typeId=, POST, PUT, DELETE

3. sys_reference seed (CirculationType)
   - Tambahkan ke V4__sys_reference_migration.sql jika belum ada
   - Buat Java enum CirculationType { DISPOSISI(1), MEMO_MANDIRI(2), MEMO(3), CC(4), REPLY(5), FORWARD(6) }

OUTPUT: Entity, Repository (JPA), Service (Command+Query), Controller, DTOs, Flyway seed SQL.
```

**ATTACH**: `memory.md` · `planning.md §F2`

---

## 🔴 FASE 3a — MailFolder & UserMailbox

```
ROLE: Lead Engineer Spring Boot 4.0.x Microservice

CONTEXT:
- SystemFolder enum: ROOT(1), INBOX(2), DRAFT(3), READ(4), SENT(5), DELETED(6), PERSONAL_ROOT(10), PURGED(-1)
- Soft delete 2-level: Trash dulu (folder 6), lalu Purge (hapus permanent)
- Counter badge: gunakan JOOQ window function (1 query, bukan N+1)
- Cache: mailFolder Redis 10 menit, @CacheEvict saat CRUD folder

TASK: Implementasikan MailFolder:
1. Entity PersonalFolder (id, name, parentId, userId, RecordStatus) + @SQLRestriction
2. FolderCounterRepository (JOOQ): hitung unread per folder dengan window function, return Map<Integer, FolderCountDto>
3. MailFolderService:
   - getFolderTree(userId): system folders + personal folders, inject counter
   - createFolder / renameFolder / deleteFolder (personal only)
   - moveMail(mailId, targetFolderId, userId)
   - deleteMail(mailId, userId): jika belum di trash → pindah ke DELETED(6); jika sudah di trash → purge
   - restoreMail(mailId, userId): kembalikan ke folder asal (ambil dari DB, bukan dari client — fix B4)
   - emptyTrash(userId): purge all di folder DELETED
4. REST endpoints sesuai planning.md §Folder & Mailbox
5. @CacheEvict("mailFolder") di semua mutasi

OUTPUT: Entity, JOOQ query, Service, Controller, DTOs lengkap.
CONSTRAINT: restore_folder_id HARUS diambil dari DB (sesuai issue B4 di planning-issues.md).
```

**ATTACH**: `memory.md` · `planning-issues.md` · `planning.md §F3`

---

## 🔴 FASE 3b — MailRecipient

```
ROLE: Lead Engineer Spring Boot 4.0.x Microservice

CONTEXT:
- CirculationType enum: DISPOSISI(1), MEMO_MANDIRI(2), MEMO(3), CC(4), REPLY(5), FORWARD(6)
- HR Service: http://192.168.1.214:8080, OpenFeign, userId == pegawaiId
- emp_name/pos_name di entity: PERTAHANKAN sebagai fallback, tapi isi dari HR Service saat add
- Cache HR: hrEmployee Redis 60 menit

TASK: Implementasikan MailRecipient:
1. Entity MailRecipient: mailId, employeeId, circulationType, isNotified, isRead, folderPosition,
   emp_name (denorm fallback), pos_name (denorm fallback)
2. MailRecipientService:
   - addRecipient(mailId, employeeId, type): lookup ke HR Service, simpan dengan denorm
   - batchAddRecipients(mailId, requests): parallel HR lookup, return BatchRecipientResponse (fix B14 silent failure)
   - copyRecipientsFrom(mailId, refMailId): copy untuk reply
   - copyThreadRecipients(mailId, refMailId): copy semua dari thread untuk reply-all
   - JOOQ findDistinctThreadRecipients(rootMailId): distinct recipients across thread
3. REST endpoints sesuai planning.md §Recipients

OUTPUT: Entity, Service, JOOQ query, Controller, DTOs. BatchRecipientResponse harus report per-item success/fail.
```

**ATTACH**: `memory.md` · `planning-issues.md §B14` · `analysis/08-hr-service-api-analysis.md`

---

## 🔴 FASE 3c — Mail Core

```
ROLE: Lead Engineer Spring Boot 4.0.x Microservice

CONTEXT (lihat memory.md + planning-issues.md untuk semua bug B1–B10):
- MailStatus: DRAFT(0), SENT(1)
- SystemFolder: ROOT(1)=1, INBOX(2), DRAFT(3), SENT(5)
- Domain event: MailSentEvent → async statistik + cache evict mailStats
- Nomor surat: Strategy pattern per tenant (TenantConfig.mailNumberFormatRef)

TASK: Implementasikan Mail Core:

1. Entity Mail: id, subject, body, senderId, mailTypeId, mailCategoryId, circulationType,
   mailStatus, folderId, rootMailId, parentMailId, mailNumber, createdDate, updatedDate
   @SQLRestriction("mail_status != 'DELETED'")

2. MailCommandService:
   - createDraft(req, principal): buat draft, simpan di DRAFT folder, return mailId
   - updateDraft(mailId, req, principal): update jika masih DRAFT
   - sendMail(mailId, principal): ATOMIC @Transactional —
     * generate nomor surat via MailNumberGenerator
     * set status SENT, set sentDate
     * pindahkan ke SENT folder sender
     * buat UserTask untuk semua recipients (taruh di INBOX)
     * publish MailSentEvent
     (Fix: B2 race condition, B3 temp ID collision)
   - deleteMail / restoreMail: delegasi ke MailFolderService

3. MailQueryService (JOOQ):
   - readFolder(folderId, userId, pageable): paginated, dengan unread flag
   - trackMail(mailId): full thread tree
   - findMails(filter, pageable): global search, JOOQ parameterized (fix B1)
   - getReport(filter): aggregasi untuk laporan

4. MailNumberGenerator: Strategy interface, implementasi default baca format dari TenantConfig → sys_reference

5. Domain event: MailSentEvent listener → @CacheEvict mailStats semua penerima

OUTPUT: Entity, CommandService, QueryService, NumberGenerator, EventListener, Controller, DTOs.
CRITICAL: sendMail() HARUS @Transactional (B2). JOOQ untuk semua filter query (B1).
```

**ATTACH**: `memory.md` · `planning-issues.md` · `planning-arch.md`

---

## 🟡 FASE 4 — QuickMessage & Attachment

```
ROLE: Lead Engineer Spring Boot 4.0.x Microservice

CONTEXT:
- QuickMessage: data semi-statis, cache agresif di Redis "tenantConfig" 6 jam
- Attachment: polymorphic — bisa milik mail ATAU mail_archive
- Download history: audit trail tiap download

TASK:

1. QuickMessage
   - Entity: id, title, content, RecordStatus
   - @Cacheable("tenantConfig") untuk GET /lookup (dropdown, hanya ACTIVE)
   - @CacheEvict saat create/update/delete
   - REST: CRUD + GET /api/v1/quick-messages/lookup

2. Attachment
   - Entity: id, fileName, fileSize, mimeType, filePath, ownerType(MAIL/ARCHIVE), ownerId
   - AttachmentService: upload (simpan file + record), download (stream + log history), delete (soft)
   - AttachmentDownloadHistory: attachmentId, downloadedBy, downloadedAt, ipAddress
   - REST: POST /api/v1/attachments (upload), GET /api/v1/attachments/{id}/download, DELETE

OUTPUT: Entity, Service, Controller. Upload via MultipartFile. Download via StreamingResponseBody.
```

**ATTACH**: `memory.md` · `planning.md §F4`

---

## 🟡 FASE 5 — Mail Archive

```
ROLE: Lead Engineer Spring Boot 4.0.x Microservice

CONTEXT:
- ArchiveStatus: DRAFT(1), ARCHIVED(2), DELETED(3)
- ArchiveLocation: @Embedded (rack, shelf, box, folder position)
- Access control: berdasarkan office_code (posisi jabatan yang bisa akses)
- Nomor arsip: Strategy pattern (ArchiveNumberGenerator), format dari TenantConfig.archiveNumberFormatRef
- Issue B6: set_access() HARUS @Transactional (delete lama + insert baru)
- Issue B7: save() archive multi-step HARUS @Transactional
- Issue B8: filter date fields pakai JOOQ (bukan string concat)
- Issue B9: report() belum ada → implement dengan JOOQ aggregation
- Issue B10: ownership check via @PreAuthorize + office_code

TASK: Implementasikan Mail Archive:
1. Entity MailArchive: id, title, archiveNumber, year, @Embedded ArchiveLocation,
   archiveStatus, officeCode, createdBy, publishedAt, @SQLRestriction
2. Entity MailArchiveAccess: archiveId, positionId (jabatan yang bisa akses)
3. MailArchiveCommandService (@Transactional):
   - createDraft, updateDraft, publishArchive (DRAFT→ARCHIVED + generate nomor)
   - deleteArchive (soft), setAccess (delete all + insert batch — atomic, fix B6)
4. MailArchiveQueryService (JOOQ):
   - findForAdmin(filter, pageable): semua arsip kantor ini
   - searchWithAcl(filter, pageable, positionIds): filter by access
   - getReport(filter): aggregasi per tahun/kategori (fix B9 — implement)
5. Archive Notif: @TransactionalEventListener ArchivePublishedEvent → kirim notif ke subscribers
6. REST endpoints sesuai planning.md §Archive

CONSTRAINT: Semua filter query via JOOQ. @PreAuthorize cek office_code ownership (fix B10).
```

**ATTACH**: `memory.md` · `planning-issues.md` · `planning.md §F5`

---

## 🟢 FASE 6 — Auxiliary & Reporting

```
ROLE: Lead Engineer Spring Boot 4.0.x Microservice

CONTEXT:
- MailResponseTime: hitung waktu respons (sentDate reply - createdDate mail asal)
- PrintLog: catat setiap cetak surat, UUID auth_code sebagai digital signature
- StatisticReport: JOOQ real-time query (gantikan pre-aggregated table)

TASK: Implementasikan modul auxiliary:

1. MailResponseTime
   - Entity: mailId, senderId, responseTimeMinutes, createdAt
   - Hitung & simpan via async event saat MailSentEvent (jika mail adalah reply)

2. PrintLog
   - Entity: mailId, printedBy, printedAt, authCode (UUID), ipAddress
   - PrintLogService.logPrint(mailId, principal): generate UUID authCode, simpan
   - REST: POST /api/v1/mails/{id}/print-log, GET /api/v1/mails/{id}/print-log

3. StatisticReport (JOOQ)
   - MailCategoryStatistic: count by category, date range → real-time JOOQ COUNT + GROUP BY
   - MailOrgStatistic: count by unit kerja (join HR data), aggregasi
   - REST: GET /api/v1/reports/by-category?from=&to=, GET /api/v1/reports/by-org

OUTPUT: Entity, Service, Controller. StatisticReport HARUS pakai JOOQ (bukan query pre-aggregated table).
```

**ATTACH**: `memory.md` · `planning.md §F6`

---

## 🔵 FASE 7 — Data Migration (V99)

```
ROLE: Database Migration Specialist + Spring Boot Engineer

CONTEXT:
- DB: MariaDB smartoffice · Flyway · Semua tabel sudah ada schema dari V1–V8
- mail_recipient: ada kolom denormalisasi emp_name, pos_name yang sudah tidak relevan

TASK: Buat V99__data_migration.sql untuk normalisasi & fix data legacy:

1. SET m_root_id = m_id WHERE m_root_id IS NULL
   -- Surat lama tanpa root: set sendiri sebagai root thread

2. Fix typo pesan_singkat (3 record dengan content tidak valid) — cek dan perbaiki

3. Charset unification:
   ALTER TABLE ... CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   -- untuk semua tabel mail scope

4. Exclude mail_trial:
   DELETE FROM mail WHERE id IN (SELECT mail_id FROM mail_trial);
   -- ATAU: tandai dengan status DELETED, jangan benar-benar hapus

5. Verifikasi akhir:
   -- SELECT count(*) FROM mail WHERE m_root_id IS NULL; → should be 0
   -- SELECT count(*) FROM mail_recipient WHERE emp_name IS NULL; → acceptable

OUTPUT: File SQL V99__data_migration.sql yang idempoten (bisa dijalankan ulang tanpa error).
CONSTRAINT: Semua operasi harus bisa di-rollback (gunakan transaction). Tambahkan komentar per section.
```

**ATTACH**: `memory.md` · `planning-flyway.md`
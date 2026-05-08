# PRD: Migrasi Mail/Disposisi Legacy → Mail Service

**Konsolidasi 4 Branch Grilling**: Role-in-Context · Folder/UserTask · MailResponseTime SLA · Signature/Print-Verification

- Beads issue: `mail-service-t0v`
- Status: Draft (needs-triage)
- Target: Spring Boot 4.0.4 mail-service (`id.perumdamts.mail`)
- Source legacy: SmartOffice (`smartoffice@192.168.230.84:3307`)
- Bahasa: Bilingual ID/EN

---

## Problem Statement

Aplikasi legacy SmartOffice (PHP) mengelola 1.8M+ surat dinas dengan keterbatasan:

1. **Tidak ada flag "selesai" eksplisit** untuk disposisi — status emergent dari traversal pohon Mail (cek apakah recipient sudah membuat Mail child). Pengguna sulit tahu disposisi mana yang masih menunggu tindak lanjut.
2. **Cascading thread sangat dalam** (8–10 level, lebar 30+ unit) sulit diaudit. User harus klik berulang untuk telusuri thread.
3. **Folder pribadi (`mail_folder`)** memungkinkan kedalaman tak terbatas, banyak duplikat nama per parent, dan 609 folder berstatus `folder_status=3` (terhapus) masih direferensikan oleh `sys_user_task.folder_id` → orphan reference.
4. **`mail_respontime`** ada tapi minim agregasi — tidak ada laporan SLA per kategori/per unit/per pejabat. Tidak ada filter "Fwd:" sehingga reply otomatis (auto-prefix) mengotori metrik response time.
5. **Print verification** menggunakan PHP `uniqid()` 13-char yang sudah pernah collision (`61fb4ce3c6f80`). Tidak ada rate-limit pada endpoint verifikasi → potensi enumeration attack.
6. **Role-in-context belum eksplisit** — `MailPrincipal` belum membawa `activePosId`, sehingga visibility surat untuk pengguna multi-jabatan/Plt ambigu.
7. **Tidak ada audit historis sender** — saat pejabat mutasi, surat lama menampilkan jabatan saat ini, bukan jabatan saat surat dikirim.

## Solution

Migrasi data + aplikasi ke `mail-service` (Spring Boot 4.0.4, Java 25, MariaDB, Redis) dengan pendekatan **convention-over-schema-change** pada 6 tabel inti (`mail`, `mail_recipient`, `user_task`, `mail_folder`, `print_log`, `mail_respontime`). Skema 6 tabel inti **tidak boleh diubah** — kompatibilitas data lama dijaga via konvensi (mapping `@Column`, soft-delete sentinel, role-in-context derivasi runtime).

Solusi mencakup:

- **Disposisi/Status**: derivasi "selesai" via tree-traversal + cache Redis 5 menit per mail-thread.
- **Role-in-Context**: `MailPrincipal.activePosId` di-set dari JWT claim atau header `X-Active-Position`; visibility surat difilter berdasarkan posisi aktif.
- **Personal Folder**: validasi depth-3 cap, unique-per-parent, empty-check sebelum delete; system folders (INBOX/READ/SENT/DRAFT/DELETED/PURGED/ROOT/PERSONAL_ROOT) dibuat lazy per user.
- **Response Time SLA**: dashboard agregat first-reply-wins dengan filter "Fwd:" + percentile (p50/p90/p99).
- **Print Verification (no crypto in MVP)**: 16-char UUID hex `auth_code` + QR code; endpoint publik `/api/mails/verify-sign/{code}` rate-limited 30 req/min/IP; selalu balas 200 (valid/invalid) — tidak pernah 404/500 → cegah enumeration.
- **Migrasi**: Flyway V15–V30 (sudah pre-flight pada staging V14, 0 orphan/duplicate).

## User Stories

### A. Disposisi & Status (derivasi tree-traversal)

1. As a Direktur, I want to see all disposisi yang masih menunggu tindak lanjut bawahan, so that I can chase laggards before deadline.
2. As a Manajer, I want to mark sebuah disposisi sebagai "selesai" dengan membuat Mail child reply, so that status emergent terupdate otomatis tanpa flag manual.
3. As a Staf, I want to see deadline (`m_max_response_date`) jika ada, so that I prioritise pekerjaan — meskipun field ini opsional (hanya 11% surat punya deadline).
4. As a Sekretariat, I want to see thread cascading penuh dengan kedalaman 8–10 level, so that I can audit jalur disposisi.
5. As a User, I want subject anak otomatis di-prefix "Fwd:", so that thread mudah dikenali secara visual.
6. As an Auditor, I want soft-deleted records (`status=DELETED`) excluded by default, so that laporan tidak terkontaminasi data lama.

### B. Role-in-Context (multi-jabatan/Plt)

7. As a pejabat dengan rangkap jabatan, I want to switch active position via header/JWT claim, so that inbox saya menampilkan surat sesuai jabatan aktif saja.
8. As a Plt (Pelaksana Tugas), I want surat ditujukan ke posisi yang saya jabat sementara muncul di inbox saya, so that pekerjaan tidak tertahan.
9. As a User, I want melihat siapa pengirim asli pada saat surat dikirim (snapshot jabatan), so that audit historis akurat meskipun pengirim sudah mutasi.
10. As a Sistem, I want `MailPrincipal.activePosId` ter-set saat request masuk, so that semua query downstream konsisten.
11. As a Backend, I want HR cache `hrEmployee` (TTL 60m) ter-invalidate saat ada perubahan jabatan, so that role-in-context tidak basi.
12. As a Frontend, I want endpoint `/api/me/positions` mengembalikan list jabatan saya + flag `isPlt`, so that dropdown switcher tampil benar.

### C. Personal Folder Management

13. As a User, I want membuat folder pribadi maksimal 3 level dalam, so that struktur tidak liar.
14. As a User, I want validasi nama folder unik per parent, so that tidak ada duplikat membingungkan.
15. As a User, I want delete folder hanya jika kosong (tidak ada UserTask di dalamnya), so that tidak kehilangan referensi surat.
16. As a Sistem, I want system folders (INBOX/READ/SENT/DRAFT/DELETED/PURGED/ROOT/PERSONAL_ROOT) dibuat lazy saat user pertama login, so that tidak boilerplate seed.
17. As a Migrator, I want 609 legacy folder berstatus `folder_status=3` di-handle (skip atau migrate as DELETED) — keputusan final di triage.
18. As a User, I want counter badge per folder (total atau unread — keputusan final di triage), so that I tahu berapa item di tiap folder.

### D. Response Time SLA

19. As a Direktur Utama, I want laporan rata-rata response time per kategori surat, so that I dapat mengevaluasi kinerja unit.
20. As a Manajer, I want filter "Fwd:" auto-exclude dari metrik, so that auto-prefix reply tidak mengotori SLA.
21. As an Analyst, I want percentile p50/p90/p99, so that outlier kelihatan.
22. As a User, I want first-reply-wins logic (hanya reply pertama dihitung), so that SLA fair.
23. As a Sistem, I want event `MailSentEvent` → `MailResponseTimeListener` async, so that pencatatan tidak mem-block command path.
24. As a Reporter, I want default range laporan (bulan/kuartal/tahun — keputusan final di triage), so that dashboard load cepat.

### E. Print & Signature Verification

25. As a Pejabat, I want sign surat menghasilkan 16-char UUID hex `auth_code` + QR code, so that surat dapat diverifikasi publik.
26. As a Penerima fisik, I want scan QR code → endpoint publik mengembalikan ringkasan surat (nomor, tanggal, penandatangan, status), so that I yakin asli.
27. As a Security, I want endpoint `/api/mails/verify-sign/{code}` rate-limited 30 req/min/IP, so that brute-force/enumeration dicegah.
28. As a Security, I want endpoint selalu balas 200 (valid/invalid) — tidak pernah 404/500, so that attacker tidak bisa membedakan code-exists vs not-exists.
29. As an Auditor, I want IP penanda-tangan + IP pemverifikasi terekam (X-Forwarded-For chain handling), so that audit lengkap.
30. As a Pejabat, I want soft-deleted mail tidak bisa diverifikasi (status=DELETED → balas "tidak valid"), so that surat batal tidak diakui.
31. As a Migrator, I want 95k legacy `print_log` dengan 13-char `uniqid()` di-keep apa adanya (backfill 16-char di-skip dari MVP), so that scope MVP terkendali.
32. As a Sistem, I want crypto signature (BSrE BSSN vs PrivyID — keputusan final di triage) **out of scope MVP** — yang ada hanya print-verification, bukan tanda tangan elektronik tersertifikasi.

### F. Migrasi Data

33. As a DBA, I want Flyway V15–V30 jalan tanpa downtime signifikan pada 1.8M+ rows, so that production switch lancar.
34. As a DBA, I want pre-flight check (duplicate, orphan FK) sudah validated pada staging V14, so that risk migrasi terukur. (sudah: 0 orphan/duplicate)
35. As a DBA, I want backfill `mail_respontime` dari pasangan parent-child Mail historis (count exact = TBD di triage), so that dashboard SLA punya data dari hari pertama.
36. As an Ops, I want rollback plan per Flyway version, so that jika ada masalah dapat revert tanpa data loss.

## Implementation Decisions

### Modul (deep modules — extractable for isolation testing)

Deep module = encapsulates much functionality di balik interface sederhana, jarang berubah, mudah dites isolated.

1. **`MailSignatureService`** *(deep)* — `signMail(mailId, signerPosId)` + `verifySignature(authCode, requestIp)`. Encapsulates: 16-char UUID hex generation, soft-delete check, IP capture (X-Forwarded-For chain), rate-limit hook, "always 200" response shape. Interface kecil, logic dalam.
2. **`DispositionStatusDeriver`** *(deep)* — `deriveStatus(mailId)` → `{PENDING, IN_PROGRESS, DONE}`. Encapsulates: tree-traversal child existence check, soft-delete filter, Redis cache 5min.
3. **`PersonalFolderValidator`** *(deep)* — `validateCreate(parentId, name)`, `validateDelete(folderId)`. Encapsulates: depth-3 cap, unique-per-parent, empty-check (no UserTask inside).
4. **`MailNumberGenerator`** *(deep, sudah ada — perbaikan di issue `s31`)* — strategy pattern Default/BMS/SMD/BPN, `MAX(parsed_seq)` per-(YEAR, m_category) dengan SELECT FOR UPDATE.
5. **`ResponseTimeAggregator`** *(deep)* — `aggregate(filter)` → `{count, p50, p90, p99}`. Encapsulates: first-reply-wins, "Fwd:" filter, percentile calc.
6. **`MailRoleContextResolver`** *(deep, baru)* — resolve `activePosId` dari JWT/header → set di `MailPrincipal`. Encapsulates: Plt detection, multi-position fallback, HR cache lookup.

### Modul (shallow / orchestration)

- `MailCommandService`, `MailQueryService`, `MailFolderCommandService`, `MailFolderQueryService` — CQRS-lite split, orchestrator yang delegate ke deep modules + repository.
- `UserTaskCommandService`, `UserTaskQueryService` — akses validation untuk attachment/mail.
- `MailSignatureVerificationService` — controller-side wrapper untuk `MailSignatureService.verify`, handle public endpoint shape.
- `MailResponseTimeQueryService` — read-side untuk dashboard SLA.

### Event Listeners

- `MailSentEvent` → `MailResponseTimeListener` (async, `@TransactionalEventListener` AFTER_COMMIT)
- `ArchivePublishedEvent` → `ArchivePublishedEventListener` (perbaikan di issue `a3s`: wrong table, missing pos→user resolution, missing notif_log fan-out)
- `PublicationPublishedEvent` → existing listener
- `EmployeePositionChangedEvent` *(proposed, post-MVP)* → invalidate `hrEmployee` cache key `{employeeId}` + emit downstream

### Schema (NO change to 6 core tables)

Mapping legacy → service via `@Column` annotation pada entity. Soft-delete via `@SQLRestriction("status != 'DELETED'")`.

Legacy quirks yang dipertahankan:
- `mail_respontime.orig_date` & `reply_date` (TIDAK ada `created_at`)
- `mail_folder.folder_id` PK (bukan `id`)
- `mail.m_parent_id=0` sentinel root
- `mail.m_status` 99.2%=1 — soft-delete-like, BUKAN status disposisi
- `mail.m_max_response_date` 11% terisi — deadline OPSIONAL
- `sys_user_task` hanya punya `read_status (0/1)`, tidak ada `completed_at`
- Numbering scope per-(`YEAR(m_created_date)`, `m_category`)

### API Contract (highlights)

- `GET /api/mails/verify-sign/{code}` — public, rate-limited 30/min/IP, **selalu 200**:
  - valid → `{valid:true, mailNumber, signedAt, signerName, signerPosition, archiveStatus}`
  - invalid/deleted/not-found → `{valid:false, reason:"INVALID_OR_DELETED"}` (uniform message)
- `POST /api/mails/{id}/sign` — `@PreAuthorize`, body `{signerPosId}`, returns `{authCode, qrUrl}`
- `GET /api/me/positions` — list jabatan user + `isPlt` flag
- Header `X-Active-Position: {posId}` (optional override) → diproses `MailRoleContextResolver`

### Architectural Decisions

- **CQRS-lite**: write JPA, read JOOQ 3.20.1 — wajib dipertahankan per ADR.
- **Single-tenant**: `TenantConfig` via `app.tenant.*` properties, cache TTL 6h.
- **Caching**: Redis. `hrEmployee` 60m, `mailFolder` 10m, `tenantConfig` 6h, `mailStats` 5m, `disposisiStatus` 5m (baru).
- **Auth**: AppWrite JWT → `AppWriteAuthFilter` → `MailPrincipal` (extended with `activePosId`).
- **Numbering**: strategy pattern + `SELECT FOR UPDATE` (race-safe), `MAX(parsed_seq)` (bukan COUNT(*)), range BETWEEN (sargable).
- **No crypto in MVP**: print-verification only. Vendor crypto (BSrE BSSN free vs PrivyID paid) → resolve di triage, post-MVP.

### Migration (Flyway V15–V30)

Sudah ada plan 24 issues di-pecah ke beads + GH #31–#54 (lihat memory `plan24-issues`). Pre-flight script V15–V30 sudah dijalankan pada staging V14 — 0 orphan/duplicate. Catatan: staging DB saat ini hanya berisi master data; core tables (mail, attachments, dll) kosong → uji ulang pada staging penuh sebelum prod.

## Testing Decisions

### What makes a good test (in this codebase)

- **External behavior only** — tes interface publik service/controller, BUKAN field private/method internal.
- **Deterministic** — no flaky time-dependent atau order-dependent.
- **Real database via Testcontainers** untuk integration test (ada prior art di repo) — JANGAN mock JPA/JOOQ untuk service yang sentuh DB. Mock OK untuk unit test kelas algoritmik murni.
- **One assertion-area per test** — readable, mudah debug.
- **Bilingual test names** OK (ID/EN), tapi konsisten per file.

### Modul yang akan dites (priority order)

1. **`MailSignatureService`** *(WAJIB tes — security-sensitive)*
   - `signMail`: auth_code generation 16-char unique, soft-delete edge case (sign mail yang sudah deleted → reject)
   - `verifySignature`: valid case, invalid case, deleted-mail case (selalu return `valid:false`, never throw)
   - IP capture: X-Forwarded-For chain handling
   - Tipe: integration (Testcontainers) + unit untuk auth_code generator
2. **`DispositionStatusDeriver`** — tree traversal correctness (root with no child = PENDING; root with child but child not-deleted = IN_PROGRESS or DONE per logic), cache invalidation. Tipe: unit (with in-memory tree fixture) + integration.
3. **`PersonalFolderValidator`** — depth-3 cap, unique-per-parent, empty-check. Tipe: integration (real DB).
4. **`MailNumberGenerator`** *(perbaikan di `s31`)* — race-condition test (concurrent generate → no duplicate), per-(year,category) scope. Tipe: integration dengan parallel threads + Testcontainers MariaDB.
5. **`ResponseTimeAggregator`** — first-reply-wins, "Fwd:" filter exclusion, percentile correctness. Tipe: unit (with synthetic dataset).
6. **Verify endpoint integration test** — public route, rate-limit (30/min), always 200 contract. Tipe: `@SpringBootTest` + `MockMvc`.

### Prior art di codebase

- `PrintLogTest` — pattern untuk auth_code/print log (referensi gaya).
- `PublicationControllerTest` — pattern untuk integration test controller dengan AppWrite auth stub.
- `AttachmentCommandServiceTest` — pattern untuk command service + storage interaction.
- Pakai `@SpringBootTest(webEnvironment=RANDOM_PORT)` + Testcontainers untuk integration.

### Testing scope yang DI-SKIP MVP

- Performance/load test — punya environment terpisah, post-MVP.
- E2E browser test — frontend di-handle tim FE.
- Migration rollback test pada 1.8M rows — manual test pada staging penuh, bukan automated.

## Out of Scope

- **Crypto signature elektronik tersertifikasi** (BSrE BSSN / PrivyID) — vendor decision di-defer ke triage, implementasi post-MVP.
- **Backfill 95k legacy `print_log`** dari 13-char ke 16-char — keep apa adanya, biar legacy tetap verifiable dengan format lama.
- **SLA cron auto-escalation** (notif otomatis saat melewati deadline) — post-MVP.
- **Role-switcher UI** — frontend scope, mail-service hanya expose `/api/me/positions`.
- **Multi-signer / co-sign** — single-signer dulu di MVP.
- **Audit trail viewer UI** — data tersimpan di `print_log` + `mail_respontime`, viewer post-MVP.
- **Mobile push notification** — email/in-app sudah ada, push post-MVP.
- **AI summarization thread** — di-eksplorasi via Spring AI 2.0.0-M1, tapi bukan MVP.

## Further Notes

### Memori persisten yang relevan (bd memories)

- `legacy-disposisi-no-completion-flag` — derivasi status dari tree, kedalaman 8–10 level
- `mail-seq-scope-verified` — scope numbering per-(YEAR, m_category)
- `plan24-issues` — 24 beads issue + GH #31–#54 untuk Flyway V15–V30
- `pre-flight-script-v15-v30-run-on-staging` — 0 orphan/duplicate
- `cache-redis-pada-cacheconfig-pakai-genericjacksonjsonredisse` — Redis serializer caveat (jangan cache `Page<T>`, bungkus ke `PagedResult<T>`)
- `oq1-attachment-dup`, `oq2-archive-ref`, `oq3-org-stat`, `oq4-msg-template` — open Q yang sudah resolved untuk plan 24

### Beads dependencies

PRD ini bergantung pada (akan blocked-by):

- `mail-service-a3s` (P1) — Fix ArchivePublishedEventListener (wrong table, missing pos→user resolution, missing notif_log fan-out)
- `mail-service-s31` (P1) — Fix numbering placeholder bugs di `AbstractMailNumberGenerator` (`#MR#` roman, `#type#` letter, MailType load)
- `mail-service-lfe` (P3) — Rate-limit `/api/mails/verify-sign/{code}` (Bucket4j atau Redis token bucket, 30 req/min/IP)

Command untuk wire-up (jalankan saat triage approve):

```bash
bd dep add mail-service-t0v mail-service-a3s
bd dep add mail-service-t0v mail-service-s31
bd dep add mail-service-t0v mail-service-lfe
```

### Open Questions (resolve at triage)

1. [RESOLVED] **Plt model**: Menggunakan Opsi 1 (direct update `employee.emp_pos_id`). Konfirmasi DB legacy: 0 duplicate emp_id dan no assignment table. Lihat ADR 001.
2. [RESOLVED] **Personal folder migration**: Menggunakan strategi **Clean DB & Move Tasks**. Task yang terjebak di folder status=3 dipindahkan ke folder DELETED (ID 6), lalu folder status=3 di-hard-delete. Lihat ADR 004.
3. **Backfill `mail_respontime`**: dari pasangan parent-child Mail historis, count exact? — TBD, butuh `SELECT COUNT(*) FROM mail_respontime`, `MIN(orig_date)`, `MAX(orig_date)`.
4. **Filter "Fwd:" untuk SLA report**: heuristik `subject LIKE 'Fwd:%'` — apa cukup? Berapa persen reply yang ter-tag "Fwd:" di legacy? — TBD, butuh query `mail_respontime JOIN mail`.
5. **Default range laporan SLA**: month / quarter / year? — preferensi UX, tanya stakeholder.
6. **HR cache invalidation protocol**: HTTP webhook / Kafka / Redis pub-sub? — preferensi infra, tanya tim HR.
7. [RESOLVED] **Audit historis sender**: Menggunakan snapshot `employee` ke kolom JSON di `mail` (`m_sender_snapshot`) saat insert. Lihat [ADR 002](adr/002-sender-snapshot-strategy.md).
8. [RESOLVED] **Counter badge personal folder**: total items vs unread only? — Menggunakan strategi **Unread-First** (Unread untuk Inbox/Personal, Total untuk Sent/Draft). Lihat [ADR 005](adr/005-folder-counter-semantics.md).
9. **Crypto vendor**: BSrE BSSN (free, gov-mandated, PKI X.509) vs PrivyID (paid, e-meterai integration)? — keputusan post-MVP, tanya legal/procurement.

### Catatan teknis tambahan

- **Caveman mode** aktif — bahasa bilingual ID/EN sepanjang PRD dan implementasi.
- **No git ops** dari agent — stealth mode.
- **Beads-only task tracking** — tidak ada TodoWrite atau markdown task list di luar beads.
- File ini (`docs/PRD-migrasi-mail-disposisi.md`) adalah artifact reference — beads issue `mail-service-t0v` adalah single source of truth untuk status PRD.

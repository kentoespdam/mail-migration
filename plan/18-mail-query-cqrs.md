# Plan 18 — Implementasi CQRS untuk Modul Mail, UserTask, & Recipient

> **Tujuan**: memperjelas dan memperkuat penerapan CQRS-lite pada modul `Mail`,
> `UserTask`, dan `Recipient` agar batas Command/Query bersih, performa baca optimal,
> serta siap berkembang ke read-model terpisah bila kebutuhan tumbuh.
>
> **Terakhir diperbarui**: 2026-04-28 — disesuaikan dengan kondisi kode aktual.

---

## 1. Status CQRS Aktual

| Modul | Command (JPA) | Query (JOOQ) | Status | Catatan |
|-------|---------------|--------------|--------|---------|
| Mail | `MailCommandService` | `MailQueryService` | ⚠️ | Masih inject `MailRepository` & `AttachmentRepository` (JPA); `getDetail` tanpa cek akses & soft-delete |
| UserTask | `UserTaskCommandService` | `UserTaskQueryService` | ⚠️ | Mixed: inject `UserTaskRepository` (JPA) + DSLContext; resolveRootId inline |
| Recipient | `MailRecipientCommandService` | `MailRecipientQueryService` | ❌ | Masih inject `MailRecipientRepository` (JPA); tidak memiliki tracking/read-status |
| Mail (tracking) | — | `MailTrackService` | ✅ | Sudah pakai JOOQ via `MailQueryRepository` |
| Mail (thread tree) | — | `MailThreadService` | ✅ | Pure logic, tidak inject repository |
| Attachment | — | `AttachmentQueryRepository` | ✅ | JOOQ, sudah filter `status=1`, belum dipakai `MailQueryService` |
| Recipient (JOOQ) | — | `RecipientQueryRepository` | ✅ | Ada `findDistinctThreadRecipients`, belum dipakai `MailRecipientQueryService` |

**Kesimpulan**: Struktur layering sudah ada; beberapa komponen JOOQ baru (`AttachmentQueryRepository`,
`RecipientQueryRepository`) sudah tersedia tapi belum dihubungkan. Pelanggaran CQRS masih di 3 titik.

---

## 2. Tujuan Arsitektur

1. Setiap modul memiliki dua bounded service: `*CommandService` (JPA, transactional) dan
   `*QueryService` (JOOQ, `@Transactional(readOnly = true)`).
2. Query side **tidak boleh** meng-inject repository JPA, kecuali untuk lookup ringan referensial
   yang tidak terjangkau JOOQ dan bukan merupakan state dari aggregate utama.
3. Setiap `QueryService` hanya menjawab pertanyaan domain miliknya:
   - **MailQueryService** → detail surat, thread, search, report.
   - **UserTaskQueryService** → listing folder per user, counter, resolusi thread per user.
   - **MailRecipientQueryService** → tracking, read-status, daftar penerima per surat.
4. Komunikasi antar `QueryService` melalui nilai primitif atau DTO tipis — tidak tukar-menukar entity JPA.
5. Komponen JOOQ repository yang telah ada (`AttachmentQueryRepository`, `RecipientQueryRepository`)
   dipakai sepenuhnya oleh service yang relevan.

---

## 3. Pemetaan Tanggung Jawab Target

### 3.1 MailQueryService (trimmed)

| Method | Status | Keterangan |
|--------|--------|------------|
| `getDetail(mailId, userId)` | 🔧 Ubah | Tambah `userId`, pakai JOOQ + `AttachmentQueryRepository` |
| `getThread(mailId)` | ✅ Tetap | Sudah JOOQ, delegasi ke `MailTrackService` jika perlu |
| `search(request)` | ✅ Tetap | Sudah JOOQ |
| `getReport(request)` | ✅ Tetap | Sudah JOOQ |
| `lookup(userId, params)` | 🔧 Wrapper | Delegasi ke `UserTaskQueryService.findAll` — pertimbangkan apakah perlu dipertahankan atau controller langsung inject `UserTaskQueryService` |
| `findThreadTracking(mailId)` | 🔧 Pindah | Delegasi ke `MailRecipientQueryService` |
| `getTracking(mailId)` | 🔧 Pindah | Pindah ke `MailRecipientQueryService` |
| `getReadStatus(mailId)` | 🔧 Pindah | Pindah ke `MailRecipientQueryService` |

### 3.2 UserTaskQueryService (minor cleanup)

| Method | Status | Keterangan |
|--------|--------|------------|
| `findAll(userId, params)` | ✅ Tetap | Sudah JOOQ |
| `findThread(rootMailId)` | ✅ Tetap | Sudah JOOQ |
| `resolveRootId(mailId)` | 🔧 Review | Pertahankan di sini; hapus duplikasi di `MailQueryRepository.findThread` |
| `existsActive(userId, mailId)` | ✅ Tetap | Sudah JOOQ |
| `countUnread(userId, folderId)` | ✅ Tetap | Sudah JOOQ |
| `findUserTask(userId, mailId)` | ⚠️ Hapus | Satu-satunya yang masih inject `UserTaskRepository` JPA — evaluasi apakah bisa dihapus atau diganti JOOQ |

### 3.3 MailRecipientQueryService (diperluas)

| Method | Status | Keterangan |
|--------|--------|------------|
| `getRecipients(mailId)` | 🔧 Migrasi | Pindah dari JPA (`MailRecipientRepository`) ke `RecipientQueryRepository` atau JOOQ langsung |
| `getTracking(mailId)` | 🆕 Tambah | Pindahan dari `MailQueryService` via `MailQueryRepository` |
| `getReadStatus(mailId)` | 🆕 Tambah | Pindahan dari `MailQueryService` via `MailQueryRepository` |
| `findThreadTracking(mailId)` | 🆕 Tambah | Gabungkan `resolveRootId` + `findThread` + tracking |

---

## 4. Strategi Pemisahan

### 4.1 Tahap 1 — CQRS-lite Murni (target dekat, Sprint 1-2)

Berurutan sesuai prioritas:

1. **Perbaiki `getDetail`** (Plan 17 Tahap A):
   - Tambah parameter `userId`.
   - Ganti `MailRepository.findById` + `AttachmentRepository` dengan query JOOQ + `AttachmentQueryRepository.findByRef`.
   - Hapus injeksi JPA dari `MailQueryService`.

2. **Pindahkan tracking & read-status**:
   - Tambahkan `getTracking`, `getReadStatus`, `findThreadTracking` ke `MailRecipientQueryService`
     menggunakan JOOQ (query yang sudah ada di `MailQueryRepository` dapat
     dipindah atau diekstrak ke `RecipientQueryRepository`).
   - Hapus method-method tersebut dari `MailQueryService`.
   - Update `MailController`: inject `MailRecipientQueryService` untuk endpoint tracking & read-status.

3. **Migrasi `MailRecipientQueryService.getRecipients`**:
   - Ganti `MailRecipientRepository` JPA dengan `RecipientQueryRepository` (JOOQ) atau JOOQ langsung.
   - Hapus injeksi `MailRecipientRepository` JPA.

4. **Evaluasi `UserTaskQueryService.findUserTask`**:
   - Cek apakah pemanggil internal bisa menggunakan JOOQ `existsActive` sebagai gantinya.
   - Jika tidak, pertahankan tapi tandai sebagai technical debt.

5. **Konsolidasi resolveRootId**:
   - Hapus duplikasi inline di `MailQueryRepository.findThread` (panggil `resolveRootId` dari
     service, atau jadikan private method di repository).

### 4.2 Tahap 2 — Materialized Read Model (opsional, jangka menengah)

Bila volume read meningkat (laporan, search, dashboard):

- **Read table denormalized** `mail_view` yang di-update via event:
  - `MailSentEvent` → upsert `mail_view`.
  - `RecipientReadEvent` → update read counter.
  - `MailDeletedEvent` → mark inactive.
- **Cache layer** (`mailDetail:v2`, `mailThread:v2`, `mailTracking:v2`) dengan invalidasi event.
- **Search index** FULLTEXT/Elasticsearch untuk `searchMails`/`getReport`.

### 4.3 Tahap 3 — Event-Driven Projection (opsional, jangka panjang)

- Command menerbitkan event domain (sudah ada `MailSentEvent` dst).
- Listener async memproyeksikan ke read store (`mail_view`, `recipient_view`).

> Tahap 2 & 3 hanya dikejar bila metrik (latency, beban DB) menunjukkan tekanan; jangan
> dieksekusi prematur.

---

## 5. Boundary & Anti-Pattern yang Dihindari

- ❌ Query side inject repository JPA — kecuali `findUserTask` sebagai debt sementara.
- ❌ Query mengembalikan entity JPA langsung — selalu DTO/record.
- ❌ Service memanggil repository JPA milik modul lain.
- ✅ Komponen JOOQ yang sudah ada (`AttachmentQueryRepository`, `RecipientQueryRepository`) dipakai.
- ✅ `MailThreadService` tetap sebagai pure logic service (tidak inject repository).
- ✅ `MailTrackService` tetap sebagai facade tracking berbasis JOOQ.
- ✅ Cache name menggunakan suffix versi (`:v2`) sesuai konvensi yang sudah ada.

---

## 6. Dampak ke Komponen Lain

| Komponen | Dampak |
|----------|--------|
| `MailController` | Inject `MailRecipientQueryService` untuk `/tracking` & `/read-status`; update signature `getById` untuk pass `userId` ke `getDetail` |
| `MailAttachmentController` | Tidak terdampak langsung — sudah pakai `AttachmentQueryRepository` |
| `MailRecipientController` | Perlu diverifikasi — mungkin perlu inject `MailRecipientQueryService` baru |
| Event listener | Tidak terdampak Tahap 1; perlu event baru untuk cache invalidation di Tahap 2 |
| Tests | Perlu update test unit `MailQueryService` & tambah test `MailRecipientQueryService` |

---

## 7. Acceptance Criteria

1. Tidak ada `*QueryService` yang meng-import `repository.*.jpa.*` (kecuali `findUserTask` sebagai debt).
2. `MailQueryService` tidak lagi meng-import `MailRepository` & `AttachmentRepository`.
3. `MailRecipientQueryService` memiliki `getTracking`, `getReadStatus`, `findThreadTracking`.
4. `MailRecipientQueryService` tidak lagi meng-import `MailRecipientRepository` JPA.
5. `getDetail(mailId, userId)` menolak mail terhapus dan mail milik user lain.
6. Resolusi `rootId` tidak duplikasi di lebih dari satu lokasi.
7. Semua endpoint paginated di Query side mengembalikan `PagedResponse<>`.

---

## 8. Risiko & Mitigasi

| Risiko | Mitigasi |
|--------|----------|
| Perubahan signature `getDetail` memutus controller & FE | Update controller secara bersamaan; tidak butuh versioning endpoint |
| Pemindahan tracking/read-status mengubah routing | Verifikasi path tetap sama (`/mails/{id}/tracking`) |
| Konsolidasi thread mengubah urutan/struktur tampilan | Snapshot test sebelum refactor |
| `MailRecipientQueryService` menjadi fat service | Split ke sub-service jika perlu di masa depan |

---

## 9. Roadmap Eksekusi

1. **Sprint 1** — Tahap A Plan 17: `getDetail` fix (softdelete + auth + hapus JPA).
2. **Sprint 2** — Tahap 1 Plan 18: Pindah tracking/read-status ke `MailRecipientQueryService`,
   konsolidasi resolveRootId, migrasi `getRecipients` ke JOOQ.
3. **Sprint 3** — Standarisasi `PagedResponse<>`, unifikasi exception, cache thread & tracking.
4. **Backlog** — Read model materialized, search index, projection async (Tahap 2 & 3).

---

## 10. Hubungan dengan Plan Lain

- **Plan 17** — audit & quick fix `MailQueryService` (prasyarat Sprint 1).
- **Plan V10-mail-search-index.md** — optimasi indeks pendukung `search`/`report`.
- **Plan 15-mail-usertask-integration-lookup.md** — sudah memformalkan jalur UserTask→Mail
  untuk lookup; plan ini melengkapi sisi Mail & Recipient.

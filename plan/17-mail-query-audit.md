# Plan 17 — Audit & Perbaikan `MailQueryService`

> **Tujuan**: mengidentifikasi bug, celah keamanan, hambatan performa, dan code smell pada
> `MailQueryService` beserta dependensi terdekatnya, lalu menyusun rencana perbaikan
> bertingkat prioritas. Penjelasan ditulis high-level — tidak menyertakan kode.
>
> **Terakhir diperbarui**: 2026-04-28 — disesuaikan dengan kondisi kode aktual.

---

## 1. Konteks & Cakupan

- **File utama**: `src/main/java/id/perumdamts/mail/service/core/mail/MailQueryService.java`
- **Dependensi langsung** (kondisi aktual):
  - `repository/core/jooq/MailQueryRepository` ✅ (JOOQ)
  - `repository/core/jpa/MailRepository` ⚠️ (JPA — harusnya di Query side)
  - `repository/core/jpa/AttachmentRepository` ⚠️ (JPA — harusnya di Query side)
  - `service/core/usertask/UserTaskQueryService`
  - `service/core/mail/MailMapper`
- **Modul terkait**: `MailTrackService`, `MailThreadService`, `UserTaskQueryService`,
  `MailRecipientQueryService`, `RecipientQueryRepository`, `AttachmentQueryRepository`.

---

## 2. Koreksi Temuan Lama vs. Kondisi Aktual

| Temuan di Plan Lama | Status Aktual |
|---------------------|---------------|
| Method signature mismatch `findByRefTypeAndRefId` vs `findAllByRefTypeAndRefId` | **TIDAK VALID** — `AttachmentRepository` mendefinisikan **keduanya** (`findByRefTypeAndRefId` dan `findAllByRefTypeAndRefId`), jadi tidak ada compile error. Namun pemanggilan via JPA tetap melanggar CQRS. |
| Soft-delete leak di `getDetail` | **VALID** — `mailRepository.findById(mailId)` tidak memfilter status. |
| `getDetail` tidak memverifikasi kepemilikan | **VALID** — tidak ada pengecekan `userId` di `getDetail(Long mailId)`. |
| JPA dipakai di Query side | **VALID** — `MailRepository` & `AttachmentRepository` masih di-inject. |
| `AttachmentQueryRepository` (JOOQ) sudah ada | **BARU DITEMUKAN** — sudah tersedia `AttachmentQueryRepository.findByRef(refType, refId)` yang lebih lengkap (filter `status=1`). `MailQueryService` belum menggunakannya. |
| Duplikasi logika thread | **VALID SEBAGIAN** — `MailQueryRepository.findThread()` dan `UserTaskQueryService.findThread()` keduanya traverse thread namun mengembalikan tipe berbeda (`MailSummaryResponse` vs `MailTrackingItemResponse`). Tujuannya memang berbeda (tampilan vs tracking), tapi resolusi `rootId` tersebar. |
| `MailTrackService` & `MailThreadService` sudah ada | **BARU DITEMUKAN** — `MailTrackService` sudah memisahkan logika pelacakan sirkulasi dari `MailQueryService`. `MailThreadService` sudah membangun tree. Ini mengurangi duplikasi yang dikhawatirkan. |
| Tiga jenis wrapper | **VALID** — `Page<>` di `lookup`, `PagedResponse<>` di `search`/`report`, `List<>` di thread/tracking. |
| `MailRecipientQueryService` masih menggunakan JPA | **VALID** — masih menggunakan `MailRecipientRepository` (JPA), belum menggunakan `RecipientQueryRepository` (JOOQ). |

---

## 3. Ringkasan Temuan (Diperbarui)

| Kategori | Temuan | Prioritas |
|----------|--------|-----------|
| Bug correctness | Soft-delete leak di `getDetail` | P0 |
| Authorization gap | `getDetail` tanpa cek akses user | P0 |
| CQRS violation | `MailRepository` & `AttachmentRepository` di-inject di Query side | P1 |
| CQRS violation | `MailRecipientQueryService` menggunakan JPA, padahal `RecipientQueryRepository` (JOOQ) sudah tersedia | P1 |
| Duplikasi resolusi rootId | `resolveRootId` tersebar di `UserTaskQueryService` dan `MailQueryRepository.findThread` | P1 |
| Aset tidak digunakan | `AttachmentQueryRepository.findByRef` (JOOQ) ada tapi tidak dipakai oleh `MailQueryService` | P1 |
| Konsistensi response | `lookup` mengembalikan `Page<>` (dibungkus `PagedModel<>`), `search`/`report` masih `PagedResponse<>` (custom) — perlu diseragamkan ke `PagedModel<>` Spring | P2 |
| Error handling | `EntityNotFoundException` di `getDetail`, modul lain mungkin menggunakan exception domain sendiri | P2 |
| Cache | `getThread`, `getTracking`, `getReadStatus` belum di-cache | P3 |
| Validasi input | Tidak ada `@Valid` di parameter query controller | P3 |
| Naming | Campuran `get*` vs `find*` vs `lookup` vs `search` | P3 |

---

## 4. Temuan Detail

### 4.1 Bug & Correctness

1. **Soft-delete leak** — `mailRepository.findById(mailId)` memuat record tanpa filter
   `status != DELETED`. Padahal seluruh query JOOQ sudah memfilter status. Akibatnya endpoint
   detail dapat mengakses mail yang sudah dihapus atau berstatus DRAFT milik user lain.
   **Fix**: Gunakan `AttachmentQueryRepository` + query JOOQ baru `findDetailWithAttachments`
   yang memfilter status aktif.

### 4.2 Authorization Gap

2. **`getDetail` tidak memverifikasi kepemilikan** — signature `getDetail(Long mailId)`
   tidak menerima `userId`. Padahal controller menerima `MailPrincipal` dan bisa meneruskan
   `userId`. Tidak ada pengecekan apakah user is sender / recipient / memiliki UserTask.
   **Fix**: Tambahkan parameter `userId` + pengecekan via `UserTaskQueryService.existsActive`
   atau query JOOQ langsung.

### 4.3 Pelanggaran CQRS-lite

3. **`MailRepository` & `AttachmentRepository` (JPA)** di-inject di `MailQueryService`.
   `AttachmentQueryRepository` (JOOQ) sudah tersedia dan sudah mem-filter `status=1`.
   **Fix**: Ganti dengan `AttachmentQueryRepository.findByRef` + query JOOQ baru untuk
   mail detail. Hapus injeksi kedua JPA repository dari `MailQueryService`.

4. **`MailRecipientQueryService` masih menggunakan JPA** (`MailRecipientRepository`),
   padahal `RecipientQueryRepository` (JOOQ) sudah ada. Saat ini `MailRecipientQueryService`
   hanya memiliki `getRecipients(mailId)` — sangat terbatas.
   **Fix**: Pindahkan `getTracking` dan `getReadStatus` dari `MailQueryService` ke sini,
   gunakan `RecipientQueryRepository` atau JOOQ langsung; hapus `MailRecipientRepository` JPA.

### 4.4 Duplikasi Resolusi rootId

5. **`resolveRootId` tersebar** — ada di `UserTaskQueryService.resolveRootId()` (JOOQ),
   dan juga diulang secara inline di `MailQueryRepository.findThread()`.
   **Fix**: Jadikan `resolveRootId` sebagai utility private di `MailQueryRepository`
   atau helper domain, dan gunakan dari satu titik.

### 4.5 Konsistensi Response

6. **`lookup` sudah mengembalikan `PagedModel<>`** (karena controller wrap dengan `new PagedModel<>(page)`),
   sedangkan `search`/`report` mengembalikan custom `PagedResponse<>` dan `archive`/`publication`
   juga masih pakai `PagedResponse<>`. Ini tidak konsisten dengan pola master.
   **Rekomendasi**: seragamkan ke `PagedModel<>` (Spring) — service return `Page<T>`,
   controller wrap dengan `new PagedModel<>()`. `PagedResponse<>` custom dapat dihapus.

### 4.6 Error Handling

7. **`EntityNotFoundException`** dilempar langsung di `getDetail`. Verifikasi apakah proyek
   memiliki `BusinessException` atau `ResourceNotFoundException` yang seharusnya digunakan
   secara seragam dengan pesan yang i18n-friendly.

---

## 5. Rencana Perbaikan

### Tahap A — Quick Win Keamanan (P0)

1. **Perbaiki `getDetail`**:
   - Tambahkan parameter `userId` ke signature.
   - Ganti `mailRepository.findById` dengan query JOOQ yang filter status aktif.
   - Tambahkan pengecekan akses via `UserTaskQueryService.existsActive(userId, mailId)`.
   - Gunakan `AttachmentQueryRepository.findByRef` untuk attachment (sudah filter status=1).
   - Hapus injeksi `MailRepository` dan `AttachmentRepository` dari `MailQueryService`.

### Tahap B — Penguatan CQRS-lite (P1)

2. **Migrasi `MailRecipientQueryService`**:
   - Pindahkan `getTracking(mailId)` dan `getReadStatus(mailId)` dari `MailQueryService` ke
     `MailRecipientQueryService`.
   - Hapus injeksi `MailRecipientRepository` (JPA) dari `MailRecipientQueryService`.
   - Gunakan `RecipientQueryRepository` (JOOQ) atau JOOQ langsung.
   - Update controller: `getTracking` & `getReadStatus` → inject `MailRecipientQueryService`.

3. **Konsolidasi `resolveRootId`**:
   - Pindahkan logika resolve rootId dari `MailQueryRepository.findThread()` ke method shared
     (tidak diulang dua kali).

### Tahap C — Konsistensi API (P2)

4. **Standarisasi wrapper response** → `PagedModel<>` (Spring) untuk semua endpoint paginated.
   - Service mengembalikan `Page<T>` (`PageImpl`) — sudah berlaku untuk `lookup` dan `search`/`report` setelah refactor.
   - Controller membungkus dengan `new PagedModel<>(page)` — sama persis dengan pola endpoint master.
   - Berlaku untuk semua controller: `MailController`, `MailArchiveController`, `PublicationController`.
   - Hapus class `PagedResponse<>` custom setelah tidak ada yang menggunakannya.

5. **Unifikasi exception**: Verifikasi dan gunakan exception domain proyek secara konsisten.

### Tahap D — Cache & Validasi (P3)

6. **Cache** `getThread`, `getTracking`, `getReadStatus` dengan invalidasi event-based
   (`MailSentEvent`, `RecipientReadEvent`).

7. **Validasi input**: Tambahkan `@Valid` di controller & constraint pada `MailLookupParams`,
   `MailSearchRequest`, `MailReportRequest`.

8. **Naming**: Standarisasi prefix (`get*` untuk single, `find*` untuk koleksi).

---

## 6. Acceptance Criteria

- `getDetail(mailId, userId)` tidak dapat mengembalikan mail terhapus / mail user lain.
- `MailQueryService` tidak lagi meng-import `MailRepository` & `AttachmentRepository`.
- `MailRecipientQueryService` tidak lagi meng-import `MailRecipientRepository` JPA.
- `getTracking` dan `getReadStatus` tersedia di `MailRecipientQueryService`.
- Seluruh endpoint paginated mengembalikan `PagedModel<>` (Spring); class `PagedResponse<>` custom dihapus.
- Resolusi `rootId` tidak duplikasi di lebih dari satu titik.

---

## 7. Risiko & Mitigasi

| Risiko | Mitigasi |
|--------|----------|
| Perubahan signature `getDetail` memengaruhi controller | Update signature controller secara bersamaan |
| Pemindahan `getTracking`/`getReadStatus` memutus endpoint | Test endpoint setelah refactor; verifikasi routing |
| Standarisasi wrapper mengubah shape response | Sinkronisasi dengan FE; versioning endpoint bila perlu |
| `findThread` dan `findTracking` hasil berbeda setelah konsolidasi | Snapshot test sebelum refactor |

---

## 8. Dependensi & Urutan

1. **Plan ini (Plan 17)** → correctness & cleanup. Prasyarat plan berikutnya.
2. **Plan 18** → CQRS Mail + UserTask + Recipient — struktur jangka panjang.
3. **Plan V10-mail-search-index.md** → optimasi index untuk `search`/`report`.

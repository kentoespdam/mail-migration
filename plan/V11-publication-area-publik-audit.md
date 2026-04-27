# V11 — Audit Tabel `area_publik` & Modul Publication

> Audit kesesuaian (mismatch) antara struktur legacy `area_publik` di `smartoffice.sql`
> dengan implementasi modul `Publication` (Entity, Repository, Service, DTO, Controller)
> serta migrasi Flyway `V8__publication_schema.sql`.

---

## 1. Ringkasan Tabel Legacy `area_publik`

Sumber: `smartoffice.sql` baris 179–196.

| Kolom Legacy | Tipe Legacy | Catatan |
|--------------|-------------|---------|
| `id` | INT AUTO_INCREMENT | PK |
| `title` | VARCHAR(128) | judul publikasi |
| `description` | TEXT | deskripsi |
| `type` | INT (FK ke `jenis_dokumen.id`) | nullable |
| `status` | ENUM('Draft','Ditampilkan') | hanya 2 nilai |
| `published_date` | DATETIME NOT NULL | tanggal terbit |
| `created_by_name` | VARCHAR(128) NOT NULL | snapshot nama pembuat |
| `created_by_title` | VARCHAR(128) NOT NULL | snapshot jabatan |
| `original_file_name` | VARCHAR(256) | nama file asli |
| `system_file_name` | VARCHAR(256) | nama file di disk |
| `file_size` | INT | ukuran file (byte) |
| `notif_flag` | INT | 0/1 untuk notifikasi |

> Tabel legacy **tidak menyimpan** `created_by_user_id`, `created_at`, `updated_at`, atau `file_path`.
> Status hanya 2 nilai (Draft / Ditampilkan) — tidak ada `DELETED` (soft delete) di skema lama.

---

## 2. Temuan (Mismatch) Antara Legacy ⇄ Implementasi Saat Ini

### 2.1. Penamaan Kolom Berbeda dari Legacy

Implementasi V8 mengganti nama kolom legacy tanpa rencana migrasi data:

| Legacy | V8 / Entity | Dampak |
|--------|-------------|--------|
| `title` | `judul` | Data lama tidak terbaca |
| `description` | `desk` | Data lama tidak terbaca |
| `original_file_name` | `file_name` | Data lama tidak terbaca |
| `system_file_name` | `file_path` (semantik berubah jadi path lengkap) | Hilang pemisahan original vs system |

**Rencana**: kembalikan ke nama kolom legacy (`title`, `description`, `original_file_name`, `system_file_name`) untuk kompatibilitas data lama. Tambahkan kolom turunan baru bila perlu (`file_path` opsional sebagai derived/virtual).

### 2.2. Kolom Tambahan Tidak Konsisten

Entity & V8 menambahkan kolom baru: `created_by_user_id`, `created_at`, `updated_at`, `file_path`.
Tabel legacy tidak punya. Untuk migrasi data dari sistem lama, kolom-kolom ini harus **nullable**
dengan default sensible (e.g. `created_at = published_date` saat backfill).

### 2.3. `status` — Tipe & Nilai Tidak Cocok

| Aspek | Legacy | V8 / Entity |
|-------|--------|-------------|
| Tipe | ENUM(`Draft`,`Ditampilkan`) | VARCHAR(20) + CHECK(`DRAFT`,`PUBLISHED`,`DELETED`) |
| Default | NULL | `'DRAFT'` |
| Soft delete | Tidak ada | Pakai `DELETED` |

**Rencana**: tetap pakai VARCHAR + CHECK constraint (lebih portabel, mendukung soft-delete).
Tambah skrip backfill: `Draft → DRAFT`, `Ditampilkan → PUBLISHED`, NULL → `DRAFT`.

### 2.4. `published_date` — Constraint Tidak Cocok

Legacy: `NOT NULL`. V8: `NULL`. Entity logika: hanya di-set saat `publish()`.

**Rencana**: pertahankan `NULL` di V8 (karena DRAFT belum punya tanggal terbit). Saat migrasi data lama,
isi `published_date` legacy apa adanya; baris baru bertipe DRAFT akan tetap NULL. Konsisten dengan domain.

### 2.5. `created_by_name` & `created_by_title` — Panjang Berkurang

Legacy: VARCHAR(128). V8: VARCHAR(100). Migrasi data nama panjang akan **terpotong / gagal**.

**Rencana**: naikkan ke VARCHAR(128) agar match legacy.

### 2.6. `notif_flag` — Default & Nullability

Legacy: INT NULL. V8: `INT NOT NULL DEFAULT 0`. Entity: default 0.

**Rencana**: pertahankan V8 (NOT NULL DEFAULT 0). Pada backfill, `NULL → 0`.

### 2.7. DTO `PublicationResponse` — `createdByUserId` Type Mismatch

Field `createdByUserId` di response bertipe `String` (di-encode Sqids) padahal di entity `Integer`,
dan kolom DB-nya bukan PK Sqid-encoded. Pemakaian Sqids di sini **salah** — ini ID user dari sistem
HR/Auth, bukan PK lokal entity.

**Rencana**: ekspos `createdByUserId` sebagai `Integer` mentah (atau `String` plain), **jangan** Sqids-encode.
PublicationQueryRepository juga melakukan `encoder.encode(Publication.class, createdByUserId)` — ini bug.

### 2.8. `PublicationMapper` Tidak Map `createdByUserId`

Mapper hanya punya mapping `id`, `documentType`, `status`. Field `createdByUserId` tidak dipetakan secara
eksplisit dan tipenya berbeda (Integer → String). MapStruct akan mencoba auto-convert dan menghasilkan
nilai mentah string angka, **bukan** Sqids — inkonsistensi dengan repository JOOQ.

**Rencana**: setelah `createdByUserId` diubah ke `Integer` di response, mapper otomatis benar.

### 2.9. `PublicationParams` — Sort Whitelist Pakai Nama Legacy

`ALLOWED` memetakan `title → p.judul`. Setelah rename kembali ke `title`, sort tetap konsisten
(`title → p.title`). Pastikan diperbarui.

### 2.10. `PublicationQueryRepository` — Hardcode Nama Kolom

Semua field reference (`p.judul`, `p.desk`, `p.file_name`, dll) hardcoded sebagai string.
Harus ikut diperbarui setelah rename kolom.

### 2.11. `PublicationCommandService` — Path Storage Tidak Simpan Original Filename

`storeFile()` menyimpan `file.getOriginalFilename()` ke field `fileName` (yang sebenarnya original) lalu
`filePath` ke kolom `file_path`. Semantik tercampur dengan legacy `original_file_name` + `system_file_name`.

**Rencana**: pisahkan kembali — simpan `original_file_name` (dari upload) + `system_file_name` (UUID di disk),
lalu derive `file_path` di service saat download (tidak perlu disimpan; gunakan `yyyyMM/{system_file_name}`).

### 2.12. `Publication` Entity — `notifFlag` Bukan Boolean

Tipe `Integer` (0/1) — sesuai legacy & V8. Aman, tidak diubah.

### 2.13. Index FK ke `jenis_dokumen`

V8 sudah punya FK constraint `fk_publik_type`. Legacy tidak punya FK eksplisit. Aman, dipertahankan.
Pastikan `jenis_dokumen` sudah di-seed sebelum data legacy diimpor.

### 2.14. Charset / Collation

Legacy: `latin1 / latin1_swedish_ci`. V8: `utf8mb4 / utf8mb4_unicode_ci`. Saat ETL data legacy,
**wajib** transcode `latin1 → utf8mb4` agar karakter non-ASCII tidak korup.

---

## 3. Rencana Perbaikan (High Level)

### Tahap 1 — Selaraskan Schema (Migrasi V11)

1. Buat migrasi `V11__publication_align_legacy.sql` yang:
   - Rename kolom: `judul → title`, `desk → description`.
   - Tambah kolom: `original_file_name`, `system_file_name` (gantikan `file_name` & `file_path`).
   - Naikkan panjang `created_by_name` & `created_by_title` ke VARCHAR(128).
   - Drop kolom `file_name` & `file_path` setelah data dipindah ke pasangan baru.
2. Sediakan skrip backfill terpisah (V99 / one-shot) untuk import dari `area_publik` legacy:
   - Map kolom 1:1, transcode latin1 → utf8mb4.
   - Map status: `Draft → DRAFT`, `Ditampilkan → PUBLISHED`.
   - `notif_flag NULL → 0`, `created_at = published_date`, `updated_at = published_date`.

### Tahap 2 — Sinkronkan Layer Java

1. **Entity `Publication`**: ganti `@Column(name="judul")` → `name="title"`, `desk → description`.
   Pisah `fileName/filePath` menjadi `originalFileName / systemFileName`.
2. **PublicationMapper**: hapus mapping yang tidak perlu di-Sqids untuk `createdByUserId`.
3. **PublicationResponse DTO**: ubah `createdByUserId: String → Integer`, ganti `fileName` jadi
   `originalFileName`, drop `filePath` (tidak perlu di response).
4. **PublicationParams**: update whitelist sort `title → p.title`.
5. **PublicationQueryRepository**: ganti seluruh referensi kolom hardcoded; hapus `encoder.encode(Publication.class, createdByUserId)`.
6. **PublicationCommandService**: pisah penyimpanan `originalFileName` & `systemFileName`; hilangkan
   penyimpanan `filePath` di kolom DB — derive dari konvensi `publik/{yyyyMM}/{systemFileName}`.
7. **PublicationController**: tidak ada perubahan endpoint, hanya bergantung pada DTO yang sudah benar.

### Tahap 3 — Verifikasi

1. Test integrasi: create → publish → download → soft-delete.
2. Smoke test ETL: import 10 baris legacy sample, pastikan render benar di list & detail.
3. Cek query `findByNotifFlag(0)` masih bekerja untuk scheduler notifikasi.

---

## 4. Architecture Decisions

### AD-1. Tetap Pakai VARCHAR + CHECK Constraint untuk `status`

ENUM MySQL legacy sulit di-extend (perlu `ALTER TABLE` setiap nilai baru) dan tidak portabel ke
MariaDB/Postgres. VARCHAR + CHECK + Java Enum (`PublicationStatus`) lebih fleksibel dan mendukung
pola soft-delete (`DELETED`) yang konsisten dengan modul lain di codebase ini.

### AD-2. Pisahkan `original_file_name` & `system_file_name`, Tidak Simpan `file_path`

`file_path` adalah derived data (`publik/{yyyyMM}/{system_file_name}`). Menyimpan path di DB
menyebabkan:
- Duplikasi sumber kebenaran (DB vs konvensi storage).
- Sulit reorganisasi struktur folder tanpa migrasi data.
Solusi: hanya simpan `system_file_name`, derive path saat akses. Sesuai dengan tabel `attachments`
legacy yang juga hanya menyimpan `system_filename`.

### AD-3. Jangan Sqids-encode `createdByUserId`

`createdByUserId` adalah **foreign reference** ke sistem HR/Auth eksternal, **bukan** PK lokal modul
Publication. Sqids hanya untuk obfuscate PK internal. Kebocoran semantik sebelumnya (encode pakai
class `Publication.class`) menghasilkan ID yang tidak bisa di-decode balik dengan benar — bug laten.

### AD-4. ETL Legacy Sebagai Migrasi Terpisah, Bukan Bagian dari `V11`

Pemisahan schema-change vs data-import memudahkan rollback, dan environment dev/staging tidak
selalu butuh data legacy. Skrip ETL dijalankan one-shot via task admin, bukan Flyway versioned
migration.

---

## 5. Checklist Eksekusi

- [ ] Tulis `V11__publication_align_legacy.sql` (rename + pisah file kolom).
- [ ] Update `Publication.java` — anotasi kolom.
- [ ] Update `PublicationResponse.java` — tipe `createdByUserId`, rename file fields.
- [ ] Update `PublicationMapper.java` — hapus encode salah.
- [ ] Update `PublicationQueryRepository.java` — kolom & encoder.
- [ ] Update `PublicationCommandService.java` — `storeFile` simpan systemFileName saja.
- [ ] Update `PublicationParams.java` — whitelist sort.
- [ ] Test: unit + integrasi modul publication.
- [ ] Siapkan ETL one-shot dari `area_publik` legacy (terpisah, opsional per-environment).

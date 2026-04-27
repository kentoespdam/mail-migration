# V14 — Audit Lanjutan `area_publik` & Sinkronisasi Layer Java

> Audit kondisi modul Publication setelah migrasi `V11`–`V13`.
> Schema DB sudah selaras legacy `smartoffice.sql`, namun beberapa berkas Java
> masih merujuk nama kolom lama (`judul`, `desk`, `file_name`, `file_path`).
> Plan ini melengkapi `V11-publication-area-publik-audit.md`.

---

## 1. Status Migrasi DB Saat Ini

| Versi | Cakupan | Status |
|-------|---------|--------|
| `V8`  | Tabel awal `area_publik` (judul, desk, file_name, file_path) | Sudah dijalankan |
| `V11` | Rename ke `title`, `description` + pisah `original_file_name`/`system_file_name` + naikkan VARCHAR snapshot pembuat | Sudah dijalankan |
| `V12` | Backfill `created_at` dari `published_date` | Sudah dijalankan |
| `V13` | Normalisasi `system_file_name` (buang prefix path Windows/Linux) | Sudah dijalankan |

**Kesimpulan**: struktur tabel `area_publik` sekarang sesuai dengan referensi legacy
(`title`, `description`, `original_file_name`, `system_file_name`, snapshot pembuat
VARCHAR(128)). Tidak diperlukan migrasi schema baru.

---

## 2. Temuan Audit (Inkonsistensi Layer Java ⇄ DB)

### 2.1. Entity `Publication.java` Masih Pakai Nama Kolom Lama

Anotasi `@Column` masih mengacu ke kolom yang **sudah di-rename / di-drop** oleh `V11`:

| Field Java | Anotasi Saat Ini | Kolom Aktual di DB |
|------------|------------------|---------------------|
| `title` | `name = "judul"` | `title` |
| `description` | `name = "desk"` | `description` |
| `originalFileName` | `name = "file_name"` | `original_file_name` |
| `systemFileName` | `name = "file_path"` | `system_file_name` |
| `createdByName` / `createdByTitle` | `length = 100` | sudah VARCHAR(128) |

**Dampak**: setiap operasi tulis JPA (create/update/publish/delete) akan
gagal saat Hibernate mem-validasi atau menyusun SQL — kolom `judul`, `desk`,
`file_name`, `file_path` tidak ada lagi.

### 2.2. `PublicationQueryRepository.findFileMeta` Masih Query Kolom Lama

Method `findFileMeta` masih `select p.file_name, p.file_path`. Kolom tersebut
sudah di-drop pada `V11`. Jalur download (`/api/v1/publications/{id}/download`)
otomatis gagal di runtime.

Sebagian besar method lain (`findAll`, `findById`) sudah benar memakai
`p.title`, `p.description`, namun masih memetakan field lama (`p.file_name`)
pada bagian SELECT — perlu diluruskan agar konsisten dengan `original_file_name`
dan `system_file_name`.

### 2.3. JPA Repository / Service Tidak Punya Validasi Nama Kolom

`PublicationRepository` (Spring Data JPA) bergantung pada nama field entity,
sehingga setelah anotasi entity diperbaiki, tidak butuh perubahan.
Tetapi command service yang menyimpan file harus memastikan nilai
`originalFileName` dan `systemFileName` benar-benar dipisah saat upload —
bukan menyimpan path lengkap di kolom `system_file_name`.

### 2.4. DTO/Mapper Sudah Sesuai, Verifikasi Saja

- `PublicationResponse` sudah mengekspos `originalFileName` dan `createdByUserId`
  bertipe `Integer` (sesuai keputusan AD-3 di plan V11).
- `PublicationMapper` tidak lagi melakukan Sqids encode salah.
- `PublicationParams.ALLOWED` sudah memetakan `title → p.title`.

Tidak ada perubahan yang diperlukan, hanya verifikasi sebagai bagian dari
checklist setelah Entity diperbaiki.

### 2.5. `PublicationFileStorageService` Sudah Memisah Original vs System

Sudah benar: `store()` menghasilkan `StoredFile(systemFileName, originalFileName, fileSize)`.
Tidak ada `file_path` yang disimpan ke DB — sesuai dengan keputusan AD-2 plan V11.

### 2.6. Konsistensi Search Index dengan `V10`

`V10__mail_search_indexes.sql` belum menyentuh `area_publik`. Pencarian
keyword pada `findAll` saat ini memakai `LIKE` biasa pada `title`/`description`.
Untuk volume kecil (publikasi internal) ini cukup; FULLTEXT bisa ditambah jika
kebutuhan pencarian meningkat — bukan prioritas saat ini.

---

## 3. Rencana Perbaikan (High Level)

### Tahap 1 — Sinkronkan Entity & Repository dengan Schema

1. Update `@Column` di `Publication.java`:
   - `judul → title`
   - `desk → description`
   - `file_name → original_file_name`
   - `file_path → system_file_name`
   - Naikkan `length` `createdByName` & `createdByTitle` menjadi 128 agar
     mengikuti definisi DB.
2. Perbaiki `PublicationQueryRepository.findFileMeta` agar memilih dan
   memetakan kolom `original_file_name` & `system_file_name`.
3. Telusuri seluruh file JOOQ lain (jika ada) yang masih memakai `p.file_name`
   atau `p.file_path` dan luruskan ke kolom baru.

### Tahap 2 — Verifikasi End-to-End

1. Jalankan unit test `PublicationCommandService` & `PublicationQueryService`.
2. Smoke test API:
   - `POST /publications` dengan upload file → cek baris `area_publik` punya
     `original_file_name` & `system_file_name` terisi benar.
   - `PATCH /publications/{id}/publish` → status berubah dan `published_date`
     terisi.
   - `GET /publications/{id}/download` → file ter-stream tanpa error.
3. Jalankan Flyway `migrate` ulang di environment dev untuk memastikan
   tidak ada drift schema vs migrasi.

### Tahap 3 — Kebersihan Dokumentasi

1. Update `memory.md` (root project) jika ada catatan tabel `area_publik` yang
   masih merujuk nama lama.
2. Tutup checklist plan `V11-publication-area-publik-audit.md` yang masih
   tersisa pada item Entity & Repository.

---

## 4. Architecture Decisions Tambahan

### AD-5. Tidak Buat Migrasi Baru untuk Inkonsistensi Java

Schema sudah benar. Membuat migrasi tambahan hanya untuk "menyamakan" Java
justru menambah noise. Cukup perbaiki anotasi di layer aplikasi.

### AD-6. Hindari Dual-Write `original_file_name`/`system_file_name`

Jangan menambahkan kolom turunan (`file_path`) di DB. Path penuh selalu
diturunkan dari konvensi storage `publik/{yyyyMM}/{system_file_name}` agar
tidak ada duplikasi sumber kebenaran.

---

## 5. Checklist Eksekusi

- [ ] Update anotasi `@Column` di `Publication.java` (4 field utama + length snapshot pembuat).
- [ ] Perbaiki `PublicationQueryRepository.findFileMeta` (kolom `original_file_name` & `system_file_name`).
- [ ] Audit cepat seluruh referensi `file_name` / `file_path` di kode Java; ganti ke kolom baru.
- [ ] Jalankan test suite modul publication.
- [ ] Smoke test create → publish → download → soft delete.
- [ ] Update dokumentasi memori jika perlu.

# 08 — Perbaikan `PublicationFileStorageService.load` — File Not Found (Path Ganda)

> Audit & rencana perbaikan untuk error `File not found on disk` pada endpoint
> `GET /api/v1/publications/{id}/download`. Gejala terbaru: path resolusi
> mengandung segmen `publik/{yyyyMM}` **dua kali**.

---

## 1. Ringkasan Masalah

Path yang dicari oleh service muncul dengan duplikasi segmen:

```
/mnt/DATA/.../data/attachments/publik/202604/publik/202604/a6b6567b-...jpeg
                                ^^^^^^^^^^^^ ^^^^^^^^^^^^
                                base+month   diulang lagi
```

Padahal file fisik berada di:

```
/mnt/DATA/.../data/attachments/publik/202604/a6b6567b-...jpeg
```

Akibatnya `findFile()` selalu return `null` → `load()` melempar
`IllegalStateException: File not found on disk`.

---

## 2. Akar Penyebab

### 2.1. `system_file_name` Berisi Prefix Path, Bukan Basename

Kontrak service **mengasumsikan** kolom `system_file_name` di DB hanya berisi
**basename** (mis. `a6b6567b-....jpeg`). Konstruktor `storagePath` sudah
mengarah ke `<base>/publik`, dan `findFile()` melakukan
`storagePath.resolve(yyyyMM).resolve(systemFileName)`.

Jika nilai DB ternyata `publik/202604/a6b6567b-....jpeg` (mengandung prefix
folder), hasil resolve menjadi `<base>/publik/202604/publik/202604/<file>` —
persis seperti gejala yang dilaporkan.

Sumber kontaminasi data:

- **Migrasi V11 §3** mem-backfill `system_file_name` dari `SUBSTRING_INDEX(file_path, '/', -1)`.
  Ekspektasinya menghasilkan basename, tetapi tidak tahan terhadap data legacy
  yang punya separator non-`/` (mis. backslash Windows), trailing slash,
  whitespace, atau `file_path` yang **null** sementara `file_name` sudah
  berisi path relatif.
- Tidak ada validasi/sanitasi nilai `system_file_name` saat insert/upload
  baru — hanya `store()` yang menjamin hasilnya basename. Path-form yang
  berasal dari migrasi atau import manual lolos tanpa pemeriksaan.

### 2.2. Resolver Tidak Defensif Terhadap Input Berisi Separator

`findFile()` langsung men-`resolve` apa pun yang masuk. Tidak ada langkah
normalisasi yang men-**strip** komponen direktori dari `systemFileName`.
Validasi `path.startsWith(storagePath)` tetap lolos karena hasilnya memang
masih di bawah `storagePath` — hanya saja **lebih dalam** dari yang seharusnya.

Hal ini juga membuat fallback flat & glob ikut gagal, karena ketiga jalur
pencarian sama-sama memakai `systemFileName` mentah.

### 2.3. Asimetri Kontrak `store()` vs `load()`

`store()` selalu menulis file dengan basename UUID → DB seharusnya konsisten
basename. Tetapi `load()` tidak menegakkan kontrak yang sama saat membaca,
sehingga setiap row legacy/migrasi yang menyimpang langsung tidak ter-serve.

---

## 3. Rencana Perbaikan (High Level)

### Tahap 1 — Tegakkan Kontrak "Basename Saja" pada Resolver

1. Sebelum melakukan resolusi path, **ekstrak basename** dari nilai yang
   diterima service (buang segmen direktori apa pun, normalisasi separator
   campuran). Ini menjadi *single normalization point* untuk `load()`,
   `delete()`, dan `findFile()`.
2. Tolak / log WARN nilai yang setelah dinormalisasi menjadi kosong, hanya
   titik, atau mengandung karakter terlarang. File tidak boleh di-serve
   dari nilai yang tidak meyakinkan.
3. Pertahankan guard `path.startsWith(storagePath)` sebagai pengaman
   path-traversal di setiap percobaan resolusi.

### Tahap 2 — Bersihkan Data Legacy di DB

1. Tambah migrasi follow-up (V13) yang menormalisasi `system_file_name`
   yang masih berisi prefix folder → ambil basename-nya saja.
2. Cakup variasi legacy: separator `/`, `\\`, kombinasi, trailing slash.
3. Sebagai jaring pengaman, log/laporkan baris yang basename-nya tidak
   cocok dengan file fisik di disk supaya bisa direkonsiliasi manual
   (tidak harus auto-fix di migrasi).

### Tahap 3 — Cegah Regresi di Jalur Tulis Baru

1. Pada `store()` dan setter di entity/DTO, pastikan nilai yang masuk ke
   `system_file_name` selalu berupa basename. Jika ada path lain yang
   menulis kolom ini (mis. importer/backfill), tambahkan normalisasi yang
   sama di sana.
2. Opsional: tambah constraint domain (validasi di entity) yang menolak
   nilai mengandung separator path. Gagal cepat saat insert lebih baik
   daripada gagal saat user men-download.

### Tahap 4 — Verifikasi

1. Test integrasi: simpan publikasi, lalu manual update `system_file_name`
   menjadi bentuk berprefix (`publik/{yyyyMM}/{file}`) — `load()` harus
   tetap mengembalikan resource yang benar.
2. Test integrasi: nilai dengan backslash atau separator campuran.
3. Smoke test endpoint download untuk row legacy yang tadinya gagal,
   pastikan path final yang di-log tidak duplikat.

---

## 4. Architecture Decisions

### AD-1. Resolver Memaksa Basename, Bukan Mempercayai DB

Mengandalkan integritas data legacy (V11) sebagai prasyarat agar `load()`
berfungsi membuat sistem rapuh — sekali ada satu row "kotor", endpoint
download diam-diam rusak. Lebih kuat menjadikan resolver **idempotent**
terhadap variasi input: apa pun bentuk nilai DB, resolver mengambil basename
sebelum resolve. Migrasi data tetap dilakukan untuk kebersihan, tetapi
resolver tidak bergantung padanya.

### AD-2. Normalisasi Sekali di Pintu Masuk Service

Hindari menyebar normalisasi ke setiap call-site (`load`, `delete`, dan masa
depan). Cukup satu helper privat di service yang dipanggil oleh semua
public API. Kontrak: "service ini hanya tahu basename".

### AD-3. Tidak Mengubah Skema atau Storage Layout

Layout `<base>/publik/{yyyyMM}/{basename}` sudah benar dan dipakai
`store()`. Tidak perlu refactor layout, kolom DB, atau migrasi struktural.
Perbaikan terbatas pada (a) sanitasi nilai input pada resolver dan
(b) backfill normalisasi data legacy.

---

## 5. Checklist Eksekusi

- [ ] Tambah normalisasi basename di `PublicationFileStorageService` (dipakai
      oleh `load`, `delete`, dan internal `findFile`).
- [ ] Tambah test reproduksi: `system_file_name` berisi `publik/{yyyyMM}/<uuid>.<ext>`
      → `load` berhasil, path final tidak duplikat.
- [ ] Tambah test untuk separator campuran (`\\`, trailing slash, spasi).
- [ ] Buat migrasi V13 untuk normalisasi `system_file_name` legacy ke basename.
- [ ] Smoke test manual `GET /api/v1/publications/{id}/download` pada row
      yang sebelumnya gagal (UUID `a6b6567b-5123-43cc-8bce-ef56ba04c06c`).
- [ ] Catat di log WARN setiap kali resolver perlu menormalisasi nilai
      berprefix — jadi indikator data legacy yang masih perlu dirapikan.

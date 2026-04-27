# Plan: Implementasi CQRS Pattern untuk Modul `Publication`

> **Audit & rencana refactor** kepatuhan modul `Publication` terhadap pola CQRS-lite
> (JPA = write, JOOQ = read) yang sudah menjadi standar di proyek `mail-service`.
>
> Target pembaca: junior dev / AI assistant kecil.
> Penjelasan high-level — tidak menyertakan contoh kode.

---

## ⚡ Wajib: Gunakan Context7

> [!IMPORTANT]
> Sebelum mulai refactor, query Context7 untuk memastikan praktik terbaru:
> - `"Spring Boot 4 CQRS pattern command query separation"`
> - `"Spring Data JPA write-only repository best practice"`
> - `"jOOQ 3.20 read model projection DSLContext"`
> - `"Spring transactional readOnly true performance"`
> - `"Spring ApplicationEventPublisher transactional event listener"`

---

## 1. Ringkasan Kondisi Saat Ini

Modul `Publication` **sudah dipisah** menjadi `PublicationCommandService` (write/JPA)
dan `PublicationQueryService` (read/JOOQ). Namun pemisahan masih dangkal dan
beberapa tanggung jawab masih tercampur:

| Komponen                       | Lokasi                                      | Peran sekarang        |
|--------------------------------|---------------------------------------------|-----------------------|
| `PublicationController`        | `controller/core/`                          | Memanggil dua service |
| `PublicationCommandService`    | `service/core/publication/`                 | Create/Update/Delete/Publish + I/O file |
| `PublicationQueryService`      | `service/core/publication/`                 | findAll, findById, **download** |
| `PublicationQueryRepository`   | `repository/core/jooq/`                     | Query JOOQ list & detail |
| `PublicationRepository` (JPA)  | `repository/core/jpa/`                      | Save/find untuk write & download |
| `PublicationMapper`            | `dto/core/publication/`                     | Map entity → DTO (write path) |
| `PublicationPublishedEvent`    | `event/`                                    | Event publish async |

### Temuan utama

1. **Pelanggaran batas Query → Write Repository.**
   `PublicationQueryService.download()` memakai `PublicationRepository` (JPA)
   bukan jalur read JOOQ. Akibatnya read service masih bergantung pada JPA
   entity graph + lazy loading (potensi N+1 & boot Hibernate context tidak perlu).

2. **Read service melakukan I/O sistem berkas.**
   `download()` resolve path file di filesystem. Ini bukan murni "read model",
   melainkan side-effect (filesystem access, validasi path traversal).
   Bercampur antara *query data* dan *file streaming concern*.

3. **Command service mengembalikan DTO via JPA mapper.**
   Setelah `save()`, Command memanggil `mapper.toDto(pub)` (entity-based).
   Ini menyebabkan dua jalur mapping berbeda untuk satu DTO yang sama
   (JOOQ projection di Query, MapStruct di Command) — sumber inkonsistensi
   field (mis. `documentType` lookup di JOOQ vs lazy load di JPA).

4. **Tidak ada kontrak (interface) eksplisit untuk Command & Query.**
   Service di-inject sebagai class konkret. Membuat sulit menggantinya
   (mis. menambah caching decorator pada Query) tanpa menyentuh controller.

5. **Read transaction boundary tidak optimal.**
   `@Transactional(readOnly = true)` dipasang di kelas Query, namun
   karena memakai JOOQ `DSLContext` (auto-commit DataSource), anotasi ini
   hanya bermakna jika ada bagian JPA. Konsisten gunakan satu strategi.

6. **Filter & sort logic tertanam di repository.**
   Pembentukan `Condition` (where keyword/typeId/dates) ada di
   `PublicationQueryRepository`. Untuk query yang akan tumbuh
   (filter status, range publikasi, full-text), lebih baik dipisahkan
   ke "specification builder" / criteria object agar dapat di-unit-test
   tanpa membangkitkan SQL.

7. **Publish flow memutar ulang event di dua tempat.**
   `create(publish=true)` dan `publish()` keduanya `publishEvent`.
   Risikonya: jika `update()` mengubah `publish=true` setelah
   `create(publish=false)`, event terbit ulang? (sudah ada guard
   `wasPublished`, tapi tidak terdokumentasi sebagai invariant CQRS).

8. **Belum ada read-side caching.**
   Modul publik (area_publik) cocok di-cache (TTL menengah) karena read-heavy
   dan write-rare. Tidak ada layer cache di Query path.

---

## 2. Sasaran Akhir (Target Architecture)

Pisahkan Publication menjadi **dua jalur tegas**:

```
                ┌─────────────────────────────────────┐
                │   PublicationController (thin)      │
                └──────────┬──────────────┬───────────┘
                  WRITE    │              │  READ
                           ▼              ▼
              ┌────────────────────┐ ┌────────────────────┐
              │ PublicationCommand │ │ PublicationQuery   │
              │ Handler            │ │ Handler            │
              │ (interface + impl) │ │ (interface + impl) │
              └─────┬───────┬──────┘ └─────────┬──────────┘
                    │       │                  │
        ┌───────────┘       └─────────┐        │
        ▼                             ▼        ▼
   JPA Repository               File Storage   JOOQ Read Repo
   (write only)                 Service        (projection only)
        │                             │             │
        └───────────► MariaDB ◄───────┴─────────────┘
                          │
                          ▼
                ApplicationEventPublisher
                (PublicationPublishedEvent → async listeners)
```

Prinsip:

- Command = ubah state + emit event. Tidak return projection rumit;
  cukup return ID/Sqid atau "ringkasan" dari entity yang baru disimpan.
- Query = baca-only, JOOQ saja, hasil langsung DTO projection.
- Filesystem (download/store) **bukan** bagian Command/Query —
  pisahkan ke `PublicationFileStorageService`.

---

## 3. Langkah-Langkah High Level

### Langkah 1 — Stabilkan Kontrak (Interface Layer)

- Buat dua interface: `PublicationCommandHandler` & `PublicationQueryHandler`.
- Pindahkan kelas existing menjadi `*Impl` yang mengimplementasi interface.
- Controller hanya bergantung pada interface. Memudahkan
  decoration (cache, audit, metric) tanpa ubah controller.

### Langkah 2 — Tarik Filesystem Keluar dari Query

- Buat service baru `PublicationFileStorageService` (single source of truth
  untuk path resolution, store, delete, load resource, fallback legacy flat path).
- `PublicationCommandServiceImpl.storeFile/deleteOldFile` panggil service ini.
- Endpoint `download` pindah ke handler tersendiri (tetap di controller),
  memanggil `PublicationQueryHandler.findFileMetadata(id)` + `FileStorageService.load(...)`.
- Hilangkan dependensi `PublicationRepository` (JPA) dari `PublicationQueryService`.

### Langkah 3 — Bersihkan Read Path JOOQ

- Pindahkan filter/sort ke object terpisah (mis. `PublicationFilterCriteria`)
  yang dibangun dari `PublicationParams` di service, bukan di repository.
- Repository JOOQ hanya menerima criteria object & mengembalikan
  `Page<PublicationResponse>` / `Optional<PublicationResponse>` /
  `Optional<PublicationFileMeta>`.
- Hindari `field("p.x")` string raw kalau memungkinkan — gunakan
  generated jOOQ records jika sudah tersedia (lihat plan `V11`).
- Pastikan `findById` tidak ikut terkena `@SQLRestriction` JPA;
  Query JOOQ selalu eksplisit `status != 'DELETED'`.

### Langkah 4 — Bersihkan Write Path JPA

- `PublicationCommandHandlerImpl` hanya boleh menyentuh:
  `PublicationRepository`, `DocumentTypeRepository`,
  `AllowedFileTypeService`, `PublicationFileStorageService`,
  `ApplicationEventPublisher`.
- Setelah `save()`, **jangan** map ke DTO besar via Mapper.
  Cukup return `PublicationCommandResult` ringkas (Sqid + status + timestamp),
  atau kembalikan `id` lalu controller fetch ulang melalui Query handler
  agar response shape selalu identik dengan list/detail (single source of truth).
- Ini menghilangkan dual-mapping (JOOQ vs MapStruct) untuk DTO yang sama.

### Langkah 5 — Konsolidasi Event Publishing

- Pindahkan logic "kapan event terbit" ke method domain di entity
  `Publication.publish()` agar transition draft→published atomik.
- Command handler hanya cek hasil transisi (return boolean) untuk memutuskan
  apakah perlu `publishEvent`. Hilangkan duplikasi guard di create/update/publish.
- Tetap pakai `@TransactionalEventListener(AFTER_COMMIT)` di listener.

### Langkah 6 — Tambah Read-Side Caching (Opsional, recommended)

- Cache `findAll(params)` & `findById(id)` di Query layer dengan Redis.
  TTL ~5–10 menit (modul publik, jarang berubah).
- Invalidasi cache di Command handler (post-commit) untuk semua mutasi:
  create/update/delete/publish.
- Gunakan `cacheNames="publications"` agar bisa di-flush eksplisit
  via `CacheManager` saat batch update.

### Langkah 7 — Penegakan Boundary di Build

- Pertimbangkan ArchUnit test:
  - "Class di package `service.*.publication` yang namanya `*QueryService*`
    tidak boleh import `repository.core.jpa.*`."
  - "Class di package `service.*.publication` yang namanya `*CommandService*`
    tidak boleh import `repository.core.jooq.*`."
- Ini mencegah regresi batas CQRS di masa depan.

### Langkah 8 — Test Strategy

- **Command tests**: `@DataJpaTest` + Testcontainers MariaDB,
  verifikasi state transisi DRAFT→PUBLISHED dan event terbit sekali.
- **Query tests**: integration test JOOQ dengan dataset Flyway,
  fokus pada filter (keyword, type, date range), sort, paging.
- **File storage tests**: unit test path traversal, fallback flat-path legacy.
- **Controller tests**: tetap `@WebMvcTest` mocking kedua handler interface.

---

## 4. Urutan Pengerjaan

```
1. Buat interface Command/Query handler         (refactor non-breaking)
2. Pisahkan PublicationFileStorageService       (extract method-level)
3. Hapus dependency JPA dari Query service      (download → file service)
4. Pindahkan filter/sort ke FilterCriteria      (clean read path)
5. Ubah Command return menjadi ringkas + re-fetch lewat Query
6. Konsolidasi event publishing di domain method
7. Tambahkan caching Redis di Query (opsional)
8. Tambahkan ArchUnit boundary test
9. Tambahkan/refresh test command, query, storage
```

Setiap langkah harus tetap menjaga API publik di
`PublicationController` **stabil** (path & response shape tidak berubah).

---

## 5. Kriteria Selesai (Definition of Done)

- [ ] `PublicationQueryService` tidak lagi `import` package `repository.core.jpa`.
- [ ] `PublicationCommandService` tidak lagi `import` package `repository.core.jooq`.
- [ ] Endpoint `GET /publications/{id}/download` tidak menyentuh JPA repository.
- [ ] Response DTO untuk list, detail, dan hasil create/update **identik shape**.
- [ ] `PublicationPublishedEvent` terbit tepat satu kali per transisi DRAFT→PUBLISHED.
- [ ] Cache Redis (jika diaktifkan) ter-invalidasi setelah create/update/delete/publish.
- [ ] ArchUnit test memvalidasi boundary CQRS.
- [ ] Test coverage Command & Query masing-masing ≥ 80% jalur happy + edge.

---

## 6. Architecture Decisions

> Bagian ini wajib dibaca sebelum mulai coding — beberapa keputusan
> mengubah perilaku internal walau API publik tetap.

### AD-1. Command tidak mengembalikan DTO penuh

**Alasan**: dua jalur mapping (JOOQ projection vs MapStruct entity-mapper)
untuk DTO yang sama menyebabkan field rawan drift (mis. `documentType`
lookup berbeda). Dengan re-fetch via Query handler setelah commit,
semua response berbasis satu sumber kebenaran (JOOQ projection).

**Konsekuensi**: 1 round-trip DB tambahan per write.
Diterima karena modul `Publication` write-rare.

### AD-2. Filesystem service dipisah dari Command & Query

**Alasan**: filesystem I/O bukan domain CQRS. Mencampurnya membuat
Query service tidak murni "read model" dan menyulitkan testing.

**Konsekuensi**: tambahan satu kelas service & satu interface storage
abstraction (siap untuk migrasi ke object storage / S3 di masa depan).

### AD-3. Hilangkan `@Transactional(readOnly=true)` di Query handler murni JOOQ

**Alasan**: JOOQ `DSLContext` mengelola koneksi sendiri via
`DataSourceConnectionProvider`. `@Transactional` hanya menambah overhead
proxy tanpa manfaat (tidak ada Hibernate session yang di-flush).
Tetap pertahankan jika ada bagian campuran JPA+JOOQ pada method tertentu.

**Konsekuensi**: konsistensi runtime; dokumentasikan jelas di kelas.

### AD-4. Event publishing dipindah ke domain method `Publication.publish()`

**Alasan**: invariant "event terbit hanya saat transisi DRAFT→PUBLISHED"
harus dilindungi di level entity, bukan dihitung ulang di tiap method
service. Mencegah duplikasi event saat alur create-with-publish
diikuti update-with-publish.

**Konsekuensi**: entity sedikit lebih "rich" (anti-anemic). Tidak menambah
ketergantungan Spring di entity (cukup return boolean / status hasil transisi
agar service yang menerbitkan event).

### AD-5. Caching read-side adalah opsional, tapi disarankan

**Alasan**: `area_publik` dibaca publik (frontend area publikasi),
tetapi update jarang. Cache TTL 5–10 menit memberi reduksi load DB
signifikan tanpa risiko stale yang besar.

**Konsekuensi**: butuh strategi invalidasi yang ketat di Command handler
(post-commit). Gunakan `@TransactionalEventListener(AFTER_COMMIT)` untuk
flush cache supaya tidak ada race condition saat rollback.

### AD-6. ArchUnit sebagai pengaman regresi CQRS

**Alasan**: Tanpa pengaman build-time, pelanggaran batas (Query memakai
JPA repo, atau sebaliknya) akan terus berulang setiap kali dev menambah
fitur. Test ArchUnit murah dan cepat.

**Konsekuensi**: 1 file test baru. Bisa dijadikan template untuk modul
lain (Mail, Archive) sebagai langkah lanjutan project-wide.

---

## 7. Out of Scope (Tidak Ditangani Plan Ini)

- Migrasi data legacy `area_publik` (sudah dibahas di plan `V11`).
- Penyatuan skema kolom (`title` vs `judul`, dst.) — prasyarat dari V11.
- Penambahan endpoint baru (mis. statistik publikasi) — tunggu V11 selesai.
- Perpindahan file storage ke object storage (S3/MinIO) — task tersendiri.

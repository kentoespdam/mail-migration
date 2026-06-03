# ADR-010: Opaque External Id via Typed Wrappers

- **Status**: Final
- **Date**: 2026-05-09
- **Supersedes**: —
- **Related**: `SqidsEncoder`, `MailMapper`, all controllers in `controller/core` & `controller/master`

## Context

Internal id surat & master data adalah `Long` (BIGINT). Klien tidak boleh
melihat id numerik mentah — id eksternal di-encode sebagai **sqid** (opaque,
per-class shuffled alphabet) lewat `SqidsEncoder`.

Implementasi saat ini mengangkut sqid sebagai `String` di seluruh wire layer
(path var, request body, response). Setiap controller method memanggil
`encoder.decode(EntityClass, id)` manual, dan setiap mapper memanggil
`encoder.encode(EntityClass, id)` manual. Konsekuensinya:

1. **Bug class-mismatch tak terdeteksi kompiler.**
   `MailMapper.toAuditDto:73` meng-encode `createdBy` (user id) memakai token
   `Mail.class`. Sqid yang dikirim ke klien valid dari sisi struktur, tapi
   ketika dikirim balik akan gagal decode karena prefix/alphabet berbeda.
2. **Decode error → HTTP 500.** `IllegalArgumentException` dari decoder
   tidak di-map; sqid invalid dari klien jadi server error, bukan 400.
3. **Width truncation.** `MailAttachmentController:62` melakukan
   `(int) encoder.decode(...)` — id BIGINT dipersempit ke `int` secara diam.
4. **Bypass tak konsisten.** `MessageTemplateController` tidak men-decode
   sqid sama sekali — id mentah masuk ke service.
5. **Shallow surface.** Setiap endpoint menyalin pola decode 1:1; tidak ada
   tempat tunggal untuk menambah validasi atau metrik.

## Decision

Adopsi **typed-wrapper seam** untuk id eksternal:

1. **`SqidId` sealed interface** + record per entity (`MailId`, `EmployeeId`,
   `MailTypeId`, dst) di `dto/id/`. Setiap record menyimpan `long value()`.
2. **Spring `Converter<String, SqidId>`** untuk path var → wrapper.
3. **Jackson `JsonDeserializer` & `JsonSerializer` global** (via
   `@JsonComponent`/Module) untuk wrapper ↔ string sqid di body & response.
4. **DTO request fields** retyped dari `String` → wrapper (`MailId`,
   `EmployeeId`, dst). Daftar wrapper umum: `MailId`, `MailTypeId`,
   `MailCategoryId`, `AttachmentId`, `PublicationId`, `QuickMessageId`,
   `MessageTemplateId`, `EmployeeId`, `OfficeId`, `PositionId`, `UserId`.
5. **DTO response fields** retyped serupa. Mapper tidak lagi memanggil
   `encoder.encode(...)` — Jackson serializer yang serialize.
6. **Service signatures tetap `long`.** Controller unwrap `id.value()` di
   edge. Repository / JOOQ / JPA tak berubah.
7. **`@RestControllerAdvice` handler** untuk `IllegalArgumentException` dari
   decoder → **HTTP 400** dengan body `{ "error": "Invalid id" }`. Berlaku
   untuk Converter (path var) maupun Jackson Deserializer (body).
8. **`SqidsEncoder` tetap ada** sebagai backing implementation. Tidak ada
   pemanggilan langsung dari controller / mapper setelah migrasi selesai.

## Consequences

**Positif (locality & leverage):**

- Bug class-mismatch lenyap secara struktural. `MailId` ≠ `UserId` di tipe
  level — kompiler tolak `MailId` di field `createdBy`.
- Sqid invalid → 400 di satu tempat. Tidak perlu try/catch per controller.
- Test surface mengkerut: `SqidIdConverter`, `SqidJsonModule`, dan error
  handler diuji satu kali; controller test fokus ke business logic.
- Width truncation di `AttachmentController` ikut hilang (service signature
  konsisten `long`).
- `MessageTemplateController` bypass terdeteksi saat field DTO di-retype.

**Negatif:**

- ~12–15 record satu-baris per entity (boilerplate, tapi terpusat di
  `dto/id/`).
- Existing JSON wire format **tidak berubah** — Jackson serializer
  mengeluarkan string sqid yang sama. Tapi tiap kali muncul entity baru,
  developer harus tambah `XId` record (dijaga lewat code review).

**Tidak diubah:**

- Encoding scheme (per-class shuffled alphabet, prefix konsonan).
- Service / repository / database schema.
- HTTP wire format yang dilihat klien.

## Alternatives Considered

- **`String` + `@SqidOf(EntityClass.class)` validator.**
  Lebih sedikit kelas, tapi tidak menutup bug class-mismatch — `String` tetap
  satu tipe. Ditolak karena gagal "deletion test": menghapus annotation
  hanya melemahkan validasi runtime, struktur shallow tetap.
- **Typed wrapper sampai service & repository.**
  Memaksa `MailId` masuk ke JPA / JOOQ. Konflik dengan instruksi eksplisit
  bahwa internal tetap `Long`. Ditolak.

## Rollout

Rolling, satu modul at a time, masing-masing PR self-contained:

1. Infrastruktur (`SqidId`, Converter, JsonModule, error handler).
2. QuickMessage (smallest, no relations).
3. MessageTemplate + master types (MailType, MailCategory, Office, Position).
4. Attachment (sekalian fix `(int)` cast).
5. Publication.
6. Mail + MailRecipient (mapper bug `createdBy` ikut di-fix).
7. Sweep — hapus residual `encoder.decode/encode` calls; assert via grep.

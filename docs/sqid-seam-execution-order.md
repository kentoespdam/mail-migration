# Urutan Pengerjaan Sqid Seam (ADR-010)

> Konsolidasi dari grilling skill `improve-codebase-architecture` → 7 issues
> rolling rollout untuk migrasi opaque external id ke typed wrapper.
> Source ADR: [`docs/adr/010-opaque-external-id-typed-wrappers.md`](./adr/010-opaque-external-id-typed-wrappers.md)
> Beads parent: — (linear chain, tanpa epic) · GH parent: belum dibuat
> Domain term: `### SqidId (Opaque External Id)` di [`CONTEXT.md`](../CONTEXT.md)

Urutan ini diatur supaya:
1. Infrastruktur (interface, Converter, JsonModule, advice 400) jadi
   pondasi sebelum modul apa pun di-migrasi.
2. Modul terkecil (QuickMessage) jadi pembuktian pola — kalau pola jalan
   di sini, sisa modul mekanis.
3. Master types lebih dulu dari Mail/Recipient karena dipakai sebagai FK
   di DTO Mail (mailTypeId, mailCategoryId).
4. Mail+Recipient terakhir karena paling besar dan men-trigger fix bug
   `MailMapper.toAuditDto` (createdBy class-mismatch).
5. Sweep di akhir untuk assert tidak ada residual call site.

Legend: **AFK** = autonomous · 🔒 = blocked-by · ⚠️ = side-effect bug fix.

---

## Wave 0 — Pondasi (tanpa side-effect ke kode existing)

| # | Beads | Tipe | Slice | Catatan | Status |
|---|---|---|---|---|---|
| 0a | `mail-service-hzx` | AFK · P2 | `SqidId` sealed interface + 11 record wrapper di `dto/id/`, `SqidIdConverter` (Spring `Converter`), `SqidJsonModule` (`@JsonComponent`), `RestControllerAdvice` map `IllegalArgumentException` decoder → 400 | Tidak menyentuh controller/DTO existing — semua test lama tetap hijau | [x] |

---

## Wave 1 — Pembuktian Pola

| # | Beads | Tipe | Slice | 🔒 Blocked by | Status |
|---|---|---|---|---|---|
| 1a | `mail-service-64h` | AFK · P2 | Migrasi **QuickMessage** ke typed wrapper (controller path var, request/response DTO, mapper) | `mail-service-hzx` (0a) | [x] |

**Acceptance gate**: setelah 1a, wire JSON harus identik dengan sebelumnya
(klien tidak melihat perubahan), dan `grep encoder.decode/encode` di scope
QuickMessage = 0 hit.

---

## Wave 2 — Master Types & Side-effect Fix Pertama

Modul-modul tanpa relasi nested. Bisa di-batch satu PR atau split per-modul.

| # | Beads | Tipe | Slice | 🔒 Blocked by | Status |
|---|---|---|---|---|---|
| 2a | `mail-service-83t` | AFK · P2 ⚠️ | Migrasi **MessageTemplate + MailType + MailCategory + Office + Position**. Side effect: `MessageTemplateController` yang sebelumnya bypass sqid (id mentah masuk ke service) — kontrak diperbaiki, klien sekarang wajib kirim sqid | `mail-service-64h` (1a) | [x] |

⚠️ **Catatan kontrak**: Setelah 2a, `MessageTemplateController` berubah
kontrak (id mentah → sqid). Catat di release note dan koordinasi dengan FE
sebelum merge.

---

## Wave 3 — Attachment & Publication

| # | Beads | Tipe | Slice | 🔒 Blocked by | Status |
|---|---|---|---|---|-----|
| 3a | `mail-service-2kn` | AFK · P2 ⚠️ | Migrasi **Attachment** + fix `MailAttachmentController:62` (int) cast bug. Service signature naik konsisten ke `long` | `mail-service-83t` (2a) | [x] |
| 3b | `mail-service-lsr` | AFK · P2 | Migrasi **Publication**. Pastikan cache hit `PublicationResponse` tetap deserialize benar (lihat memori `cache-redis-pada-cacheconfig`) — `SqidId` record harus masuk allow-list `id.perumdamts.mail` di Redis serializer | `mail-service-2kn` (3a) | [x] |

⚠️ **Side effect 3a**: Truncation `BIGINT → int` lenyap struktural. Bukan
bug aktif (tabel attachment masih < `Integer.MAX_VALUE`), tapi latent.

---

## Wave 4 — Modul Terbesar (Mail + Recipient)

| # | Beads | Tipe | Slice | 🔒 Blocked by | Status |
|---|---|---|---|---|--------|
| 4a | `mail-service-een` | AFK · P2 ⚠️ | Migrasi **Mail + MailRecipient**. Termasuk: `MailController` (11 path var), `MailCreateRequest`/`MailSendRequest`/`MailUpdateRequest`, `RecipientBatchRequest.empIds: List<EmployeeId>`, `MailResponse` + `MailComponentDto` inner records (`createdBy: UserId`, `rootMailId/parentMailId: MailId`, `mailTypeId: MailTypeId`, dst), `MailMapper` (hapus semua manual encode). Fix bug struktural `MailMapper.toAuditDto:73` (`createdBy` di-encode pakai token `Mail.class`) | `mail-service-lsr` (3b) | [x]    |

⚠️ **Catatan kontrak 4a**: Klien yang sebelumnya menerima sqid 'salah'
untuk `createdBy` (encoded with `Mail.class`) sekarang menerima sqid valid
(encoded with `User.class`). Sqid lama tidak akan decode kembali — bukan
breaking change (sebelumnya juga tidak bisa decode), tapi catat di release
note.

⚠️ **Stress test seam**: Wave ini menguji list-of-sqid (`empIds`),
multiple wrapper types dalam satu DTO, dan kompiler-tolak class-mismatch.

---

## Wave 5 — Sweep & Guard

| # | Beads | Tipe | Slice | 🔒 Blocked by | Status |
|---|---|---|---|---|---|
| 5a | `mail-service-8hr` | AFK · P3 | Sweep: `grep encoder.decode/encode` di `controller/` + mapper = 0 hit. ArchUnit rule (opsional) cegah controller/mapper memanggil `SqidsEncoder` langsung. Konfirmasi ADR-010 status final | `mail-service-een` (4a) | [x] |

---

## Critical Path (jalur linear)

```
0a (infra)
   │
   ▼
1a (QuickMessage)
   │
   ▼
2a (MessageTemplate + master types)
   │
   ▼
3a (Attachment ⚠️) ── 3b (Publication)
                          │
                          ▼
                       4a (Mail + Recipient ⚠️)
                          │
                          ▼
                       5a (Sweep)
```

Tidak ada paralelisasi — chain linear dipilih sengaja agar setiap wave
membuktikan pola sebelum lanjut. Total: **7 issues, 7 wave** (1 issue per
wave), bisa selesai bertahap tanpa long-lived branch.

---

## Side-Effect Bugs yang Ikut Hilang (struktural)

Tidak perlu issue terpisah — efek samping dari migrasi seam:

| Bug | Lokasi | Lenyap di Wave |
|---|---|---|
| `createdBy` di-encode dengan token `Mail.class` (mestinya `User.class`) | `MailMapper.toAuditDto:73` | 4a |
| `(int) encoder.decode(...)` — BIGINT truncated | `MailAttachmentController:62` | 3a |
| Sqid bypass total — id mentah masuk service | `MessageTemplateController` | 2a |
| Sqid invalid → HTTP 500 (semestinya 400) | semua controller | 0a |

---

## Rekomendasi Eksekusi

1. **Wave 0 dulu, jangan lanjut sebelum infra hijau.** Test integration
   `SqidIdConverter` + `SqidJsonModule` + advice 400 harus bukti seam
   berfungsi sebelum modul apa pun di-migrasi.
2. **Wave 1 (QuickMessage)** sebagai canary. Kalau pola gagal di
   QuickMessage (mis. Jackson serializer tidak engaged), stop dan
   refactor infra — jangan rambat ke modul lain.
3. **Wave 2 koordinasi FE** untuk kontrak `MessageTemplateController`
   yang berubah.
4. **Wave 4 koordinasi FE** untuk perubahan kontrak `createdBy` di
   `MailComponentDto.MailAuditInfoDto`.
5. **Wave 5 (sweep)** wajib — tanpa ini, bisa terjadi residual call site
   yang menggagalkan locality.

---

## Cross-reference

- Beads list: `bd ready` (pick), `bd show <id>` (detail)
- ADR: [`docs/adr/010-opaque-external-id-typed-wrappers.md`](./adr/010-opaque-external-id-typed-wrappers.md)
- Domain term: `### SqidId (Opaque External Id)` di [`CONTEXT.md`](../CONTEXT.md)
- Memori beads: `sqid-seam-adr10` (recall lintas-sesi via `bd memories sqid`)
- Bilingual ID/EN — ikuti convention proyek.

# Plan: Core Mail — Draft & Update (CRUD)

> **Urutan: TAHAP 3A-1 — kerjakan pertama di antara core mail**
> Fondasi mail. Send (`03b`) bergantung pada file ini selesai.

---

## Fungsi

Mengurus pembuatan surat baru sebagai **Draft** dan update isi draft sebelum dikirim.

---

## Alur Kerja: Membuat Surat (Draft)

```
POST /api/mail/messages
  Body: CreateMailRequest {
    tempId,             ← UUID sementara (dari frontend saat form dibuka)
    date, type, category, subject, content, note, maxResponseDate,
    rootId, parentId,   ← 0 jika surat baru; isi jika reply
    noSuratMasuk, asalSuratMasuk, tglSuratMasuk,
    tujuanSuratKeluar, penerimaSuratKeluar,
    send                ← boolean: true = langsung kirim setelah simpan
  }

MailMessageService.create(request, userId):  @Transactional
  1. Insert ke tabel `mail` → status = DRAFT
  2. Update mail_recipient: ganti mail_id = tempId → mailId baru
  3. Update attachments: ganti ref_id = tempId → mailId baru
  4. Hitung attachmentQty dari tabel attachments
  5. Tentukan threading:
     - rootId == 0 → m_root_id = mailId (surat baru jadi root)
     - rootId != 0 → m_root_id = rootId, m_parent_id = parentId
  6. Update mail record (qty + threading)
  7. Insert sys_user_task (userId=sender, folder=DRAFT, read=READ)
  8. Rebuild m_to_str: query penerima → "nama (jabatan); ..." → update mail
  9. Jika send=true → delegasi ke MailSendService.send(mailId)

Response: MailMessageDto { mailId, status, number, ... }
```

> [!IMPORTANT]
> **Seluruh alur di atas harus dalam satu `@Transactional`.** Jika langkah manapun gagal, semua rollback.

---

## Alur Kerja: Update Surat (Draft)

```
PUT /api/mail/messages/{id}
  Body: UpdateMailRequest { ... field yang sama dengan create ... }

MailMessageService.update(id, request, userId):  @Transactional
  1. findById(id) → 404 jika tidak ada
  2. Hitung attachmentQty terbaru
  3. Update semua field isi surat di tabel mail
     ⚠️ JANGAN update createdAt / createdBy
  4. Rebuild m_to_str dari penerima terkini
  5. Jika send=true → delegasi ke MailSendService.send(id)

Response: MailMessageDto
```

> [!WARNING]
> **Jangan update `createdAt` / `createdBy` saat update.**
> Ini adalah bug di kode lama (`m_created_date` ikut di-overwrite saat UPDATE).
> Di Spring Boot, gunakan `@CreatedDate` — otomatis hanya di-set saat INSERT.

---

## Konsep Temp ID (Penting!)

> [!NOTE]
> Saat user membuka form surat baru, surat **belum ada di database**.
> Frontend men-generate `tempId` (UUID v4) di awal dan menggunakannya sebagai referensi sementara untuk:
> - `mail_recipient.mail_id`
> - `attachments.ref_id`
>
> Saat `POST /api/mail/messages` dipanggil, service mengganti semua referensi `tempId` → `mailId` yang baru.

---

## Endpoints

| HTTP | URL                       | Deskripsi                              |
|------|---------------------------|----------------------------------------|
| POST | `/api/mail/messages`      | Buat surat (draft atau langsung kirim) |
| PUT  | `/api/mail/messages/{id}` | Update surat                           |

---

## Package

```
controller/ MailMessageController.java
service/    MailMessageService.java
repository/ MailRepository.java
            UserTaskRepository.java
dto/        CreateMailRequest.java
            UpdateMailRequest.java
            MailMessageDto.java
```

# Plan: Core Mail — Kirim Surat & Penomoran

> **Urutan: TAHAP 3A-2 — setelah `03a-core-mail-crud.md` selesai**
> Send dipanggil oleh create/update (03a), jadi service ini harus sudah tersedia.

---

## ⚡ Wajib: Gunakan Context7

> [!IMPORTANT]
> Query Context7 sebelum implementasi:
> - `"Spring Boot @Transactional propagation"`
> - `"Spring Data JPA saveAll batch performance"`
> - `"Spring Data JPA @Lock pessimistic write"`
> - `"Spring Boot sequence number generation database"`

---

## Bagian A: Kirim Surat

### Fungsi
Mengubah status surat dari DRAFT → SENT, memindahkan ke folder Sent sender, dan mendistribusikan ke inbox semua penerima.

### Alur Kerja

```
MailSendService.send(mailId, userId):  @Transactional

  1. Ambil mail dari DB, validasi status = DRAFT
  2. Generate nomor surat (delegasi ke MailNumberingService)
  3. Update mail: status = SENT, m_no = nomor yang dihasilkan
  4. Update sys_user_task sender: folder DRAFT → SENT
  5. Query semua penerima dari mail_recipient
  6. Bangun List<UserTask> untuk setiap penerima:
     { userId, mailId, folder=INBOX, readStatus=UNREAD, mailCreatedDate=now() }
  7. userTaskRepository.saveAll(userTasks)  ← batch insert, BUKAN loop satu per satu

Response: MailMessageDto { status=SENT, number=... }
```

> [!IMPORTANT]
> **Langkah 7 wajib menggunakan `saveAll()`** — bukan loop `save()`.
> Aktifkan JDBC batching di `application.yml`:
> ```yaml
> spring:
>   jpa:
>     properties:
>       hibernate:
>         jdbc:
>           batch_size: 50
>         order_inserts: true
>         order_updates: true
> ```

---

## Bagian B: Generate Nomor Surat

### Fungsi
Membangkitkan nomor surat sesuai format template dari `sys_reference`, dengan sequence yang atomic dan bebas race condition.

### Format Nomor
Template contoh: `{org_code}/{type}/{seq}/{m_cat}/{MR}/{YYYY}`

Placeholder yang perlu diganti:
| Placeholder | Nilai |
|---|---|
| `#org_code#` | Kode organisasi dari config |
| `#type#` | Jenis surat (I = Internal) |
| `#MR#` | Bulan Roma (I, II, III, ...) |
| `#YYYY#` | Tahun 4 digit |
| `#m_cat#` | Kode kategori surat |
| `#seq#` | Nomor urut (auto-increment, 3 digit) |

### Alur Kerja

```
MailNumberingService.generate(mailId, categoryCode):

  1. Ambil template dari sys_reference WHERE code = {client.ref_code}
  2. Replace semua placeholder kecuali #seq#
  3. Ambil sequence dengan LOCK:
     @Lock(LockModeType.PESSIMISTIC_WRITE)
     findByTaskAndPrefix(TASK_MAIL, prefix)
     increment → save
  4. Replace #seq# dengan nilai sequence (zero-padded, 3 digit)
  5. Return nomor jadi
```

> [!IMPORTANT]
> **Sequence harus atomic.** Gunakan `@Lock(LockModeType.PESSIMISTIC_WRITE)` pada query sequence agar tidak ada dua surat dengan nomor yang sama (race condition di lingkungan concurrent).

> [!TIP]
> Alternatively, gunakan `SELECT ... FOR UPDATE` via native query atau database SEQUENCE object (jika migrate ke PostgreSQL) yang lebih ringan dari row-level lock.

---

## Endpoints

| HTTP | URL | Deskripsi |
|---|---|---|
| POST | `/api/mail/messages/{id}/send` | Kirim surat yang sudah jadi draft |

> Kirim juga bisa dipicu dari create/update (`send=true` di request body `03a`).

---

## Package

```
service/    MailSendService.java
            MailNumberingService.java
repository/ SequenceRepository.java     ← untuk atomic numbering
            UserTaskRepository.java
dto/        (shared dengan 03a)
```

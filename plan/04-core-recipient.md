# Plan: Core — Penerima Surat (Recipient Management)

> **Urutan pengerjaan: TAHAP 3A (parallel dengan 03-core-mail)**
> Bergantung pada Tahap 1 (Infrastructure) selesai.

---

## ⚡ Wajib: Gunakan Context7

> [!IMPORTANT]
> Sebelum menulis kode di layer ini, query Context7 dengan topik:
> - `"Spring Data JPA saveAll batch insert"`
> - `"Spring Boot REST bulk delete endpoint"`
> - `"Spring Data JPA @Modifying @Query"`
> - `"Spring Boot @RequestBody List validation"`

---

## Deskripsi

Layer ini mengurus seluruh operasi pada entitas **Penerima Surat** (`mail_recipient`): tambah, hapus, salin penerima, dan update data penerima.

---

## Konsep Temp ID

> [!IMPORTANT]
> **Pahami Temp ID Pattern sebelum mengimplementasi layer ini.**
>
> Ketika user membuka form surat baru, surat belum disimpan ke database (belum punya `mail_id`).
> Namun user bisa langsung menambahkan penerima dan lampiran.
> Sistem lama menggunakan `get_temp_id(user_id)` untuk membuat ID sementara.
>
> **Di Spring Boot, gunakan UUID v4 sebagai tempId yang di-generate di frontend atau saat inisiasi form.**
> - `mail_recipient.mail_id` diisi dengan tempId (UUID)
> - `attachments.ref_id` diisi dengan tempId yang sama
> - Saat surat disimpan (`POST /api/mail/messages`), service akan update semua referensi tempId → mailId yang sesungguhnya.

---

## 1. Tambah Satu Penerima

### Alur Kerja
```
Client → POST /api/mail/messages/{mailId}/recipients
  Body: { empId, circulation }
  Note: mailId bisa berupa UUID temp (surat belum disimpan)

MailRecipientService.addRecipient(mailId, empId, circulation, userId):
  1. Query data employee dari tabel employee + position
  2. Insert mail_recipient:
     { mail_id=mailId, user_id, emp_id, emp_name, pos_id, pos_name, circulation }
  3. Return MailRecipientDto
```

---

## 2. Tambah Banyak Penerima (Batch)

### Alur Kerja
```
Client → POST /api/mail/messages/{mailId}/recipients/batch
  Body: { empIds: [1,2,3,...], circulation }

MailRecipientService.addMultiRecipients(mailId, empIds, circulation):
  1. Query semua data employee sekaligus:
     employeeRepository.findAllById(empIds)  ← satu query, bukan N query
  2. Build List<MailRecipient> dari hasil query
  3. recipientRepository.saveAll(recipients)  ← satu batch insert
  4. Return { success, mailId, count }
```

> [!IMPORTANT]
> **Ini adalah perbaikan kritis dari kode lama.**
> Kode lama melakukan N kali insert satu per satu dalam loop.
> Di Spring Boot, wajib gunakan `saveAll()` untuk batch insert.
> Pastikan `spring.jpa.properties.hibernate.jdbc.batch_size=50` dikonfigurasi di `application.yml` agar batch benar-benar aktif di level JDBC.

---

## 3. Hapus Penerima

### Alur Kerja
```
DELETE /api/mail/recipients/{id}        ← hapus satu
DELETE /api/mail/recipients?ids=1,2,3  ← hapus banyak

MailRecipientService.deleteRecipients(ids):
  1. recipientRepository.deleteAllByIdIn(ids)  ← satu query DELETE
  2. Return 204 No Content
```

> [!TIP]
> Gunakan `@Modifying @Query("DELETE FROM MailRecipient r WHERE r.id IN :ids")` untuk batch delete yang efisien.

---

## 4. Query Penerima

### Alur Kerja
```
GET /api/mail/messages/{mailId}/recipients

MailRecipientService.getRecipients(mailId):
  1. recipientRepository.findByMailId(mailId)
  2. Return List<MailRecipientDto>
```

---

## 5. Update Data Penerima

### Alur Kerja
```
PUT /api/mail/recipients/{id}
  Body: { circulation, ... field yang boleh diubah }

MailRecipientService.updateRecipient(id, request):
  1. findById(id) — throw 404 jika tidak ada
  2. Update field yang berubah
  3. save(recipient)
  4. Return MailRecipientDto
```

> [!WARNING]
> **Di kode lama, `Recipient()` adalah satu method yang menangani QUERY dan UPDATE.**
> Di Spring Boot, ini **wajib** dipisah menjadi dua endpoint terpisah (GET dan PUT).
> Satu method satu tanggung jawab.

---

## 6. Salin Penerima (Copy)

### Alur Kerja: Copy dari Satu Surat
```
POST /api/mail/messages/{newMailId}/recipients/copy-from/{refMailId}

MailRecipientService.copyRecipients(newMailId, refMailId):
  1. Query semua penerima dari refMailId
  2. Buat entri baru untuk newMailId (clone, ganti mail_id)
  3. recipientRepository.saveAll(clones)
  4. Return count
```

### Alur Kerja: Copy dari Seluruh Thread
```
POST /api/mail/messages/{newMailId}/recipients/copy-thread/{refMailId}

MailRecipientService.copyThreadRecipients(newMailId, refMailId):
  1. Dari refMailId, ambil m_root_id
  2. Query semua penerima dari semua mail yang punya m_root_id yang sama
  3. Deduplicate berdasarkan user_id (satu user cukup satu entri)
  4. saveAll ke newMailId
```

---

## 7. Endpoints

| HTTP | URL | Deskripsi |
|---|---|---|
| GET | `/api/mail/messages/{mailId}/recipients` | Daftar penerima |
| POST | `/api/mail/messages/{mailId}/recipients` | Tambah satu penerima |
| POST | `/api/mail/messages/{mailId}/recipients/batch` | Tambah banyak penerima |
| DELETE | `/api/mail/recipients/{id}` | Hapus satu penerima |
| DELETE | `/api/mail/recipients?ids=` | Hapus banyak penerima |
| PUT | `/api/mail/recipients/{id}` | Update penerima |
| POST | `/api/mail/messages/{newId}/recipients/copy-from/{refId}` | Salin penerima |
| POST | `/api/mail/messages/{newId}/recipients/copy-thread/{refId}` | Salin penerima satu thread |

---

## 8. Package Structure

```
com.yourapp.mail
├── controller/
│   └── MailRecipientController.java
├── service/
│   └── MailRecipientService.java
├── repository/
│   └── MailRecipientRepository.java
└── dto/
    ├── AddRecipientRequest.java
    ├── AddMultiRecipientsRequest.java
    ├── UpdateRecipientRequest.java
    └── MailRecipientDto.java
```

# Plan: Core — Inbox & Manajemen Surat

> **Urutan pengerjaan: TAHAP 3B (setelah 03-core-mail selesai)**
> Bergantung pada Mail dan Recipient sudah bisa berjalan.

---

## ⚡ Wajib: Gunakan Context7

> [!IMPORTANT]
> Sebelum menulis kode di layer ini, query Context7 dengan topik:
> - `"Spring Data JPA @Modifying update query"`
> - `"Spring Boot PATCH endpoint partial update"`
> - `"Spring Security get current user from SecurityContext"`
> - `"Spring Data JPA delete by criteria"`

---

## Deskripsi

Layer ini mengurus **operasi manajemen surat setelah surat diterima**: menandai dibaca, memindahkan antar folder, menghapus ke trash, restore, mengosongkan trash, dan membaca counter badge.

---

## 1. Tandai Dibaca (`setRead`)

### Fungsi
Menandai surat sebagai sudah dibaca **dan** memindahkannya ke folder Read.

### Alur Kerja
```
PATCH /api/mail/messages/{id}/read

MailInboxService.setRead(mailId, originFolder, userId):
  1. Cari record di sys_user_task
     WHERE user_id = userId AND tm_id = mailId
  2. Update: read_status = READ, read_date = now()
  3. Update: folder_id = FOLDER_READ
  4. Return 200 OK
```

---

## 2. Flag Dibaca (`flagRead`)

### Fungsi
Hanya update status baca di `sys_user_task` — **tidak** memindahkan folder. Digunakan saat user membuka surat untuk preview cepat.

### Alur Kerja
```
PATCH /api/mail/tasks/{taskId}/flag-read

MailInboxService.flagRead(taskId, userId):
  1. Cari UserTask WHERE id = taskId AND user_id = userId
  2. Update: read_status = READ, read_date = now()
  3. Return 200 OK
```

> [!TIP]
> Pertimbangkan untuk menggabungkan `flagRead` dan `setRead` menjadi satu endpoint dengan parameter `mode=flag|move`. Ini menyederhanakan API surface.

---

## 3. Pindah Folder (`move`)

### Alur Kerja
```
PATCH /api/mail/messages/{id}/move
  Body: { toFolderId, fromFolderId }

MailInboxService.moveMail(mailId, toFolderId, fromFolderId, userId):
  1. Cari UserTask WHERE user_id = userId AND tm_id = mailId AND folder_id = fromFolderId
  2. Update: folder_id = toFolderId
  3. Return 200 OK
```

---

## 4. Hapus ke Trash (`delMail`)

### Alur Kerja
```
DELETE /api/mail/messages/{id}?restoreFolderId={currentFolderId}

MailInboxService.softDeleteMail(mailId, restoreFolderId, userId):
  1. Cari UserTask WHERE user_id = userId AND tm_id = mailId
  2. Update: restore_folder_id = restoreFolderId, folder_id = FOLDER_DELETED
  3. Return 204 No Content
```

> [!TIP]
> `restoreFolderId` adalah folder asal surat sebelum dihapus, sehingga user bisa restore ke tempat yang benar. Simpan di kolom `restore_folder_id` di `sys_user_task`.

---

## 5. Restore dari Trash

### Alur Kerja
```
PATCH /api/mail/messages/{id}/restore

MailInboxService.restoreMail(mailId, userId):
  1. Cari UserTask WHERE user_id = userId AND tm_id = mailId AND folder_id = FOLDER_DELETED
  2. Ambil restore_folder_id
  3. Update: folder_id = restore_folder_id, restore_folder_id = null
  4. Return 200 OK
```

---

## 6. Kosongkan Trash (`empty_trash`)

### Alur Kerja
```
DELETE /api/mail/trash

MailInboxService.emptyTrash(userId):
  1. Hard delete semua sys_user_task WHERE user_id = userId AND folder_id = FOLDER_DELETED
  2. Return 204 No Content
```

> [!WARNING]
> Ini adalah **operasi destruktif permanen**. Pertimbangkan menambahkan konfirmasi di level API (parameter `?confirm=true`) atau middleware audit log sebelum delete dieksekusi.

---

## 7. Counter Surat Belum Dibaca (`getcounter`)

### Fungsi
Mengambil jumlah surat belum dibaca per folder untuk ditampilkan sebagai badge di UI sidebar.

### Alur Kerja
```
GET /api/mail/counters

MailInboxService.getCounters(userId):
  1. Query: SELECT folder_id, COUNT(*) FROM sys_user_task
            WHERE user_id = userId AND read_status = UNREAD
            GROUP BY folder_id
  2. Map hasil ke MailCounterDto { folderId, unreadCount }
  3. Return List<MailCounterDto>
```

> [!TIP]
> Endpoint ini dipanggil sangat sering (polling UI). Pertimbangkan:
> - Cache dengan TTL pendek (5-10 detik per user) menggunakan Redis
> - Atau push-based via WebSocket / SSE untuk mengurangi polling

---

## 8. Endpoints

| HTTP | URL | Deskripsi |
|---|---|---|
| PATCH | `/api/mail/messages/{id}/read` | Tandai dibaca + pindah ke Read folder |
| PATCH | `/api/mail/tasks/{taskId}/flag-read` | Hanya flag dibaca |
| PATCH | `/api/mail/messages/{id}/move` | Pindah folder |
| DELETE | `/api/mail/messages/{id}` | Soft delete ke Trash |
| PATCH | `/api/mail/messages/{id}/restore` | Restore dari Trash |
| DELETE | `/api/mail/trash` | Kosongkan Trash |
| GET | `/api/mail/counters` | Counter unread per folder |

---

## 9. Package Structure

```
com.yourapp.mail
├── controller/
│   └── MailInboxController.java
├── service/
│   └── MailInboxService.java
├── repository/
│   └── UserTaskRepository.java      ← shared dengan core-mail
└── dto/
    ├── MoveMailRequest.java
    └── MailCounterDto.java
```

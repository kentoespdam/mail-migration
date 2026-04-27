# Plan: Core Mail — Query, Pencarian & Laporan

> **Urutan: TAHAP 3A-3 — bisa parallel dengan 03a dan 03b**
> Tidak ada ketergantungan write operation, hanya read.

---

## ⚡ Wajib: Gunakan Context7

> [!IMPORTANT]
> Query Context7 sebelum implementasi:
> - `"Spring Data JPA Specification dynamic query"`
> - `"Spring Data JPA Page Pageable count query"`
> - `"Spring Boot Criteria API join fetch"`
> - `"Spring Data JPA @Query countQuery"`
> - `"Spring Boot projection interface DTO"`

---

## 1. Baca Surat di Folder (`readFolder`)

### Fungsi
Mengambil daftar surat dalam satu folder dengan dukungan pagination, sorting, filter tanggal, dan pencarian keyword.

### Alur Kerja

```
GET /api/mail/folders/{folderId}/messages
  Params: page, size, sort, keyword, sdate, edate, threaded

MailFolderQueryService.readFolder(folderId, params, userId):

  Build Specification<UserTask>:
  ├── WHERE sys_user_task.user_id = userId
  ├── AND sys_user_task.folder_id = folderId
  ├── JOIN mail m ON m.m_id = ut.tm_id
  ├── JOIN mail_category, mail_type, sys_user (LEFT JOIN)
  ├── Jika folderId ∈ [INBOX, READ, DELETED, PERSONAL]:
  │     LEFT JOIN mail_recipient (filter user_id)
  │     LEFT JOIN sys_reference (sirkulasi)
  ├── Jika folderId == DELETED:
  │     LEFT JOIN mail_folder (restore_folder_name)
  ├── Jika sdate && edate:
  │     (m_date BETWEEN sdate AND edate) OR read_status = UNREAD
  │     ⚠️ Surat UNREAD selalu tampil meskipun di luar range tanggal
  └── Jika keyword:
        OR (subject, content, sender, recipient LIKE '%keyword%')

  Query count → total rows
  Query data  → Page<MailSummaryDto>

  Post-processing:
  → Jika keyword → highlight keyword dalam hasil (wrap <mark>keyword</mark>)
  → Jika threaded=true → delegasi ke MailThreadingService.buildTree()

Response: Page<MailSummaryDto> ATAU MailThreadRootDto
```

> [!WARNING]
> **Filter tanggal memiliki aturan khusus:** surat yang belum dibaca (`read_status = UNREAD`) **selalu tampil** walaupun di luar rentang tanggal yang dipilih. Ini adalah aturan bisnis, bukan bug.

> [!TIP]
> Untuk menghindari duplikasi query COUNT dan SELECT (bug di kode lama), gunakan `Page<T>` dari Spring Data.
> Jika ada multiple JOIN yang kompleks, pisahkan `countQuery` di `@Query`:
> ```java
> @Query(value = "SELECT ut FROM UserTask ut JOIN ...",
>        countQuery = "SELECT COUNT(ut) FROM UserTask ut JOIN ... " )
> ```

---

## 2. Pencarian Global (`find`)

### Fungsi
Mencari surat **lintas semua folder** milik user berdasarkan kriteria tertentu.
Berbeda dari `readFolder` yang hanya mencari dalam satu folder.

### Alur Kerja

```
GET /api/mail/messages/search
  Params: q, type, category, sdate, edate, page, size

MailSearchService.search(request, userId):
  → Build Specification dinamis berdasarkan parameter yang ada
  → Query dengan JOIN ke mail + sys_user_task (filter user_id)
  → Return Page<MailSummaryDto>
```

---

## 3. Tracking Distribusi (`trackMail`)

### Fungsi
Melihat siapa saja yang sudah menerima dan membaca surat ini.

### Alur Kerja

```
GET /api/mail/messages/{id}/tracking

MailTrackingService.track(mailId, userId):
  → Query mail_recipient JOIN sys_user_task
    WHERE mail_id = mailId
  → Return List<MailTrackingDto> {
      recipientName, posName, readStatus, readDate
    }
```

---

## 4. Status Baca per Penerima (`getReadStatus`)

### Fungsi
Melihat status baca tiap penerima untuk kebutuhan UI atau laporan.

### Alur Kerja

```
GET /api/mail/messages/{id}/read-status

MailTrackingService.getReadStatus(mailId):
  → Query: join mail_recipient + sys_user_task
  → Return List<RecipientReadStatusDto>
```

---

## 5. Data Laporan (`read/report`)

### Fungsi
Mengambil data surat untuk kebutuhan laporan/report (bukan inbox browsing).

### Alur Kerja

```
GET /api/mail/reports/messages
  Params: sdate, edate, type, category, status, page, size

MailReportService.report(params, userId):
  → Query dengan filter yang lebih bebas (tidak terikat pada folder)
  → Return Page<MailReportDto>
```

---

## Endpoints

| HTTP | URL | Deskripsi |
|---|---|---|
| GET | `/api/mail/folders/{folderId}/messages` | Baca surat di folder |
| GET | `/api/mail/messages/search` | Pencarian global |
| GET | `/api/mail/messages/{id}/tracking` | Tracking distribusi |
| GET | `/api/mail/messages/{id}/read-status` | Status baca per penerima |
| GET | `/api/mail/reports/messages` | Data untuk laporan |

---

## Package

```
controller/ MailFolderQueryController.java
            MailSearchController.java
            MailReportController.java
service/    MailFolderQueryService.java
            MailSearchService.java
            MailTrackingService.java
            MailReportService.java
specification/
            MailSpecification.java       ← Specification<UserTask> dinamis
repository/ (shared: MailRepository, UserTaskRepository)
dto/        MailSummaryDto.java
            MailSearchRequest.java
            MailTrackingDto.java
            RecipientReadStatusDto.java
            MailReportDto.java
```

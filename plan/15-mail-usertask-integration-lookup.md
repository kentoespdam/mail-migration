# Plan 15 — Integrasi `Mail` ↔ `UserTask`, CQRS `UserTask`, & Endpoint Lookup/Detail/Tracking

## Konteks

`UserTask` (`sys_user_task`) sudah berperan sebagai *daftar tugas / inbox per-user*: setiap baris mengikat `userId` ↔ `mailId` ↔ `folderId` ↔ `readStatus`. `MailRecipient` adalah daftar penerima level domain (TO/CC/BCC) per `Mail`. Keduanya saling melengkapi:

- `MailRecipient` = sumber kebenaran *siapa penerima sah* sebuah mail (audit, circulation type).
- `UserTask` = proyeksi *operasional* per-user (folder, read state, soft delete personal, restore).

Prompt meminta `UserTask` dipakai sebagai pintu filter "mail milik user login" untuk endpoint baru. Selaras dengan arah arsitektur yang sudah ada (lihat `UserTaskRepository`, `MailFolderQueryService`).

## Temuan Arsitektur Saat Ini

1. `UserTask` belum punya pemisahan CQRS sendiri. Saat ini dipakai langsung dari `UserTaskRepository` oleh service lain (`MailFolderQueryService`, `MailFolderCommandService`, `MailCommandService`). Ini menyalahi konvensi proyek (*setiap modul domain = `CommandService` + `QueryService`*).
2. Saat mail dikirim (`MailCommandService.sendMail` / `.send`), `MailRecipient` dibuat untuk tiap penerima. **Belum dipastikan** `UserTask` ikut dibuat di transaksi yang sama; kemungkinan dilakukan oleh listener `MailSentEvent`. Ini titik integrasi paling rawan dan harus diaudit dahulu.
3. `UserTaskRepository` sudah menyediakan finder yang relevan (`findByUserIdAndMailId`, `findActiveByUserIdAndMailId`, `findAllInTrashByMailId`, `countByUserIdAndFolderIdAndReadStatus`, `relocateMails`, dst).
4. `MailFolderQueryService.getMailsInFolder()` mendekati *lookup per user per folder*, tetapi proyeksi DTO-nya bukan bentuk ringkas yang diminta prompt (`circulationName` tidak ada).
5. `MailController` sudah punya `/{id}/tracking`, `/{id}/thread`, `/{id}/read-status`. Belum ada varian *by id* yang otomatis menelusuri ke `rootMail.id` lalu memuat seluruh mail dengan `m_root_id` yang sama.
6. `Mail` punya field denormalisasi `mailDate`, `subject`, `createdByName`, `maxResponseDate`, `mailType`, `mailCategory` — semua field lookup tersedia di entity, **kecuali** `circulationName` yang harus diambil dari `MailRecipient.circulation` user login dan diterjemahkan via enum `CirculationType`.
7. Pagination ada **dua pattern** di proyek ini:
   - **`PagedResponse<T>`** (custom record) — dipakai `MailController.search`, `MailController.report`. Disuplai dari kolom `count().over()` JOOQ window function via `PagedResponse.of(content, request, totalCount)`.
   - **`PagedModel<T>`** (Spring `org.springframework.data.web.PagedModel`) — dipakai semua master controller (`MailTypeController.findAll`, `MailCategoryController.findAll`, `DocumentTypeController.findAll`, `AllowedFileTypeController.findAll`, `QuickMessageController.findAll`). Service mengembalikan `Page<T>` (Spring) lalu controller membungkus: `new PagedModel<>(queryService.findAll(params))`. Request memakai `JpaPageRequest` (subclass `PageRequest` dengan `toPageable()` + `allowedSorts()` + `defaultSort()`).
   - Sesuai instruksi user, plan ini menggunakan **`PagedModel<T>`** (pattern master controller) untuk endpoint lookup. Cache `Page<T>` Spring mentah tetap dilarang (memori `cache-redis-pada-cacheconfig`); kalau perlu cache, simpan bentuk POJO/list, jangan `Page<T>`.

## Keputusan Desain (hasil klarifikasi sebelumnya)

| Area                            | Keputusan                                                                                                                                                                                                                                                                    |
|---------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Scope lookup                    | Param `folderId` opsional. Kosong → seluruh `UserTask` user yang `folderId NOT IN (DELETED, PURGED)` dan mail-nya `status = SENT`. Diisi → filter ke folder itu.                                                                                                             |
| `circulationName`               | Bersumber dari `MailRecipient.circulation` baris milik user login (TO/CC/BCC). Untuk mail di mana user login adalah *creator*, label khusus (lihat keputusan terbuka).                                                                                                       |
| Tracking                        | Endpoint menerima `id` mail apa pun → resolver mengambil `rootMailId` dari mail tsb (jika `rootMail` null, gunakan `id` itu sendiri) → fetch semua mail dengan `m_root_id = rootMailId` (atau `m_id = rootMailId` untuk root itu sendiri). Tanpa filter user.                |
| Pagination                      | `PagedModel<T>` (Spring `org.springframework.data.web.PagedModel`) mengikuti pattern `MailTypeController.findAll`. Service mengembalikan `Page<T>`, controller bungkus dengan `new PagedModel<>(...)`. Request DTO turunan `JpaPageRequest`. Hindari cache `Page<T>` mentah. |
| Flag read/unread                | Field tunggal `isRead: boolean` di `MailLookupResponse`, bersumber dari `UserTask.readStatus` user login.                                                                                                                                                                    |
| Auto mark-read                  | Endpoint detail otomatis menandai `UserTask.readStatus=READ` + `readDate=now` untuk user login saat dipanggil. Side-effect tulis di GET diakui dan didokumentasikan. Endpoint `POST /{id}/read` lama tetap ada untuk kompatibilitas.                                         |
| Label `circulationName` creator | `SENDER` saat user login = `m_created_by` dan tidak ada baris `mail_recipient` miliknya.                                                                                                                                                                                     |
| Path tracking                   | **Ganti perilaku** `GET /{id}/tracking` lama: id boleh non-root → server resolve `rootMailId`, lalu kembalikan seluruh mail dalam thread. Breaking change untuk konsumen lama; perlu disepakati di catatan rilis.                                                            |
| Akses `UserTaskRepository`      | Wajib lewat `UserTaskCommandService` / `UserTaskQueryService`. Repository JPA `UserTask` hanya di-inject di kedua service tersebut. Service domain lain (`MailFolderCommandService`, `MailCommandService`, dll) berhenti meng-inject `UserTaskRepository` setelah migrasi.   |
| Detail access policy            | User boleh akses detail kalau: (a) `m.m_created_by = userId` (creator), atau (b) ada `UserTask` aktif untuk pasangan user-mail. Auto mark-read hanya berjalan ketika kondisi (b) terpenuhi (creator tanpa `UserTask` tidak men-trigger update).                              |
| Status lookup                   | Hanya mail dengan `m.m_status = SENT`. Draft tidak ikut di lookup.                                                                                                                                                                                                           |
| PR strategy                     | Dua PR terpisah: PR-1 refactor CQRS `UserTask` + migrasi pemanggilan; PR-2 endpoint lookup/detail/tracking + DTO baru. PR-2 dependent pada PR-1 merge dulu.                                                                                                                  |

## Tujuan

1. Refactor `UserTask` ke pola CQRS-lite (`UserTaskCommandService` + `UserTaskQueryService`) sebagai fondasi yang konsisten dengan modul lain.
2. Pastikan kontrak integrasi `Mail` → `UserTask` solid (idempoten, transaksional, tidak bisa drift).
3. Sediakan tiga endpoint baru:
   - `GET` lookup — daftar mail per-user (PagedResponse).
   - `GET` detail — detail mail + attachment.
   - `GET` tracking — daftar mail satu thread, di-resolve dari `id` mail manapun ke `rootMail.id`.

## Langkah Tingkat Tinggi

### Fase 1 — Pisahkan `UserTask` jadi CQRS-lite

1. Inventarisasi semua pemanggilan `UserTaskRepository` di service lain. Klasifikasikan tiap call ke salah satu sisi:
   - **Write side** (Command): create dari send/recipient, update `readStatus`, move folder, soft-delete, restore, purge, relocate.
   - **Read side** (Query): finder `findByUserIdAndMailId` untuk autorisasi, count unread, listing folder.
2. Buat `service/core/usertask/UserTaskCommandService` (JPA) yang memiliki tanggung jawab tunggal: memproyeksikan kejadian domain (mail terkirim, recipient ditambah, folder dipindah, mail dihapus, restore, purge) menjadi perubahan baris `UserTask`. API service ini berorientasi *aksi domain*, bukan CRUD baris.
3. Buat `service/core/usertask/UserTaskQueryService` (JOOQ) yang memiliki tanggung jawab tunggal: menjawab pertanyaan baca seputar `UserTask` — antara lain *lookup per-user*, *cek kepemilikan mail*, *count per-folder*, *thread ownership*. Pemanggil lama yang membutuhkan finder JPA tetap boleh memakai `UserTaskRepository` sebatas write/transactional invariant; tetapi semua *read* yang berorientasi tampilan harus melalui `UserTaskQueryService`.
4. Migrasikan pemanggilan eksisting:
   - `MailFolderQueryService` (read) → delegasi ke `UserTaskQueryService`.
   - `MailFolderCommandService` (delete/restore/move/purge) → delegasi ke `UserTaskCommandService`.
   - `MailCommandService.deleteMail/restoreMail/markRead` → delegasi ke `UserTaskCommandService`.
5. Pertahankan boundary CQRS: `UserTaskQueryService` **tidak** meng-inject repository JPA `UserTask`; `UserTaskCommandService` **tidak** meng-inject DSLContext. Tambahkan boundary test serupa `plan/07-publication-cqrs.md`.

### Fase 2 — Audit & Penguatan Integrasi `Mail` ↔ `UserTask`

1. Telusuri jalur send (`MailCommandService.sendMail` / `.send` / `MailSentEvent` listener): pastikan untuk setiap `MailRecipient` yang dibuat, ada `UserTask` di folder INBOX milik user terkait. Bila proyeksi dilakukan async, dokumentasikan eventual-consistency di endpoint lookup.
2. Identifikasi gap: kasus *recipient ditambah setelah mail terkirim* (`MailRecipientCommandService.addRecipient`, `addBatch`, `copyFrom`, `copyThread`) — apakah `UserTask` ikut dibuat? Pasti perlu, supaya filter konsisten.
3. Tetapkan invariant: `MailRecipient(mail, user)` ⇒ harus ada `UserTask(user, mail, INBOX)` (kecuali user = creator). Tulis sebagai dasar test integrasi.
4. Pastikan path destruktif user-scope (delete/purge/restore) memutar `UserTask` saja, bukan `Mail` global — fondasi agar lookup-by-user benar.
5. Tambahkan domain event opsional (`UserTaskProjectedEvent`) bila berguna untuk observability/notifikasi; tidak wajib.

### Fase 3 — Read Layer Lookup (sisi Query)

1. Tambah JOOQ query baru di `repository/core/jooq/usertask/` (atau modul read terdekat) yang melakukan *single round-trip* dengan join:
   - `JOIN sys_user_task ut ON ut.tm_id = m.m_id AND ut.user_id = :uid`
   - `LEFT JOIN mail_recipient r ON r.mail_id = m.m_id AND r.user_id = :uid`
   - `LEFT JOIN m_mail_type t ON t.id = m.m_type`
   - `LEFT JOIN m_mail_category c ON c.id = m.m_category`
   - filter: `m.m_status = SENT`, `ut.folder_id NOT IN (DELETED, PURGED)`; bila `folderId` diisi, override jadi `ut.folder_id = :folderId`.
   - proyeksi: `id, mailDate, createdByName, subject, type.name, category.name, r.circulation, m_max_response_date, ut.read_status`.
2. Mapper menghasilkan:
   - `circulationName` melalui enum `CirculationType` (TO/CC/BCC) atau label `SENDER` ketika `m.m_created_by = :uid` dan tidak ada baris `mail_recipient`.
   - `isRead: boolean` dari `ut.read_status` via enum `ReadStatus` (`READ.dbValue` → `true`, lainnya → `false`). Field ini dipakai frontend untuk badge unread / pewarnaan baris.
3. Pagination — ikuti pattern master controller (`MailTypeQueryService.findAll` + `MailTypeController.findAll`):
   - Repository JOOQ mengembalikan `Page<MailLookupResponse>` Spring. Total count diambil dari `count().over()` window function di query yang sama (single round-trip), atau query count terpisah bila lebih sederhana — pilih sesuai pola yang sudah dipakai di `MailTypeQueryRepository`.
   - Service `UserTaskQueryService.findInbox(...)` mengembalikan `Page<MailLookupResponse>`.
   - Controller membungkus dengan `new PagedModel<>(...)` (sama persis seperti `MailTypeController.findAll`).
   - Request DTO `MailLookupParams` extends `JpaPageRequest` → mengimplementasikan `allowedSorts()` (mis. `mailDate`, `subject`, `createdByName`) dan `defaultSort()` (`mailDate`).
4. Indexing: pastikan `idx_ut_user_folder` dan `idx_ut_user_mail` cukup. Bila tidak, tambahkan migrasi Flyway baru (V12+) untuk index gabungan yang mendukung filter folder + sort `mailDate DESC`.

### Fase 4 — Service Read API

1. Tambah method di `UserTaskQueryService`: *lookup per user* dengan parameter `MailPrincipal` + request DTO (folderId, paging, sort), mengembalikan `Page<MailLookupResponse>` Spring (controller akan membungkus jadi `PagedModel<MailLookupResponse>`).
2. Tambah method *cek kepemilikan/akses mail* yang dipakai endpoint detail untuk autorisasi (apakah user punya `UserTask` aktif untuk `mailId`, atau merupakan `m_created_by`).
3. Tambah method *resolve root id* yang menerima `mailId` dan mengembalikan `rootMailId` efektif (kalau `m_root_id` null → kembalikan id itu sendiri). Method ini dipakai endpoint tracking.
4. Tambah method *list mail by root id* yang mengembalikan `List<MailTrackingItemResponse>` urut `m_created_date ASC`. Tanpa filter user (sesuai keputusan), tetapi data diharapkan ringkas (subset field tracking).
5. Detail mail: tambah method di `MailQueryService` (atau service detail terdekat) yang menggabungkan `MailResponse` + attachments + recipients ringkas. Autorisasi delegasi ke `UserTaskQueryService` (point 2). Detail mail juga harus menyertakan attachment list lengkap (id, nama, ukuran, mime).

### Fase 5 — Controller (HTTP layer)

Tambahkan ke `MailController` (atau pisahkan ke `MailLookupController` agar tidak terlalu besar):

| Method | Path                          | Deskripsi                                                                                                                                                                                                                                        |
|--------|-------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `GET`  | `/api/v1/mails/lookup`        | Lookup per user; query param `folderId?`, paging via `JpaPageRequest` (`page`, `size`, `sortBy`, `sortDir`). Return `PagedModel<MailLookupResponse>`. Controller signature mengikuti `MailTypeController.findAll`.                               |
| `GET`  | `/api/v1/mails/{id}`          | Detail mail + attachments. Auto mark-read untuk user yang punya `UserTask`.                                                                                                                                                                      |
| `GET`  | `/api/v1/mails/{id}/tracking` | **Mengganti perilaku endpoint lama**. Server resolve `rootMailId` dari `id` (jika `m_root_id` null → id itu sendiri), lalu kembalikan seluruh mail dengan `m_root_id = rootMailId` (termasuk root) urut `m_created_date ASC`. Tanpa filter user. |

**Catatan konflik path detail:** belum ada `GET /{id}` di `MailController` (yang ada `PUT /{id}` & `POST /{id}/...`). Aman ditambah. Verifikasi tidak bentrok dengan path literal `/search`, `/report`, `/lookup` — Spring routing menyelesaikan literal sebelum variable, jadi urutan tidak masalah, tapi pastikan path literal tidak tertangkap oleh `{id}`.

**Catatan breaking change tracking:** signature lama `GET /{id}/tracking` mengembalikan `List<MailTrackingResponse>` per recipient. Setelah migrasi, semantiknya berubah jadi *list mail dalam thread*. Konsumen lama (frontend) harus diberi tahu sebelum rilis. Pertimbangkan tag versi major di catatan rilis.

Otentikasi: semua endpoint `@PreAuthorize("isAuthenticated()")` dan ambil `@AuthenticationPrincipal MailPrincipal`. Sqid encode/decode konsisten dengan controller lain.

### Fase 6 — Caching

1. Detail: cache key `mail:detail:{userId}:{mailId}:v1`, TTL 5–10 menit. Invalidasi pada update/delete mail/recipient.
2. Lookup: **tidak** di-cache (per-user, sering berubah karena read flag). Andalkan DB index.
3. Tracking by root: cache key `mail:tracking-root:{rootId}:v1`, TTL 5 menit; invalidate saat ada reply baru ke root tersebut.
4. Patuhi pelajaran dari `plan/08-publication-storage-load-fix.md` dan memori `cache-redis-pada-cacheconfig-pakai-genericjacksonjsonredisse`: **jangan** simpan `Page<T>` Spring mentah di Redis (akan dideserialisasi sebagai `LinkedHashMap`). Bila ingin cache hasil lookup, simpan list konten + `totalElements` di POJO terpisah, atau cache pada level repository sebelum dibungkus `Page`.

### Fase 7 — Testing

1. **CQRS boundary test** untuk `UserTask` (mengikuti pola test pemisahan di Publication CQRS): verifikasi `UserTaskQueryService` tidak inject repository JPA dan `UserTaskCommandService` tidak inject `DSLContext`.
2. **Integration test integrasi**: kirim mail ke 2 user → user A & B masing-masing melihat mail di lookup; verifikasi `circulationName` benar (TO/CC/creator-label).
3. **Authorization test**: user C (bukan recipient, bukan creator) → detail mengembalikan 403/404.
4. **Folder filter test**: pindahkan `UserTask` user A ke folder personal → lookup tanpa folderId tetap menampilkan; lookup dengan folderId INBOX tidak menampilkan.
5. **Tracking resolver test**: kirim id reply (bukan root) → endpoint tetap mengembalikan seluruh thread (root + reply lain). Kirim id root → tetap dapat seluruh thread. Kirim id mail tanpa thread → kembalikan 1 item (root = dirinya sendiri).
6. **Soft-delete test**: user softDelete mail → lookup default tidak menampilkannya; lookup dengan folderId DELETED menampilkannya.
7. **Pagination shape test**: response lookup punya struktur `PagedModel<T>` Spring (`content`, `page` object dengan `size/number/totalElements/totalPages`), konsisten dengan response `GET /api/v1/mail-types`.
8. **`isRead` flag test**: untuk user yang `UserTask.readStatus = UNREAD` → response `isRead = false`; setelah `markRead()` (atau setelah memanggil endpoint detail dengan auto mark-read) → lookup berikutnya menampilkan `isRead = true` untuk mail yang sama.

### Fase 8 — Migrasi & Backfill (jika perlu)

Jika audit Fase 2 menemukan `MailRecipient` historis tanpa pasangan `UserTask`, siapkan job satu kali untuk menyinkronkan: setiap `MailRecipient(mail, user)` tanpa `UserTask` → sisipkan `UserTask` di INBOX dengan `mail_created_date = mail.m_created_date`.

## Risiko & Catatan

1. **Drift `MailRecipient` ↔ `UserTask`** adalah risiko utama. Pastikan semua jalur tulis (send, addRecipient, addBatch, copyFrom, copyThread) memakai `UserTaskCommandService` agar invariant terjaga.
2. `circulationName` untuk *creator* perlu konvensi; sepakati istilah dengan frontend (lihat keputusan terbuka).
3. Endpoint tracking tanpa filter user berarti siapa pun yang punya id valid (sqid) bisa membaca thread. Sqid sulit ditebak namun **bukan** kontrol akses; bila kebijakan keamanan mengetat, tambahkan check kepemilikan via `UserTaskQueryService` di tiap node thread.
4. Refactor CQRS `UserTask` menyentuh banyak service. Lakukan bertahap dan jaga kompatibilitas method signature publik agar test existing tidak banjir merah sekaligus.

## Output yang Dihasilkan

- 2 service baru: `UserTaskCommandService`, `UserTaskQueryService`.
- 1 DTO baru `MailLookupResponse` dengan field: `id` (sqid), `mailDate`, `createdByName`, `subject`, `type` (name), `category` (name), `circulationName` (TO/CC/BCC/SENDER), `maxResponseDate`, **`isRead` (boolean dari `UserTask.readStatus` user login, untuk tampilan frontend)**.
- 1 DTO request `MailLookupParams` (extends `JpaPageRequest`, override `allowedSorts()` & `defaultSort()`).
- 1 DTO ringkas tracking item.
- 1 query JOOQ baru untuk lookup + (kemungkinan) 1 migrasi index Flyway.
- 3 endpoint controller baru.
- Method baru di `MailQueryService` untuk detail.
- Test boundary CQRS + integration tests skenario di Fase 7.
- (Kondisional) skrip backfill `UserTask`.

## Keputusan yang Sudah Ditutup

Semua keputusan utama telah dijawab pada sesi ini dan dirangkum di tabel "Keputusan Desain" di atas. Ringkasan:

1. `circulationName` creator → `SENDER`.
2. Flag `isRead: boolean` ikut di `MailLookupResponse`.
3. Detail endpoint melakukan auto mark-read (untuk user yang punya `UserTask`).
4. Path tracking `GET /{id}/tracking` lama diganti perilakunya (breaking change, dicatat di rilis).
5. Akses `UserTaskRepository` JPA wajib lewat command/query service.
6. Detail access: creator selalu boleh; user lain harus punya `UserTask` aktif.
7. Lookup hanya menampilkan mail `status = SENT`.
8. Eksekusi: dua PR terpisah, refactor CQRS dulu sebelum fitur endpoint.

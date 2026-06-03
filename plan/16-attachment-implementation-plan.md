# Plan 19 — Implementasi Attachment dengan CQRS-lite

## Konteks

Sistem saat ini memiliki layanan `PublicationFileStorageService` untuk penyimpanan file publikasi, tetapi belum memiliki implementasi attachment untuk mail. `AttachmentResponse.java` sudah ada di DTO dengan field dasar (id, refType, refId, originalFilename, fileExt, fileSize, docNotes, uploadDate, uploadByName), namun belum ada entitas, repository, atau service yang terkait.

Sistem memerlukan implementasi attachment yang terintegrasi dengan arsitektur CQRS-lite, menggunakan struktur penyimpanan yang konsisten dengan sistem yang ada namun berbeda dengan publication (folder `mail/{yyyyMM}/` bukan `publik/{yyyyMM}/`).

## Temuan Arsitektur Saat Ini

1. `PublicationFileStorageService` sudah ada dan berfungsi dengan baik untuk publikasi, bisa diadaptasi untuk attachment dengan perubahan path penyimpanan
2. `AttachmentResponse.java` sudah ada di `dto/core/attachment/` dengan field dasar, namun belum ada entity, repository, atau service yang terkait
3. Tidak ada entitas `Attachment` di package `entity/core/`
4. Tidak ada repository JPA atau JOOQ untuk attachment
5. Tidak ada service `AttachmentCommandService` atau `AttachmentQueryService`
6. Tidak ada relasi antara `Mail` dan `Attachment` di entity `Mail.java`
7. Tidak ada endpoint untuk upload/download attachment
8. `FileStorage.java` di `util/` memiliki fungsi dasar penyimpanan file, tapi tidak spesifik untuk attachment
9. `MailRecipient` sudah ada, tapi belum ada konsep attachment terkait mail

## Keputusan Desain

| Area                            | Keputusan                                                                                                                                                                                                                                                                    |
|---------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Struktur Data                   | Buat entitas `Attachment` dengan field: ID, nama file asli, ekstensi, ukuran, catatan, tanggal upload, pengupload, referensi entitas terkait (type & ID), status. Soft delete melalui `RecordStatus`.                                                                           |
| Penyimpanan File                | Gunakan struktur folder `mail/{yyyyMM}/` (bukan `publik` seperti publication). Implementasi penanganan nama file unik untuk menghindari konflik. Validasi path untuk mencegah directory traversal.                                                                           |
| Integrasi dengan Mail           | Tambahkan relasi antara Mail dan Attachment (1:N). Attachment akan dihapus bersamaan dengan Mail (jika Mail dihapus). Pastikan hanya pengguna yang memiliki akses ke Mail bisa mengakses attachment.                                                                           |
| CQRS Implementation             | Buat `AttachmentCommandService` (JPA) untuk operasi write (upload, delete). Buat `AttachmentQueryService` (JOOQ) untuk operasi read (download, detail). Repository hanya di-inject di service masing-masing.                                                                 |
| Keamanan                        | Validasi izin akses sebelum mengakses file. Pastikan hanya pengguna yang memiliki akses ke Mail bisa mengakses attachment. Gunakan `MailPrincipal` untuk autentikasi.                                                                                                    |
| DTO                             | Gunakan `AttachmentResponse` sebagai DTO query. Tambahkan `AttachmentRequest` untuk operasi upload. Tambahkan `AttachmentDetailResponse` untuk detail attachment.                                                                                                               |
| Cache                           | Cache detail attachment dengan key `attachment:detail:{userId}:{attachmentId}:v1`, TTL 5-10 menit. Invalidasi pada update/delete attachment. Tidak cache hasil lookup (per-user, sering berubah).                                                                              |
| Path Endpoint                   | `POST /api/v1/mails/{mailId}/attachments` untuk upload<br>`GET /api/v1/mails/{mailId}/attachments` untuk daftar attachment<br>`GET /api/v1/mails/{mailId}/attachments/{attachmentId}` untuk detail attachment<br>`DELETE /api/v1/mails/{mailId}/attachments/{attachmentId}` untuk hapus |
| PR Strategy                     | Dua PR terpisah: PR-1 refactor CQRS `Attachment` (service, entity, repository) + migrasi penyimpanan; PR-2 endpoint attachment + DTO baru. PR-2 dependent pada PR-1 merge dulu.                                                                                               |

## Tujuan

1. Implementasi attachment sebagai bagian dari arsitektur CQRS-lite, konsisten dengan modul lain
2. Integrasi dengan sistem mail yang ada (relasi Mail-Attachment)
3. Penyimpanan file yang aman dengan struktur folder `mail/{yyyyMM}/`
4. Endpoint yang aman untuk upload, download, dan hapus attachment
5. Dokumentasi yang jelas untuk penggunaan attachment dalam sistem

## Langkah Tingkat Tinggi

### Fase 1 — Implementasi Entity dan DTO

1. Buat entitas `Attachment` di `entity/core/Attachment.java` dengan field: ID, originalFilename, fileExt, fileSize, docNotes, uploadDate, uploadByName, refType, refId, status
2. Tambahkan relasi di `Mail.java` dengan `@OneToMany(mappedBy = "mail", cascade = CascadeType.ALL, orphanRemoval = true)`
3. Pastikan `Attachment` memiliki soft delete melalui `RecordStatus`
4. Buat DTO `AttachmentRequest` untuk upload (originalFilename, fileExt, fileSize, docNotes)
5. Buat DTO `AttachmentDetailResponse` untuk detail (id, originalFilename, fileExt, fileSize, docNotes, uploadDate, uploadByName)

### Fase 2 — Implementasi Storage Service

1. Buat `AttachmentFileStorageService.java` di `service/core/attachment/` dengan modifikasi dari `PublicationFileStorageService`:
   - Ubah path penyimpanan dari `publik` menjadi `mail`
   - Pertahankan logika penanganan nama file unik
   - Validasi path untuk mencegah directory traversal
2. Buat `StorageProperties` untuk konfigurasi base path storage
3. Implementasi validasi folder storage saat inisialisasi

### Fase 3 — Implementasi CQRS Service

1. Buat `AttachmentCommandService.java` di `service/core/attachment/` untuk operasi write:
   - `uploadAttachment(MultipartFile file, UUID mailId, MailPrincipal principal)` untuk upload
   - `deleteAttachment(UUID attachmentId, UUID mailId, MailPrincipal principal)` untuk hapus
   - Validasi izin akses sebelum operasi
2. Buat `AttachmentQueryService.java` di `service/core/attachment/` untuk operasi read:
   - `getAttachmentsByMailId(UUID mailId, MailPrincipal principal)` untuk daftar attachment
   - `getAttachmentDetail(UUID attachmentId, UUID mailId, MailPrincipal principal)` untuk detail attachment
3. Buat repository JPA `AttachmentRepository.java` di `repository/core/jpa/` untuk operasi write
4. Buat repository JOOQ `AttachmentQueryRepository.java` di `repository/core/jooq/` untuk operasi read

### Fase 4 — Implementasi Controller

1. Tambahkan endpoint di `MailAttachmentController.java`:
   - `POST /api/v1/mails/{mailId}/attachments` untuk upload
   - `GET /api/v1/mails/{mailId}/attachments` untuk daftar attachment
   - `GET /api/v1/mails/{mailId}/attachments/{attachmentId}` untuk detail attachment
   - `DELETE /api/v1/mails/{mailId}/attachments/{attachmentId}` untuk hapus
2. Pastikan semua endpoint menggunakan `@PreAuthorize("isAuthenticated()")` dan `@AuthenticationPrincipal MailPrincipal`
3. Implementasi validasi izin akses sebelum operasi

### Fase 5 — Testing

1. **Unit test** untuk `AttachmentFileStorageService` (penyimpanan, penanganan nama unik, validasi path)
2. **Unit test** untuk `AttachmentCommandService` dan `AttachmentQueryService` (operasi CRUD, validasi izin)
3. **Integration test** untuk endpoint attachment (upload, daftar, detail, hapus)
4. **Authorization test** (user tanpa akses ke mail tidak bisa mengakses attachment)
5. **Caching test** (detail attachment di-cache, invalidasi pada update/delete)

### Fase 6 — Dokumentasi

1. Update `memory.md` dengan penjelasan struktur attachment
2. Tambahkan catatan di `BUSINESS_LOGIC_IMPLEMENTATION.md` tentang alur attachment
3. Update API documentation untuk endpoint attachment
4. Tambahkan dokumentasi di `CLAUDE.md` tentang cara menggunakan attachment

## Risiko & Catatan

1. **Konflik nama file** jika tidak dihandle dengan baik. Pastikan sistem penamaan unik (dengan counter) bekerja dengan baik.
2. **Masalah izin akses** ke folder storage. Pastikan izin filesystem benar dan aplikasi memiliki akses tulis ke folder `mail/`.
3. **Performa jika banyak attachment** di satu Mail. Pastikan query dan cache diatur dengan baik.
4. **Relasi Mail-Attachment** harus dijaga konsistensinya. Pastikan attachment dihapus ketika Mail dihapus.
5. **Penyimpanan file** harus aman dari directory traversal. Pastikan validasi path dilakukan dengan benar.
6. **Caching detail attachment** perlu diatur dengan baik agar tidak menyebabkan kebocoran data antar user.

## Output yang Dihasilkan

- 1 entitas baru `Attachment.java` di `entity/core/`
- 1 DTO baru `AttachmentRequest.java` dan `AttachmentDetailResponse.java` di `dto/core/attachment/`
- 1 service baru `AttachmentFileStorageService.java` di `service/core/attachment/`
- 1 service baru `AttachmentCommandService.java` dan `AttachmentQueryService.java` di `service/core/attachment/`
- 1 repository JPA `AttachmentRepository.java` di `repository/core/jpa/`
- 1 repository JOOQ `AttachmentQueryRepository.java` di `repository/core/jooq/`
- 1 controller baru `MailAttachmentController.java` di `controller/core/`
- 1 migrasi Flyway untuk tabel attachment
- Test untuk semua komponen attachment
- Update dokumentasi di `memory.md`, `BUSINESS_LOGIC_IMPLEMENTATION.md`, dan `CLAUDE.md`

## Keputusan yang Sudah Ditutup

1. Struktur folder penyimpanan attachment: `mail/{yyyyMM}/`
2. Attachment dihapus bersamaan dengan Mail (jika Mail dihapus)
3. Validasi izin akses sebelum mengakses attachment
4. Caching detail attachment dengan TTL 5-10 menit
5. Endpoint attachment: 
   - `POST /api/v1/mails/{mailId}/attachments` untuk upload
   - `GET /api/v1/mails/{mailId}/attachments` untuk daftar attachment
   - `GET /api/v1/mails/{mailId}/attachments/{attachmentId}` untuk detail attachment
   - `DELETE /api/v1/mails/{mailId}/attachments/{attachmentId}` untuk hapus
6. Implementasi CQRS dengan service terpisah untuk command dan query
7. Relasi Mail-Attachment menggunakan `@OneToMany` dengan cascade ALL dan orphanRemoval true
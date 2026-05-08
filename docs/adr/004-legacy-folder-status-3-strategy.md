# ADR 004: Legacy `folder_status=3` Migration Strategy

## Status
Approved (Resolves `mail-service-aaj`)

## Context
Aplikasi legacy SmartOffice memiliki 609 folder pribadi di tabel `mail_folder` dengan `folder_status=3` (soft-deleted). Terdapat ketidakpastian apakah folder-folder ini masih memiliki referensi di `sys_user_task.folder_id` (orphaned tasks).

User Story 15 pada `PRD-migrasi-mail-disposisi.md` menyatakan: *"As a User, I want delete folder hanya jika kosong (tidak ada UserTask di dalamnya), so that tidak kehilangan referensi surat."* 

Jika kita membiarkan folder tersebut atau menghapusnya tanpa memindahkan isinya, terdapat risiko surat/tugas menjadi tidak terakses (orphan) bagi pengguna, yang melanggar prinsip integritas data pada sistem baru.

## Decision
Kami memutuskan untuk menggunakan strategi **Clean DB & Move Tasks**:
1. Memindahkan semua `sys_user_task` yang merujuk pada `folder_id` dengan `folder_status=3` ke folder sistem **DELETED** (ID 6).
2. Menghapus secara permanen (hard-delete) semua record di `mail_folder` yang memiliki `folder_status=3`.

## Reasoning
1. **Integritas Referensi Tugas (Story 15)**: Dengan memindahkan tugas ke folder "Deleted Items" (folder 6), pengguna tetap dapat menemukan surat tersebut di Trash alih-alih kehilangan referensinya sama sekali. Ini memenuhi janji "tidak kehilangan referensi surat".
2. **Pembersihan Database (DB Hygiene)**: Record dengan `status=3` di legacy adalah sampah data (junk) yang sudah dihapus oleh user. Menghapusnya secara permanen mengurangi beban baris di tabel `mail_folder` (yang sudah memiliki 1.8M+ baris di tabel terkait lainnya) dan menyederhanakan query.
3. **Kesesuaian dengan Java Entity**: `MailFolder.java` sudah menggunakan `@SQLRestriction("folder_status = 1")`. Membiarkan data `status=3` tetap ada di DB hanya akan menambah record yang tidak pernah di-load oleh JPA, namun tetap membebani index.
4. **Sederhana & Aman**: Strategi ini dapat dieksekusi melalui satu Flyway migration script yang idempotent dan aman selama `FOREIGN_KEY_CHECKS=0` (karena tidak ada FK formal pada `folder_id` di legacy).

## Consequences
- **User Experience**: Pengguna mungkin melihat surat-surat lama yang sebelumnya "hilang" (karena foldernya dihapus di legacy) muncul kembali di Trash ("Deleted Items"). Ini dianggap sebagai perilaku positif (data recovery).
- **Data Cleanup**: 609 baris akan dihapus dari `mail_folder`.
- **Consistency**: Memastikan bahwa semua `folder_id` di `sys_user_task` merujuk pada folder yang aktif (status=1) atau folder sistem yang valid.

## References
- `docs/PRD-migrasi-mail-disposisi.md` User Story 15 & Open Question #2.
- `src/main/java/id/perumdamts/mail/entity/core/MailFolder.java` (SQLRestriction).
- `src/main/java/id/perumdamts/mail/enums/SystemFolder.java` (ID 6 for DELETED).
- GH Issue #64 / `mail-service-aaj`.

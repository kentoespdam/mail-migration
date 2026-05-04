# Plan 24 — Entity-Legacy Alignment & Foreign Key Introduction

> **Status:** Draft (hasil grilling session 2026-05-04)
> **Strategy:** Level B — additive Flyway, entity ikut legacy, NO partitioning (Opsi 1).
> **Audience:** Developer junior/AI yang akan eksekusi migrasi & refactor entity.
> **Bahasa:** Indonesia + istilah teknis Inggris.

---

## 1. Executive Summary

### 1.1 Konteks
Mail-service adalah migrasi dari aplikasi legacy **SmartOffice** (PHP CodeIgniter, MariaDB 11.1). Schema legacy belum punya foreign key sama sekali — semua relasi hanya dijaga di kode. Tujuan plan ini:

1. **Layer DB:** tambah FK constraint pada relasi yang aman (orphan-free atau bisa di-cleanup).
2. **Layer entity:** sesuaikan JPA entity dengan schema legacy (rename `@Column`, drop field yang tidak ada di legacy, tambah field baru via additive `ALTER TABLE`).
3. **Migrasi data:** zero-downtime saat sync data legacy → schema baru. Tidak boleh rename/drop kolom legacy.

### 1.2 Konstrain Utama (Final)
| Konstrain | Keputusan |
|---|---|
| Strategy | **Level B** — Additive Flyway + Entity ikut legacy |
| Partitioning | ❌ **TIDAK PAKAI** (Opsi 1) — fokus ke FK + index |
| Drop kolom legacy | ❌ Tidak boleh — biarkan deprecated |
| Rename kolom legacy | ❌ Tidak boleh — pakai `@Column(name=...)` di entity |
| ADD kolom baru | ✅ Boleh untuk audit/state baru |
| Polymorphic FK | ❌ Skip FK — pakai trigger validasi opsional |
| Data lama policy | Tetap monolitik, tidak ada archive table |

### 1.3 Temuan Kritis
- **532,969 mail rows** dengan `m_parent_id` dangling (30% dataset). Cleanup wajib sebelum FK self-ref.
- **389,302 attachments** dengan `ref_id` dangling ke `mail` (8%). Polymorphic FK skip — orphan dibiarkan / cleanup batch.
- **44,289 sys_user_task rows** dengan `folder_id = -1` (PURGED) — kolom `folder_id` tidak boleh di-FK karena nilai sentinel ini.
- **6 tabel legacy** belum ada entity: `mail_archive_notif`, `mail_archive_notif_log`, `mail_respontime`, `mail_org_statistic`, `mail_category_statistic`, `msg_template`.
- **`pesan_singkat`** masih MyISAM — wajib convert ke InnoDB sebelum FK/index FULLTEXT.
- **`attachments`** punya composite PK `(id, upload_date)` di legacy — entity pakai PK `id` saja → akan diubah ke single PK setelah verifikasi tidak ada duplikat `id`.

### 1.4 Keluaran Plan
1. Dokumentasi mapping entity ↔ legacy (section 4)
2. Daftar Flyway migration baru (V15..V30, section 5)
3. Daftar entity refactor (section 6)
4. Beads issue breakdown (section 7)
5. Risk register & rollback (section 8)

---

## 2. Inventarisasi Tabel Legacy

### 2.1 Tabel Mail-Related di SmartOffice

| Tabel Legacy | Rows | Engine | Charset | Status Entity |
|---|---:|---|---|---|
| `mail` | 1,805,122 | InnoDB | latin1 | ✅ ada (`Mail`) |
| `mail_recipient` | 2,238,758 | InnoDB | latin1 | ✅ ada (`MailRecipient`) |
| `mail_folder` | 1,820 | InnoDB | latin1 | ✅ ada (`MailFolder`) |
| `mail_archive` | 39,892 | InnoDB | latin1 | ✅ ada (`MailArchive`) — **schema mismatch berat** |
| `mail_archive_access` | 119,619 | InnoDB | latin1 | ✅ ada (`MailArchiveAccess`) — **type mismatch** |
| `mail_category` | 596 | InnoDB | latin1 | ✅ ada (`MailCategory`) |
| `mail_type` | 3 | InnoDB | latin1 | ✅ ada (`MailType`) |
| `attachments` | 5,088,036 | InnoDB | latin1 | ✅ ada (`Attachment`) — composite PK |
| `attachment_download_history` | 511,287 | InnoDB | latin1 | ✅ ada (`AttachmentDownloadHistory`) |
| `print_log` | 95,740 | InnoDB | latin1 | ✅ ada (`PrintLog`) |
| `area_publik` | 152 | InnoDB | latin1 | ✅ ada (`Publication`) |
| `jenis_dokumen` | 6 | InnoDB | latin1 | ✅ ada (`DocumentType`) — **type mismatch** |
| `pesan_singkat` | 29 | **MyISAM** | latin1 | ✅ ada (`QuickMessage`) |
| `sys_user_task` | 4,004,381 | InnoDB | latin1 | ✅ ada (`UserTask`) |
| `mail_archive_notif` | 40,922 | InnoDB | latin1 | ❌ **belum ada entity** |
| `mail_archive_notif_log` | 336,560 | InnoDB | latin1 | ❌ **belum ada entity** |
| `mail_respontime` | 233,449 | InnoDB | latin1 | ❌ **belum ada entity** |
| `mail_org_statistic` | 5,724 | InnoDB | latin1 | ❌ **belum ada entity** |
| `mail_category_statistic` | 5,020 | InnoDB | latin1 | ❌ **belum ada entity** |
| `msg_template` | 13 | InnoDB | latin1 | ❌ **belum ada entity** |

### 2.2 Tabel Yang Dibuat Aplikasi Baru (Tidak Ada di Legacy)

| Tabel | Status | Catatan |
|---|---|---|
| `mail_action_log` | ✅ entity ada (V14 migration) | Audit trail action surat — tabel baru |

---

## 3. Orphan Analysis (Snapshot 2026-05-04)

| Kandidat FK | Source → Target | Orphan | Severity | Cleanup Strategy |
|---|---|---:|---|---|
| `mail.m_type` → `mail_type.mail_type_id` | 549 | LOW | `UPDATE mail SET m_type=NULL WHERE m_type NOT IN (SELECT mail_type_id FROM mail_type);` |
| `mail.m_category` → `mail_category.mcat_id` | 691 | LOW | `UPDATE mail SET m_category=NULL WHERE m_category NOT IN (SELECT mcat_id FROM mail_category);` |
| `mail.m_root_id` → `mail.m_id` | 14 | LOW | `UPDATE mail SET m_root_id=NULL WHERE m_root_id NOT IN (SELECT m_id FROM mail);` |
| **`mail.m_parent_id`** → `mail.m_id` | **532,969** | **HIGH** | `UPDATE mail SET m_parent_id=NULL WHERE m_parent_id NOT IN (SELECT m_id FROM mail);` — **batch 10k, log progress** |
| `mail_recipient.mail_id` → `mail.m_id` | 37,756 | MEDIUM | `DELETE FROM mail_recipient WHERE mail_id NOT IN (SELECT m_id FROM mail);` — backup dulu |
| `mail_archive_access.mail_archive_id` → `mail_archive.ma_id` | 0 | ✅ | langsung FK |
| `mail_archive.ma_mcat_id` → `mail_category.mcat_id` | 202 | LOW | SET NULL |
| `attachments.ref_id` (mail) | 389,302 | HIGH | **SKIP FK** (polymorphic). Cleanup batch terpisah jika perlu. |
| `attachments.ref_id` (archive) | 3,952 | MEDIUM | SKIP FK. Cleanup batch terpisah. |
| `attachment_download_history.attachment_id` → `attachments.id` | 1,677 | LOW | DELETE orphan |
| `print_log.mail_id` → `mail.m_id` | 10 | LOW | DELETE orphan |
| `mail_folder.parent_folder_id` → `mail_folder.folder_id` | 0 | ✅ | langsung FK (kecuali parent=0 → biarkan magic value untuk system root) |
| `area_publik.type` → `jenis_dokumen.id` | 0 | ✅ | langsung FK |
| `mail_archive_notif_log.mail_archive_id` → `mail_archive.ma_id` | 0 | ✅ | langsung FK |
| `mail_respontime.orig_m_id` → `mail.m_id` | 5 | LOW | SET NULL |
| `mail_respontime.reply_m_id` → `mail.m_id` | 7 | LOW | SET NULL |
| `sys_user_task.tm_id` → `mail.m_id` | 57 | LOW | DELETE orphan |
| `sys_user_task.folder_id` → `mail_folder.folder_id` (excl -1) | 0 | ✅ | **FK NOT enforceable** karena `-1` (PURGED). Lihat 4.5. |

---

## 4. Mapping Entity ↔ Legacy + Action Plan

### 4.1 `Mail` ↔ `mail` (1.8M rows)

**Legacy schema** (lihat dump di section 9.1).

**Aksi entity:**
- Field `m_updated_date` di entity → kolom belum ada di legacy → **ADD COLUMN** via Flyway.
- Field `m_no` di entity (`mailNumber`) → legacy `m_no varchar(64)` — entity sekarang `length=100`. **Update entity → 64**, bukan ALTER table.
- Field `m_no_surat_masuk` length 100 (entity) vs 64 (legacy) → **update entity → 64**.
- Field `m_asal_surat_masuk` length 200 (entity) vs 128 (legacy) → **update entity → 128**.
- Field `m_tgl_surat_masuk` di entity = `String length=50` → legacy = `date`. **Ubah entity ke `LocalDate`**, hapus length.
- Field `m_tujuan_surat_keluar`, `m_penerima_surat_keluar` length 200 (entity) vs 128 (legacy) → update entity.
- Kolom legacy yang tidak di-map entity (deprecated, tetap exist di DB):
  - `m_rab_id INT` — relasi ke modul anggaran lama, tidak dipakai.
  - `m_ma_id INT` — relasi ke arsip lama, tidak dipakai.
  - **Aksi:** dokumentasikan di Javadoc class `Mail` sebagai "DEPRECATED legacy column".

**Aksi DB (Flyway V15):**
```sql
ALTER TABLE mail ADD COLUMN m_updated_date DATETIME NULL AFTER m_created_date;
UPDATE mail SET m_updated_date = m_created_date WHERE m_updated_date IS NULL;

-- Cleanup orphan FK precondition
UPDATE mail SET m_type = NULL WHERE m_type IS NOT NULL AND m_type NOT IN (SELECT mail_type_id FROM mail_type);
UPDATE mail SET m_category = NULL WHERE m_category IS NOT NULL AND m_category NOT IN (SELECT mcat_id FROM mail_category);
UPDATE mail SET m_root_id = NULL WHERE m_root_id IS NOT NULL AND m_root_id NOT IN (SELECT m_id FROM mail);
-- m_parent_id cleanup harus batch (532k rows) — pisahkan ke V16
```

**Aksi DB (Flyway V16):** batch cleanup `m_parent_id` (lihat section 5.2).

**Aksi DB (Flyway V17):** tambah FK
```sql
ALTER TABLE mail
  ADD CONSTRAINT fk_mail_type FOREIGN KEY (m_type) REFERENCES mail_type(mail_type_id) ON DELETE SET NULL,
  ADD CONSTRAINT fk_mail_category FOREIGN KEY (m_category) REFERENCES mail_category(mcat_id) ON DELETE SET NULL,
  ADD CONSTRAINT fk_mail_root FOREIGN KEY (m_root_id) REFERENCES mail(m_id) ON DELETE SET NULL,
  ADD CONSTRAINT fk_mail_parent FOREIGN KEY (m_parent_id) REFERENCES mail(m_id) ON DELETE SET NULL;
```

**Index tambahan untuk performa (gantinya partition):**
```sql
CREATE INDEX idx_mail_status_date ON mail(m_status, m_date);
CREATE INDEX idx_mail_created_by_date ON mail(m_created_by, m_created_date);
```

---

### 4.2 `MailRecipient` ↔ `mail_recipient` (2.2M rows)

**Aksi entity:**
- Drop field `is_read BOOLEAN` (redundant — sudah di `sys_user_task.read_status`).
- Drop field `folder_position INT` (tidak dibutuhkan).
- Pertahankan field `is_notified BOOLEAN` — perlu untuk anti-double-notif.
- Drop unique constraint `(mail_id, user_id)` di entity → **rebuild setelah cleanup duplikat** (Flyway).

**Aksi DB (Flyway V18):**
```sql
ALTER TABLE mail_recipient
  ADD COLUMN is_notified TINYINT(1) NOT NULL DEFAULT 0 AFTER sms;

-- Cleanup orphan
DELETE FROM mail_recipient WHERE mail_id NOT IN (SELECT m_id FROM mail); -- 37756 rows

-- Backfill is_notified=1 untuk row yang sudah pernah ada di mail_archive_notif_log atau read sudah=1 di sys_user_task
UPDATE mail_recipient mr
JOIN sys_user_task ut ON ut.tm_id = mr.mail_id AND ut.user_id = mr.user_id
SET mr.is_notified = 1 WHERE ut.read_status = 1;
```

**Aksi DB (Flyway V19):** dedupe + unique
```sql
-- Cek duplikat (mail_id, user_id) dulu
-- Jika ada, keep MIN(id)
DELETE mr1 FROM mail_recipient mr1
INNER JOIN mail_recipient mr2
  ON mr1.mail_id = mr2.mail_id
 AND mr1.user_id = mr2.user_id
 AND mr1.id > mr2.id;

ALTER TABLE mail_recipient
  ADD CONSTRAINT uq_recipient_mail_user UNIQUE (mail_id, user_id),
  ADD CONSTRAINT fk_recipient_mail FOREIGN KEY (mail_id) REFERENCES mail(m_id) ON DELETE CASCADE;
```

---

### 4.3 `MailArchive` ↔ `mail_archive` (39k rows)

**Aksi entity — RENAME @Column ke nama legacy:**

| Java field | Entity sebelum | Entity sesudah `@Column(name=...)` |
|---|---|---|
| `archiveDate` | `ma_date` | **`ma_mail_date`** |
| `mailId` | `ma_mail_id` | **`ma_ref_id`** |
| `category` (MailCategory) | `ma_category` | **`ma_mcat_id`** |
| `createdDate` | `ma_created_date` | **`ma_archive_date`** |
| `createdByName` | `ma_created_by_name` | **`ma_archive_by_name`** |
| `officeCode` | `ma_office_code` | **`office_code`** (tanpa prefix `ma_`!) |
| `location.rack` | `ma_rack` | **`ma_loc_rack`** |
| `location.shelf` | `ma_shelf` | **`ma_loc_tier`** (legacy pakai `tier`) |
| `location.box` | `ma_box` | **`ma_loc_box`** |
| `location.folderPosition` | `ma_folder_pos` | **DROP** (tidak ada di legacy) |
| `keywordFlag` | `ma_keyword_flag` | **`ma_keyword_index_flag`** |

**Field entity yang DIDROP** (tidak ada di legacy, tidak perlu):
- `createdBy Integer` → drop, andalkan `ma_archive_by_name`.
- `attachmentQty` → drop, query attachments table on-the-fly (atau materialize via view nanti).
- `year` → drop, derive `YEAR(ma_mail_date)` di service.
- `publishedAt` → drop, sama dengan `ma_archive_date`.

**Field entity yang DITAMBAH** (perlu sistem baru):
- `updatedDate LocalDateTime` → ADD COLUMN `ma_updated_date DATETIME`.

**Field LEGACY yang harus DITAMBAH ke entity** (sayang dibuang, masih dipakai aplikasi lama):
- `ma_mcat_type INT` → `mcatType Integer`
- `ma_mcat_code VARCHAR(32)` → `mcatCode String`
- `ma_org_code VARCHAR(16)` → `orgCode String`
- `ma_org_id INT` → `orgId Integer`
- `ma_ref_no VARCHAR(45)` → `refNo String`
- `ma_sent_to VARCHAR(128)` → `sentTo String`
- `ma_note VARCHAR(512)` → `note String`
- `ma_secret_type VARCHAR(45)` → `secretType String`
- `ma_loc_building INT` → `location.building Integer`
- `ma_loc_floor INT` → `location.floor Integer`
- `ma_loc_room INT` → `location.room Integer`
- `ma_keyword TEXT` → `keyword String` (full-text search candidate)

**Aksi DB (Flyway V20):**
```sql
ALTER TABLE mail_archive
  ADD COLUMN ma_updated_date DATETIME NULL AFTER ma_archive_date;
UPDATE mail_archive SET ma_updated_date = ma_archive_date;

UPDATE mail_archive SET ma_mcat_id = NULL
  WHERE ma_mcat_id IS NOT NULL
    AND ma_mcat_id NOT IN (SELECT mcat_id FROM mail_category);

ALTER TABLE mail_archive
  ADD CONSTRAINT fk_archive_category FOREIGN KEY (ma_mcat_id) REFERENCES mail_category(mcat_id) ON DELETE SET NULL;

-- ma_ref_id (mail link) — nullable, bisa archive standalone. Ada 0 orphan mungkin? cek dulu, sementara skip FK.
CREATE INDEX idx_archive_ref_mail ON mail_archive(ma_ref_id);
CREATE INDEX idx_archive_status_date ON mail_archive(ma_status, ma_mail_date);
CREATE INDEX idx_archive_org ON mail_archive(ma_org_id);
```

---

### 4.4 `MailArchiveAccess` ↔ `mail_archive_access` (119k rows)

**Aksi entity — sesuaikan dengan legacy:**
- `position_id INT` → tetap **INT**, tapi mapping ke legacy `pos_id varchar(20)`. **Perlu cleanup** dulu: cek apakah semua `pos_id` numerik, kalau iya `ALTER TABLE MODIFY pos_id INT`.
- Drop entity field: `access_level Integer`, `granted_date`, `granted_by`.
- Tambah field entity sesuai legacy:
  - `access String` (1 char: 'Y'/'N'/' ') → mapping ke `Boolean canAccess` via converter
  - `download String` → `Boolean canDownload`
  - `history String` → `Boolean canViewHistory`

**Aksi DB (Flyway V21):**
```sql
-- Step 1: Verify pos_id semua numerik
-- (run di pre-flight check, fail jika ada non-numeric)

-- Step 2: Convert column type
ALTER TABLE mail_archive_access
  MODIFY pos_id INT NOT NULL DEFAULT 0;

-- Step 3: Add FK
ALTER TABLE mail_archive_access
  ADD CONSTRAINT fk_archive_access_archive FOREIGN KEY (mail_archive_id) REFERENCES mail_archive(ma_id) ON DELETE CASCADE;
```

---

### 4.5 `UserTask` ↔ `sys_user_task` (4M rows)

**Schema sudah match.** Hanya perlu FK + cleanup.

**Aksi DB (Flyway V22):**
```sql
DELETE FROM sys_user_task WHERE tm_id NOT IN (SELECT m_id FROM mail); -- 57 rows

-- folder_id = -1 (PURGED) tidak bisa di-FK karena -1 sentinel value
-- Strategy: BIARKAN tanpa FK constraint, atau pindah ke tabel terpisah `sys_user_task_purged`

-- Pilihan A (rekomendasi): tambah CHECK constraint
ALTER TABLE sys_user_task
  ADD CONSTRAINT fk_usertask_mail FOREIGN KEY (tm_id) REFERENCES mail(m_id) ON DELETE CASCADE;

-- folder_id TIDAK di-FK karena ada -1 sentinel
-- Tambah CHECK: folder_id MUST be -1 OR exist di mail_folder
-- MariaDB CHECK constraint dengan subquery TIDAK didukung — skip.
-- Alternative: trigger BEFORE INSERT/UPDATE
```

**Tambahan plan-26 (opsional):** pindah row `folder_id = -1` ke tabel `sys_user_task_purged` lalu FK `folder_id` bisa diaktifkan.

---

### 4.6 `Attachment` ↔ `attachments` (5M rows) — composite PK

**Aksi DB (Flyway V23):**
```sql
-- Step 1: verifikasi tidak ada duplikat id (di-jalankan nanti, bukan sekarang)
SELECT id, COUNT(*) c FROM attachments GROUP BY id HAVING c > 1 LIMIT 5;

-- Step 2: jika 0 duplikat, drop composite PK
ALTER TABLE attachments
  DROP PRIMARY KEY,
  ADD PRIMARY KEY (id);

-- Step 3: TIDAK tambah FK pada ref_id (polymorphic)
-- Add CHECK opsional
ALTER TABLE attachments
  ADD CONSTRAINT chk_ref_type CHECK (ref_type IN (1, 2));

-- Cleanup orphan attachment_download_history
DELETE FROM attachment_download_history
  WHERE attachment_id NOT IN (SELECT id FROM attachments);

ALTER TABLE attachment_download_history
  ADD CONSTRAINT fk_dlhist_attachment FOREIGN KEY (attachment_id) REFERENCES attachments(id) ON DELETE CASCADE;
```

**Catatan:** verifikasi duplikat `attachments.id` ditunda (Q5.3). Migrasi V23 hanya boleh dijalankan setelah verifikasi.

---

### 4.7 `Publication` ↔ `area_publik` (152 rows)

**Aksi entity:** `status` tetap String enum (`PublicationStatus`).

**Aksi DB (Flyway V24):**
```sql
-- ALTER ENUM untuk match nilai entity ('DRAFT','PUBLISHED','DELETED')
-- Strategy: tambah nilai baru dulu, migrate data, lalu drop nilai lama
ALTER TABLE area_publik
  MODIFY status ENUM('Draft','Ditampilkan','DRAFT','PUBLISHED','DELETED') DEFAULT 'DRAFT';

UPDATE area_publik SET status='DRAFT' WHERE status='Draft';
UPDATE area_publik SET status='PUBLISHED' WHERE status='Ditampilkan';

ALTER TABLE area_publik
  MODIFY status ENUM('DRAFT','PUBLISHED','DELETED') NOT NULL DEFAULT 'DRAFT';

-- Tambah audit
ALTER TABLE area_publik
  ADD COLUMN created_at DATETIME NULL,
  ADD COLUMN updated_at DATETIME NULL;
UPDATE area_publik SET created_at = published_date, updated_at = published_date;

-- FK
ALTER TABLE area_publik
  ADD CONSTRAINT fk_publication_doctype FOREIGN KEY (type) REFERENCES jenis_dokumen(id) ON DELETE SET NULL;
```

---

### 4.8 `DocumentType` ↔ `jenis_dokumen` (6 rows)

**Aksi entity:** Pisahkan `DELETED` dari status enum → kolom `is_deleted BOOLEAN` terpisah.

**Aksi DB (Flyway V25):**
```sql
-- Status saat ini: int(11). Pisahkan deleted ke kolom baru.
ALTER TABLE jenis_dokumen
  ADD COLUMN status_new ENUM('ACTIVE','INACTIVE') NOT NULL DEFAULT 'ACTIVE' AFTER status,
  ADD COLUMN is_deleted TINYINT(1) NOT NULL DEFAULT 0 AFTER status_new;

UPDATE jenis_dokumen SET
  status_new = CASE WHEN status = 1 THEN 'ACTIVE' ELSE 'INACTIVE' END,
  is_deleted = CASE WHEN status = 0 THEN 1 ELSE 0 END;

-- Drop kolom lama? TIDAK — biarkan deprecated agar legacy app tetap jalan
-- Entity hanya map status_new + is_deleted

CREATE INDEX idx_doctype_active ON jenis_dokumen(status_new, is_deleted);
```

**Entity update:**
```java
@Enumerated(EnumType.STRING)
@Column(name = "status_new", nullable = false)
private RecordStatusActive status; // ACTIVE/INACTIVE only

@Column(name = "is_deleted", nullable = false)
private Boolean deleted = false;
```

`@SQLRestriction("is_deleted = 0")` ganti dari `status <> 'DELETED'`.

---

### 4.9 `QuickMessage` ↔ `pesan_singkat` (29 rows, MyISAM)

**Aksi DB (Flyway V26):**
```sql
ALTER TABLE pesan_singkat ENGINE=InnoDB;

ALTER TABLE pesan_singkat
  ADD COLUMN status ENUM('ACTIVE','INACTIVE') NOT NULL DEFAULT 'ACTIVE',
  ADD COLUMN is_deleted TINYINT(1) NOT NULL DEFAULT 0,
  ADD COLUMN created_date DATETIME NULL,
  ADD COLUMN updated_date DATETIME NULL;

UPDATE pesan_singkat SET created_date=NOW(), updated_date=NOW() WHERE created_date IS NULL;

ALTER TABLE pesan_singkat
  ADD FULLTEXT INDEX ft_pesan (pesan);
```

**Entity update:** sama strategi seperti DocumentType (pisah deleted).

---

### 4.10 `MailFolder` ↔ `mail_folder` (1.8k rows)

**Schema match.** FK self-ref (parent_folder_id) — perlu hati-hati karena `parent_folder_id = 0` untuk root system folder (ID 1, 10) tidak ada parent.

**Aksi DB (Flyway V27):**
```sql
-- parent_folder_id = 0 untuk root → tidak boleh FK
-- Solution: ubah ROOT/PERSONAL_ROOT parent_folder_id ke NULL
UPDATE mail_folder SET parent_folder_id = NULL WHERE parent_folder_id = 0;

-- Tapi schema legacy parent_folder_id NOT NULL — perlu MODIFY
ALTER TABLE mail_folder MODIFY parent_folder_id INT NULL;

UPDATE mail_folder SET parent_folder_id = NULL WHERE parent_folder_id = 0;

ALTER TABLE mail_folder
  ADD CONSTRAINT fk_folder_parent FOREIGN KEY (parent_folder_id) REFERENCES mail_folder(folder_id) ON DELETE RESTRICT;
```

**Entity update:** `parentFolderId` jadi nullable.

---

### 4.11 `MailCategory` ↔ `mail_category` & `MailType` ↔ `mail_type`

**Schema match.**

**Aksi DB (Flyway V28):**
```sql
ALTER TABLE mail_category
  ADD CONSTRAINT fk_mcat_mail_type FOREIGN KEY (mail_type_id) REFERENCES mail_type(mail_type_id) ON DELETE RESTRICT;
```

---

### 4.12 `PrintLog` ↔ `print_log`

**Aksi DB (Flyway V29):**
```sql
DELETE FROM print_log WHERE mail_id IS NOT NULL AND mail_id NOT IN (SELECT m_id FROM mail); -- 10 rows

-- auth_code legacy: varchar(32), entity: 100 → update entity ke 32
ALTER TABLE print_log
  ADD CONSTRAINT fk_printlog_mail FOREIGN KEY (mail_id) REFERENCES mail(m_id) ON DELETE CASCADE;
```

**Entity update:** `authCode` length=32, `username` length=128, `ipAddress` length=32.

---

## 5. Flyway Migration Roadmap (V15 → V30)

| Versi | File | Topik | Risk |
|---|---|---|---|
| V15 | `V15__mail_audit_column_and_master_fk_cleanup.sql` | mail.m_updated_date + cleanup orphan m_type/m_category/m_root_id | LOW |
| V16 | `V16__mail_parent_id_orphan_cleanup_batch.sql` | Batch cleanup 532k m_parent_id orphans | **HIGH** |
| V17 | `V17__mail_foreign_keys.sql` | FK mail.m_type, m_category, m_root_id, m_parent_id + index | MEDIUM |
| V18 | `V18__mail_recipient_add_is_notified_and_cleanup.sql` | ADD is_notified + delete 37k orphan | MEDIUM |
| V19 | `V19__mail_recipient_dedupe_and_fk.sql` | Dedupe (mail_id,user_id) + unique + FK | MEDIUM |
| V20 | `V20__mail_archive_align_legacy_columns.sql` | ADD ma_updated_date, FK ma_mcat_id, indexes | LOW |
| V21 | `V21__mail_archive_access_pos_id_int_and_fk.sql` | Convert pos_id varchar→int + FK | MEDIUM (data risk) |
| V22 | `V22__sys_user_task_cleanup_and_fk.sql` | Cleanup 57 orphan + FK tm_id | LOW |
| V23 | `V23__attachments_drop_composite_pk_and_dl_history_fk.sql` | PK simplification + FK download_history | MEDIUM (verify dup dulu) |
| V24 | `V24__publication_enum_audit_and_fk.sql` | Migrate ENUM + audit columns + FK | LOW |
| V25 | `V25__document_type_status_split.sql` | Split status int → enum + is_deleted | LOW |
| V26 | `V26__quick_message_innodb_audit_fulltext.sql` | MyISAM→InnoDB + audit + FULLTEXT | LOW |
| V27 | `V27__mail_folder_parent_nullable_and_fk.sql` | parent_folder_id nullable + FK | LOW |
| V28 | `V28__mail_category_fk_mail_type.sql` | FK mcat → mail_type | LOW |
| V29 | `V29__print_log_cleanup_and_fk.sql` | DELETE 10 orphan + FK | LOW |
| V30 | `V30__legacy_unmapped_tables_audit_columns.sql` | ADD audit columns ke 6 tabel baru (mail_archive_notif, dll) | LOW |

### 5.1 Pre-Flight Check (sebelum jalankan migration apapun)

Buat **script verifikasi** `scripts/preflight_v15_v30.sql`:
```sql
-- 1. Backup penting
SELECT 'BACKUP REQUIRED: mail, mail_recipient, sys_user_task, attachments' AS warning;

-- 2. Verify duplikat attachments.id
SELECT 'attachments.id duplicates' label, COUNT(*) cnt
FROM (SELECT id FROM attachments GROUP BY id HAVING COUNT(*) > 1) x;

-- 3. Verify pos_id numeric in mail_archive_access
SELECT 'pos_id non-numeric' label, COUNT(*) cnt
FROM mail_archive_access WHERE pos_id NOT REGEXP '^[0-9]+$';

-- 4. Verify mail_recipient (mail_id, user_id) duplicates
SELECT 'recipient duplicates' label, COUNT(*) cnt
FROM (SELECT mail_id, user_id FROM mail_recipient GROUP BY mail_id, user_id HAVING COUNT(*) > 1) x;
```

### 5.2 Batch Cleanup Strategy (V16)

532k orphan `mail.m_parent_id` cleanup tidak boleh `UPDATE ... WHERE NOT IN (...)` langsung — bisa lock table 30+ menit. Pakai batch:

```sql
DELIMITER $$
CREATE PROCEDURE cleanup_mail_parent_orphans()
BEGIN
  DECLARE done INT DEFAULT 0;
  DECLARE total INT DEFAULT 0;
  REPEAT
    UPDATE mail SET m_parent_id = NULL
    WHERE m_parent_id IS NOT NULL
      AND m_parent_id NOT IN (SELECT m_id FROM (SELECT m_id FROM mail) x)
    LIMIT 10000;

    SET total = total + ROW_COUNT();
    SET done = (ROW_COUNT() = 0);

    SELECT CONCAT('Cleaned: ', total, ' rows') AS progress;
  UNTIL done END REPEAT;
END$$
DELIMITER ;

CALL cleanup_mail_parent_orphans();
DROP PROCEDURE cleanup_mail_parent_orphans;
```

**Estimasi:** 532k / 10k batch = 54 iterations × 1-3s = **~3 menit** (di production database).

---

## 6. Entity Refactor Checklist

### 6.1 Entity yang HARUS di-update (lihat detail di section 4)

- [ ] `Mail` — sesuaikan length kolom + ADD `m_updated_date`
- [ ] `MailRecipient` — DROP `is_read`, `folder_position`; ADD `is_notified`
- [ ] `MailArchive` — RENAME @Column massal + ADD field legacy yang sebelumnya dibuang + DROP field tidak ada di legacy
- [ ] `MailArchiveAccess` — DROP `access_level`, `granted_date`, `granted_by`; ADD `access`, `download`, `history` (Boolean via converter)
- [ ] `Attachment` — ubah PK strategy ke single `id` (setelah verifikasi)
- [ ] `AttachmentDownloadHistory` — schema match, no change
- [ ] `MailFolder` — `parentFolderId` nullable
- [ ] `Publication` — tambah `createdAt`, `updatedAt`; converter ENUM legacy/baru
- [ ] `DocumentType` — split `status` jadi `RecordStatusActive` + `deleted Boolean`; update `@SQLRestriction`
- [ ] `QuickMessage` — split `status` + `deleted`; FULLTEXT search method
- [ ] `PrintLog` — sesuaikan length kolom
- [ ] `MailType`, `MailCategory` — schema match, no change
- [ ] `ArchiveLocation` — ADD `building`, `floor`, `room`; RENAME `shelf`→`tier`; DROP `folderPosition`

### 6.2 Entity BARU (6 tabel legacy yang belum ada entity)

Buat di `entity/core/` (untuk runtime tables) dan `entity/statistic/` (untuk aggregation tables):

- [ ] `MailArchiveNotif` (queue notif) — `mail_archive_notif`
- [ ] `MailArchiveNotifLog` (audit notif) — `mail_archive_notif_log`
- [ ] `MailResponseTime` — `mail_respontime`
- [ ] `MailOrgStatistic` — `mail_org_statistic`
- [ ] `MailCategoryStatistic` — `mail_category_statistic`
- [ ] `MessageTemplate` — `msg_template`

Lihat section 4 plan-25 (akan dibuat terpisah) untuk detail field & FK.

### 6.3 Repository, Service, DTO Update

Untuk setiap entity yang berubah field-nya:
- Update JPA repository jika ada method by-field yang berubah
- Update JOOQ query (kolom rename = query SQL berubah!)
- Update MapStruct mapper
- Update DTO + endpoint kalau field exposed di API
- Update test fixture

**WARNING:** rename `@Column` di `MailArchive` akan **memengaruhi semua JOOQ query** yang select kolomnya. Audit `repository/archive/jooq/` sebelum apply.

---

## 7. Beads Issue Breakdown

Setiap migration = 1 beads issue. Dependency chain:

```
beads-FK-MASTER (V28) — independent
beads-FK-FOLDER (V27) — independent
beads-FK-PRINT (V29) — independent
beads-FK-PUB (V24)  — independent
beads-FK-DOCTYPE (V25) — depends → V24
beads-FK-QM (V26)   — independent
beads-FK-ARCHIVE-ACCESS (V21) — independent (after preflight)
beads-FK-ARCHIVE (V20) — independent
beads-FK-ATTACH (V23) — depends → preflight duplikat (Q5.3)
beads-FK-RECIPIENT (V18, V19) — depends → V15..V17
beads-FK-USERTASK (V22) — depends → V17
beads-FK-MAIL (V15, V16, V17) — chain dependent: V15 → V16 → V17
beads-NEW-ENTITY-NOTIF — independent
beads-NEW-ENTITY-NOTIF-LOG — depends → mail_archive entity update
beads-NEW-ENTITY-RESPONTIME — depends → V17 (mail FK)
beads-NEW-ENTITY-ORG-STAT — independent
beads-NEW-ENTITY-CAT-STAT — depends → V28
beads-NEW-ENTITY-MSGTPL — independent
```

**Format issue title:** `[migrate] V{NN} {description}`
**Description harus berisi:**
- Tabel target
- Pre-flight check yang harus lewat
- SQL DDL diff
- Entity diff
- Repository/JOOQ files yang harus di-update
- Acceptance criteria (FK exists, orphan = 0, app boot OK, regression test pass)

---

## 8. Risk Register & Rollback Strategy

| Risk | Probabilitas | Impact | Mitigation | Rollback |
|---|---|---|---|---|
| FK constraint fail karena orphan tertinggal | MEDIUM | HIGH | Pre-flight check + batch cleanup | `ALTER TABLE DROP FOREIGN KEY` |
| Cleanup batch hapus row yang seharusnya valid | LOW | HIGH | Backup `mail_recipient` ke `mail_recipient_backup_YYYYMMDD` sebelum DELETE | INSERT INTO ... SELECT FROM backup |
| Composite PK `attachments` bikin duplicate `id` exist | UNKNOWN | HIGH | Verifikasi Q5.3 dulu, jika ada → batalkan V23, pakai @IdClass | n/a |
| Lock table panjang saat ALTER (mail 1.8M rows) | HIGH | MEDIUM | Pakai `ALGORITHM=INPLACE, LOCK=NONE` jika MariaDB support; jalankan di maintenance window | `ALTER TABLE` reverse |
| Rename `@Column` di MailArchive break JOOQ query | HIGH | HIGH | Audit semua JOOQ file, run integration test full | revert entity rename |
| ENUM migration `area_publik.status` data corruption | LOW | HIGH | Backup table, dual-value transient state | restore backup |
| MyISAM→InnoDB conversion gagal di pesan_singkat | LOW | LOW | 29 row only, manual verify | recreate from backup |
| ETL legacy → DB baru: data lama dengan nilai sentinel (-1, 0) | HIGH | MEDIUM | Mapping rules per kolom didokumentasikan di section 9 | n/a — design issue |
| FK `ON DELETE CASCADE` accidental mass-delete | LOW | CRITICAL | Review semua CASCADE, prefer SET NULL kecuali audit trail | restore backup |

### 8.1 Backup Strategy

**Sebelum setiap migration berisiko (V16, V18, V19, V21, V23, V24, V25):**
```sql
CREATE TABLE {tablename}_backup_20260504 AS SELECT * FROM {tablename};
```
Drop backup tables setelah 1 sprint (verifikasi production stable).

### 8.2 Rollback Master Plan
- Setiap V{NN} migration **wajib punya** file `U{NN}__undo.sql` di `db/undo/` (Flyway undo support).
- Migration yang ber-data-loss (DELETE/UPDATE) **tidak punya undo perfect** — andalkan backup table.

---

## 9. Appendix

### 9.1 Schema Legacy Lengkap
Lihat `plan/24-appendix-legacy-schema-dump.sql` (akan di-generate terpisah dari `mysqldump --no-data smartoffice`).

### 9.2 Mapping Sentinel Values

| Tabel | Kolom | Sentinel | Arti | Mapping Aplikasi Baru |
|---|---|---|---|---|
| `sys_user_task` | `folder_id` | -1 | PURGED (deleted permanen) | `SystemFolder.PURGED` enum, NO FK |
| `mail_folder` | `parent_folder_id` | 0 | Root (no parent) | NULL via V27 migration |
| `mail_folder` | `owner_id` | 0 | System folder (1-6, 10) | tetap 0, dokumentasi enum |
| `mail` | `m_status` | 0 | DRAFT | `MailStatus.DRAFT` |
| `mail` | `m_status` | 1 | SENT | `MailStatus.SENT` |
| `mail_archive` | `ma_status` | 1 | DRAFT | `ArchiveStatus.DRAFT` |
| `mail_archive` | `ma_status` | 2 | ARCHIVED | `ArchiveStatus.ARCHIVED` |
| `mail_archive` | `ma_status` | 3 | DELETED | `ArchiveStatus.DELETED` |
| `mail_archive_access` | `access`/`download`/`history` | ' ' (space) | NO permission | `false` |
| `mail_archive_access` | `access`/`download`/`history` | 'Y' | YES permission | `true` |

### 9.3 6 Entity Baru — Field Reference

#### `MailArchiveNotif` (`mail_archive_notif`)
```
id INT PK
mail_archive_id INT → FK mail_archive.ma_id
notif_flag INT
insert_date DATETIME
processed_date DATETIME
```

#### `MailArchiveNotifLog` (`mail_archive_notif_log`)
```
id INT PK
mail_archive_id INT → FK mail_archive.ma_id
user_id INT
notif_date DATETIME
```

#### `MailResponseTime` (`mail_respontime`)
```
id INT PK
orig_m_id INT → FK mail.m_id ON DELETE SET NULL
orig_date DATETIME
reply_m_id INT → FK mail.m_id ON DELETE SET NULL
reply_date DATETIME
m_type INT → FK mail_type.mail_type_id
m_category INT → FK mail_category.mcat_id
respon_time INT (seconds)
```

#### `MailOrgStatistic` (`mail_org_statistic`)
```
id INT PK
period_month INT (YYYYMM format)
created_by_org INT
total INT
INDEX (period_month, created_by_org)
```

#### `MailCategoryStatistic` (`mail_category_statistic`)
```
id INT PK
period_month INT (YYYYMM format)
category_id INT → FK mail_category.mcat_id
total INT
INDEX (period_month, category_id)
```

#### `MessageTemplate` (`msg_template`)
```
template_id INT PK
message TEXT
description VARCHAR(128)
```

---

## 10. Open Questions (Belum di-resolve)

| ID | Pertanyaan | PIC | Status | Resolusi |
|---|---|---|---|---|
| OQ-1 | Verifikasi `attachments.id` ada duplikat? (Q5.3) | DBA | **RESOLVED** | Lihat `plan/24-oq-resolution.md`. Asumsi 0 duplikat; pre-flight check wajib di V23. |
| OQ-2 | Apakah `mail_archive.ma_ref_id` selalu = `mail.m_id`? | Developer | **RESOLVED** | Ya, link ke mail. Standalone archive = NULL. Rename di V20. |
| OQ-3 | `mail_org_statistic.created_by_org` = organization_id? | Developer | **RESOLVED** | Ref ke external service unit ID. Skip FK di V34. |
| OQ-4 | `msg_template` vs `pesan_singkat` — overlap? | Product | **RESOLVED** | SEPARATE. `pesan_singkat` = snippet, `msg_template` = text templates. |
| OQ-5 | Aplikasi legacy SmartOffice masih tulis paralel? | Product/Ops | **BLOCKED** | Konfirmasi ops needed. Blocker production deploy. |


---

## 11. Next Steps

1. **Review plan ini** dengan tim.
2. **Resolve OQ-1..OQ-5**.
3. **Buat beads issues** sesuai section 7 (1 issue per V{NN}).
4. **Setup staging DB** clone dari production untuk test migration.
5. **Eksekusi V15..V30** di staging, monitor durasi & error rate.
6. **Refactor entity** + run integration test full.
7. **Schedule maintenance window** untuk production migration.

# Plan: Infrastructure Layer

> **Urutan pengerjaan: TAHAP 1 (Fondasi — kerjakan pertama)**
> Layer ini harus selesai sebelum layer lain bisa diimplementasi.

---

## 1. Enum & Konstanta

Semua konstanta dari CI2 (`FOLDER_INBOX`, `MAIL_STATUS_DRAFT`, dll) wajib dipindah ke Enum Java agar type-safe.

### `MailFolder` Enum

Merepresentasikan folder sistem bawaan. Folder personal milik user disimpan di tabel `mail_folder` dan diidentifikasi dengan `folderId > FOLDER_PERSONAL`.

| Konstanta CI2    | Enum Java              | Deskripsi    |
|------------------|------------------------|--------------|
| `FOLDER_INBOX`   | `SystemFolder.INBOX`   | Kotak Masuk  |
| `FOLDER_SENT`    | `SystemFolder.SENT`    | Terkirim     |
| `FOLDER_DRAFT`   | `SystemFolder.DRAFT`   | Konsep       |
| `FOLDER_READ`    | `SystemFolder.READ`    | Sudah Dibaca |
| `FOLDER_DELETED` | `SystemFolder.DELETED` | Sampah       |

### `MailStatus` Enum

| Konstanta CI2       | Enum Java          |
|---------------------|--------------------|
| `MAIL_STATUS_DRAFT` | `MailStatus.DRAFT` |
| `MAIL_STATUS_SENT`  | `MailStatus.SENT`  |

### `ReadStatus` Enum

| Konstanta CI2 | Enum Java           |
|---------------|---------------------|
| `MAIL_UNREAD` | `ReadStatus.UNREAD` |
| `MAIL_READ`   | `ReadStatus.READ`   |

### `RefType` Enum

| Konstanta CI2           | Enum Java              |
|-------------------------|------------------------|
| `REF_TYPE_MAIL`         | `RefType.MAIL`         |
| `REF_TYPE_MAIL_ARCHIVE` | `RefType.MAIL_ARCHIVE` |

> [!TIP]
> Simpan value integer dari konstanta CI2 asli sebagai field Enum (`int value`) agar kompatibel dengan data lama di database.

---

## 2. Entity

### `Mail` Entity (tabel: `mail`)

Field utama yang dipetakan dari skema lama:

| Kolom DB            | Field Java      | Tipe                | Catatan                                 |
|---------------------|-----------------|---------------------|-----------------------------------------|
| `m_id`              | `id`            | `Long`              | PK                                      |
| `m_no`              | `number`        | `String`            | Nomor surat                             |
| `m_date`            | `date`          | `LocalDate`         | Tanggal surat                           |
| `m_type`            | `type`          | `Integer`           | FK ke `mail_type`                       |
| `m_category`        | `category`      | `Integer`           | FK ke `mail_category`                   |
| `m_subject`         | `subject`       | `String`            | Subjek                                  |
| `m_content`         | `content`       | `String`            | Konten HTML                             |
| `m_note`            | `note`          | `String`            | Catatan                                 |
| `m_status`          | `status`        | `MailStatus` (Enum) | Status surat                            |
| `m_root_id`         | `rootId`        | `Long`              | Root thread                             |
| `m_parent_id`       | `parentId`      | `Long`              | Parent langsung                         |
| `m_attachment_qty`  | `attachmentQty` | `Integer`           | Jumlah lampiran                         |
| `m_to_str`          | `toStr`         | `String`            | String tujuan (denormalized)            |
| `m_ma_id`           | `archiveId`     | `Long`              | FK ke arsip, null jika belum diarsipkan |
| `m_created_by`      | `createdBy`     | `Long`              | Audit: dibuat oleh                      |
| `m_created_by_name` | `createdByName` | `String`            | Audit: nama pembuat                     |
| `m_created_date`    | `createdAt`     | `LocalDateTime`     | Audit: waktu buat                       |

> [!IMPORTANT]
> Gunakan `@CreatedDate`, `@CreatedBy`, `@LastModifiedDate`, `@LastModifiedBy` dari Spring Data Auditing. **Jangan set field ini secara manual di service.** Ini memperbaiki bug di kode lama yang meng-overwrite `m_created_date` saat update.

### `MailFolder` Entity (tabel: `mail_folder`)

| Kolom DB           | Field Java       | Catatan                                           |
|--------------------|------------------|---------------------------------------------------|
| `folder_id`        | `id`             | PK                                                |
| `folder_name`      | `name`           |                                                   |
| `parent_folder_id` | `parentFolderId` | Self-referential                                  |
| `owner_id`         | `ownerId`        | 0 = folder sistem, >0 = milik user                |
| `folder_icon_cls`  | `iconCls`        | Untuk UI (bisa dihapus jika frontend tidak pakai) |
| `folder_status`    | `status`         | Active / Deleted                                  |

### `MailRecipient` Entity (tabel: `mail_recipient`)

| Kolom DB      | Field Java    | Catatan                           |
|---------------|---------------|-----------------------------------|
| `id`          | `id`          | PK                                |
| `mail_id`     | `mailId`      | FK ke `mail`                      |
| `user_id`     | `userId`      | FK ke `sys_user`                  |
| `emp_id`      | `empId`       | FK ke `employee`                  |
| `emp_name`    | `empName`     | Denormalized                      |
| `pos_id`      | `posId`       | FK ke `position`                  |
| `pos_name`    | `posName`     | Denormalized                      |
| `circulation` | `circulation` | Jenis sirkulasi (4=tembusan, dll) |

### `UserTask` Entity (tabel: `sys_user_task`)

Ini adalah "inbox per user". Setiap surat yang masuk/keluar menghasilkan satu baris per user.

| Kolom DB            | Field Java        | Catatan                      |
|---------------------|-------------------|------------------------------|
| `user_task_id`      | `id`              | PK                           |
| `user_id`           | `userId`          | Pemilik                      |
| `tm_id`             | `mailId`          | FK ke `mail`                 |
| `folder_id`         | `folderId`        | Folder saat ini              |
| `read_status`       | `readStatus`      | ReadStatus Enum              |
| `read_date`         | `readDate`        | Waktu dibaca                 |
| `restore_folder_id` | `restoreFolderId` | Folder asal sebelum ke Trash |
| `mail_created_date` | `mailCreatedDate` | Timestamp masuk ke folder    |

### `PrintLog` Entity (tabel: `print_log`)

| Kolom DB     | Field Java  | Catatan              |
|--------------|-------------|----------------------|
| `auth_code`  | `authCode`  | UUID (harus diindex) |
| `date`       | `printedAt` | Waktu cetak          |
| `mail_id`    | `mailId`    | FK ke `mail`         |
| `username`   | `username`  | Nama pencetak        |
| `ip_address` | `ipAddress` | IP pencetak          |

---

## 3. Konfigurasi (`@ConfigurationProperties`)

### `MailClientConfig`

Menggantikan konstanta `CLIENT_CODE` hardcoded di kode lama.

```yaml
# application.yml
mail:
  client:
    code: BMS           # atau SMD, BPN
    inbox-sort-direction: ASC   # khusus BMS/SMD
    default-folder-inbox: true
```

> [!TIP]
> Gunakan Spring Profile (`spring.profiles.active=bms`) agar konfigurasi yang berbeda per client tidak bercampur dalam satu file YML.

---

## 4. Audit Configuration

Setup satu kali di kelas `@Configuration` utama:

- Aktifkan `@EnableJpaAuditing`
- Buat `AuditorAware<Long>` bean yang membaca `userId` dari `SecurityContextHolder`
- Semua Entity yang membutuhkan audit cukup annotasi dengan `@EntityListeners(AuditingEntityListener.class)`

---

## 5. Package Structure

```
com.yourapp.mail
├── config/
│   ├── MailClientConfig.java        ← @ConfigurationProperties
│   └── JpaAuditingConfig.java       ← @EnableJpaAuditing
├── enums/
│   ├── MailStatus.java
│   ├── SystemFolder.java
│   ├── ReadStatus.java
│   └── RefType.java
└── entity/
    ├── Mail.java
    ├── MailFolder.java
    ├── MailRecipient.java
    ├── UserTask.java
    └── PrintLog.java
```

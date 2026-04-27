# Plan Index: Migrasi Modul Mail → Spring Boot

> Dokumen indeks ini adalah **titik awal** yang harus dibaca sebelum mengerjakan file plan lainnya.
> Ikuti urutan pengerjaan di bawah agar dependensi antar layer tidak terbalik.

---

## Struktur Dokumen Plan

| File                           | Layer                   | Isi                                              |
|--------------------------------|-------------------------|--------------------------------------------------|
| `00-index.md`                  | —                       | Dokumen ini. Urutan pengerjaan & overview.       |
| `01-infrastructure.md`         | Infrastructure          | Entity, Enum, Konstanta, Config, Audit           |
| `02-master.md`                 | Master Data             | MailType, MailCategory, MailFolder, PesanSingkat |
| `03-core-mail.md`              | Core: Surat             | CRUD surat, kirim, threading                     |
| `04-core-recipient.md`         | Core: Penerima          | Tambah/hapus/salin penerima                      |
| `05-core-inbox.md`             | Core: Inbox & Manajemen | Baca folder, flag, pindah, trash, counter        |
| `06-core-archive-signature.md` | Core: Arsip & TTD       | Pengarsipan & verifikasi tanda tangan cetak      |
| `V10-mail-search-index.md`   | Database / Query        | Index FULLTEXT pada tabel `mail` (Subject/Content) |

---

## Urutan Pengerjaan (Dependency Order)

```
TAHAP 1 — Infrastructure (Fondasi)
└── 01-infrastructure.md
    Entity, Enum, Config harus ada sebelum apapun bisa dibangun.

TAHAP 2 — Master Data (Data Referensi)
└── 02-master.md
    Data master harus ada sebelum core karena surat bergantung
    pada MailType dan MailCategory.

TAHAP 3A — Core: Surat & Penerima (Bersamaan)
├── 03-core-mail.md       ← bisa parallel dengan 04
└── 04-core-recipient.md  ← bisa parallel dengan 03

TAHAP 3B — Core: Inbox & Manajemen
└── 05-core-inbox.md
    Bergantung pada surat (03) dan penerima (04) sudah ada.

TAHAP 4 — Core: Arsip & Tanda Tangan
└── 06-core-archive-signature.md
    Bergantung pada semua core sebelumnya.
```

**Urutan angka:**

```
01 → 02 → 03 & 04 (parallel) → 05 → 06
```

---

## Gambaran Umum Sistem

### Arsitektur Lama (CI2 + Ext.Direct RPC)

```
Frontend (ExtJS)
     │  HTTP POST (JSON / Form)
     ▼
Direct::router()         ← single entry point
     │  dispatch RPC
     ▼
Mail (direct/mail.php)   ← controller RPC
     │
     ▼
MailModel                ← query langsung MySQL, logika bisnis campur
     │
     ▼
MySQL
```

### Arsitektur Baru (Spring Boot REST)

```
Frontend (React / Next.js / Mobile)
     │  HTTP REST (GET / POST / PUT / PATCH / DELETE)
     ▼
@RestController          ← parse request, validasi input format
     │
     ▼
@Service                 ← semua logika bisnis, orkestrasi
     │
     ▼
@Repository (JPA)        ← akses data, query
     │
     ▼
MySQL / PostgreSQL
```

### Prinsip Utama

| Prinsip                     | Penjelasan                                                        |
|-----------------------------|-------------------------------------------------------------------|
| **Controller tipis**        | Hanya parse request DTO → panggil service → return response DTO   |
| **Service tebal**           | Semua logika bisnis, validasi domain, orkestrasi antar-repository |
| **Repository murni**        | Hanya query data, tidak ada logika bisnis                         |
| **DTO layer wajib**         | Jangan expose Entity langsung ke API response                     |
| **Audit otomatis**          | Gunakan Spring Data Auditing, bukan set manual                    |
| **Enum bukan magic number** | Semua konstanta folder/status dipindah ke Enum                    |

---

## Alur Kerja Aplikasi (End-to-End)

### Alur 1: Membuat & Mengirim Surat

```
User → [Buka Form Surat]
  → POST /api/mail/messages          (simpan sebagai DRAFT)
      └─ Service: insert mail (status=DRAFT)
         update recipient ref_id (dari temp_id)
         update attachment ref_id
         hitung m_attachment_qty
         set m_root_id = m_id (surat baru)
         insert sys_user_task (folder=DRAFT, read=true)
         bangun m_to_str dari penerima
  → [Tambah Penerima]
  → POST /api/mail/messages/{id}/recipients
      └─ Service: insert ke mail_recipient
  → [Upload Lampiran]
  → POST /api/attachments (handled by attachment controller)
  → [Klik Kirim]
  → POST /api/mail/messages/{id}/send
      └─ Service: update status → SENT
         update m_no (generate nomor surat)
         pindah sender sys_user_task → FOLDER_SENT
         foreach penerima:
           insert sys_user_task (folder=INBOX, read=UNREAD)
```

### Alur 2: Membaca Surat di Inbox

```
User → [Buka Inbox]
  → GET /api/mail/folders/{FOLDER_INBOX}/messages?page=&size=&keyword=
      └─ Repository: JOIN mail + sys_user_task + mail_recipient + sirkulasi
         apply date filter (unread selalu muncul)
         apply keyword filter
         return Page<MailSummaryDto>
  → [Klik Surat]
  → PATCH /api/mail/messages/{id}/read
      └─ Service: update sys_user_task (read_status=UNREAD→READ)
         pindah folder → FOLDER_READ
```

### Alur 3: Threading (Balasan Surat)

```
User → [Klik Balas]
  → POST /api/mail/messages          (surat baru, m_parent_id = id surat dibalas)
      └─ Service: set m_parent_id = parent.m_id
         set m_root_id = parent.m_root_id
  → GET /api/mail/folders/{id}/messages?threaded=true
      └─ Service (MailThreadingService):
         bangun Map<Long, node> dari flat list
         iterasi: taruh setiap node di bawah parent-nya
         return tree structure
```

### Alur 4: Hapus & Restore Surat

```
User → [Hapus Surat]
  → DELETE /api/mail/messages/{id}?restoreFolderId={current}
      └─ Service: catat restore_folder_id di sys_user_task
         pindah ke FOLDER_DELETED
User → [Restore dari Trash]
  → PATCH /api/mail/messages/{id}/restore
      └─ Service: ambil restore_folder_id
         pindah kembali ke folder asal
User → [Kosongkan Trash]
  → DELETE /api/mail/trash
      └─ Service: hard delete sys_user_task FOLDER_DELETED milik user
```

### Alur 5: Pengarsipan Surat

```
User → [Arsipkan Surat]
  → POST /api/mail/messages/{id}/archive/initiate
      └─ Service: cek hak akses (Spring Security)
         cek apakah sudah diarsipkan (m_ma_id != null)
         cek apakah ada lampiran
         salin lampiran ke temp_id
         return {tempId, mailRoot data}
  → [Isi Form Arsip di Frontend]
  → POST /api/archive/...   (handled by archive module)
```

# Plan: Core — Pengarsipan & Tanda Tangan Cetak

> **Urutan pengerjaan: TAHAP 4 (terakhir, setelah semua core sebelumnya selesai)**
> Bergantung pada Mail, Recipient, dan Inbox sudah berjalan.

---

## ⚡ Wajib: Gunakan Context7

> [!IMPORTANT]
> Sebelum menulis kode di layer ini, query Context7 dengan topik:
> - `"Spring Security @PreAuthorize hasAuthority method security"`
> - `"Spring Boot Thymeleaf render HTML view controller"`
> - `"Java SecureRandom UUID token generation"`
> - `"Spring Boot @Transactional rollback exception"`
> - `"Spring Data JPA @Lock pessimistic locking"`

---

## Deskripsi

Layer ini mengurus dua fitur terpisah:
1. **Pengarsipan** (`directArsip`) — memulai alur pengarsipan surat ke sistem arsip fisik/digital
2. **Tanda Tangan & Verifikasi Cetak** (`signMe`, `checkSign`) — generate kode verifikasi saat cetak dan validasi keaslian print

---

## BAGIAN A: Pengarsipan Surat

### Konsep
Pengarsipan adalah proses memasukkan surat ke dalam sistem arsip resmi. Tidak semua user bisa mengarsipkan — hanya yang memiliki hak akses ke menu arsip.

Syarat surat bisa diarsipkan:
- User punya hak akses (role-based)
- Surat **belum pernah** diarsipkan (`m_ma_id` null)
- Surat **harus** memiliki lampiran (aturan bisnis sistem ini)

### Alur Kerja: Inisiasi Pengarsipan

```
POST /api/mail/messages/{id}/archive/initiate

MailArchiveService.initiateArchive(mailId, userId):

  STEP 1: Cek hak akses
    → Bisa dilakukan via @PreAuthorize("hasAuthority('MAIL_ARCHIVE')")
    → Jika tidak punya hak → 403 Forbidden (ditangani Spring Security)

  STEP 2: Ambil data surat
    → Cari mail WHERE m_id = mailId
    → Ambil root surat: WHERE m_id = mail.rootId

  STEP 3: Validasi belum diarsipkan
    → Jika mailRoot.archiveId != null:
        → Query mail_archive WHERE ma_id = archiveId
        → Throw BusinessException("Sudah diarsipkan oleh {name} nomor {no}")
        → HTTP 409 Conflict

  STEP 4: Validasi ada lampiran
    → Jika mail.attachmentQty == 0:
        → Throw BusinessException("Tidak ada lampiran")
        → HTTP 422 Unprocessable Entity

  STEP 5: Siapkan temp ID & salin lampiran
    → Generate tempId (UUID)
    → AttachmentService.copyAttachments(tempId, mailId, REF_TYPE_MAIL, REF_TYPE_MAIL_ARCHIVE)

  STEP 6: Return data ke frontend
    → Response: ArchiveInitiateResponse {
        tempId,
        mailRoot { subject, content (tanpa HTML tag), date, ... }
      }
    → Frontend melanjutkan dengan membuka form arsip
```

> [!NOTE]
> Di kode lama (`directArsip`), ada banyak kode INSERT arsip yang di-comment. Kode tersebut adalah **logika lama yang sudah tidak dipakai** dan sudah digantikan flow via frontend. **Jangan migrasikan kode yang di-comment tersebut.**

### Optimasi
- Gunakan `@PreAuthorize` dari Spring Security untuk cek hak akses — tidak perlu query manual ke `sys_role_menu_event` di dalam service.
- Definisikan hak akses sebagai Spring Security Authority: `ROLE_MAIL_ARCHIVE` atau permission string `MAIL_ARCHIVE`.

---

## BAGIAN B: Tanda Tangan & Verifikasi Cetak

### Konsep
Sebelum mencetak surat, sistem merekam log print dan membuat kode unik. Kode ini bisa di-scan/diinput untuk memverifikasi bahwa surat yang dicetak adalah sah dari sistem.

---

### Alur Kerja: Generate Kode Tanda Tangan

```
POST /api/mail/messages/{id}/sign

MailSignatureService.sign(mailId, userId, username, ipAddress):

  1. Generate token yang aman:
     → UUID.randomUUID().toString()
     → atau SecureRandom + Base64 untuk token yang lebih pendek

  2. Insert ke print_log:
     { auth_code=token, mail_id=mailId, username=username,
       ip_address=ipAddress, printed_at=now() }

  3. Return: SignResponse { code: token }
```

> [!IMPORTANT]
> **Gunakan `UUID.randomUUID()` bukan timestamp-based ID.**
> Kode lama memakai `uniqid()` PHP yang berbasis timestamp mikro dan mudah ditebak.
> Di Java, `UUID.randomUUID()` adalah kriptografis aman (menggunakan `SecureRandom` secara internal).

> [!TIP]
> Tambahkan index pada kolom `auth_code` di tabel `print_log` karena akan sering di-query saat verifikasi.

---

### Alur Kerja: Verifikasi Tanda Tangan

```
GET /print/verify?key={authCode}

PrintVerifyController.verify(authCode):

  1. Query print_log JOIN mail:
     WHERE print_log.auth_code = authCode
     SELECT print_log.*, mail.m_subject, mail.m_content, mail.m_date, ...

  2. Jika tidak ditemukan → render halaman "Kode tidak valid"

  3. Jika ditemukan → render Thymeleaf template "cek_signature.html"
     dengan data: { printInfo, mailData, authCode }
```

> [!WARNING]
> **Endpoint ini BUKAN REST JSON — ini adalah view renderer.**
> Gunakan `@Controller` (bukan `@RestController`) dan kembalikan nama template Thymeleaf.
> Pisahkan dari controller lain yang menggunakan `@RestController`.

---

## Endpoints

| HTTP | URL | Tipe | Deskripsi |
|---|---|---|---|
| POST | `/api/mail/messages/{id}/archive/initiate` | REST JSON | Inisiasi pengarsipan |
| POST | `/api/mail/messages/{id}/sign` | REST JSON | Generate kode cetak |
| GET | `/print/verify` | HTML View | Verifikasi keaslian print |

---

## Error & Exception Handling

| Kondisi | Exception | HTTP Status |
|---|---|---|
| Tidak punya hak arsip | `AccessDeniedException` (Spring Security) | 403 Forbidden |
| Surat sudah diarsipkan | `AlreadyArchivedException` | 409 Conflict |
| Tidak ada lampiran | `ArchiveValidationException` | 422 Unprocessable Entity |
| Kode sign tidak ditemukan | — | Render halaman "invalid" |

Semua exception ditangani oleh `@ControllerAdvice` global yang memberi response JSON terstruktur:
```
{
  "status": 409,
  "code": "ALREADY_ARCHIVED",
  "message": "Surat sudah diarsipkan oleh...",
  "details": { ... }
}
```

---

## Package Structure

```
com.yourapp.mail
├── controller/
│   ├── MailArchiveController.java       ← @RestController
│   ├── MailSignatureController.java     ← @RestController (sign endpoint)
│   └── PrintVerifyController.java       ← @Controller (render HTML)
├── service/
│   ├── MailArchiveService.java
│   └── MailSignatureService.java
├── repository/
│   └── PrintLogRepository.java
├── exception/
│   ├── AlreadyArchivedException.java
│   └── ArchiveValidationException.java
└── dto/
    ├── ArchiveInitiateResponse.java
    └── SignResponse.java
```

---

## Catatan Akhir: Kode yang Di-comment di Kode Lama

Di bagian bawah file `mail.php` (baris 425–447), terdapat blok komentar besar yang berisi logika lama pengarsipan langsung. Kode tersebut melakukan INSERT langsung ke `mail_archive` dan update `m_ma_id` yang sudah digantikan oleh flow baru via frontend.

> [!CAUTION]
> **Jangan migrasikan kode yang ada di dalam blok komentar tersebut.**
> Kode itu sudah tidak relevan dan akan menyebabkan konflik dengan alur arsip yang seharusnya.

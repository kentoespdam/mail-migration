# Plan: Fix MailMapper — Unmapped Target Properties

**Versi:** 1.0  
**Tanggal:** 2026-04-28  
**Terkait:** `MailMapper.java:38` — warning `Unmapped target properties: "type, category, thread, summary, audit"`

---

## 1. Ringkasan Masalah

MapStruct menghasilkan warning berikut saat kompilasi:

```
MailMapper.java:38: warning: Unmapped target properties:
  "type, category, thread, summary, audit"
```

Warning ini muncul pada **method kedua** di `MailMapper`:

```java
// method ini TIDAK memiliki @Mapping apapun
public abstract MailResponse toResponse(Mail entity);   // <-- baris 38
```

Sedangkan **method pertama** (`toResponse(Mail entity, List<Attachment> attachments)`) sudah lengkap dengan 6 `@Mapping` eksplisit yang menangani semua field kompleks (`id`, `type`, `category`, `thread`, `audit`, `summary`, `attachments`).

### Mengapa ini terjadi?

`MailResponse` memiliki field-field berikut yang **tidak bisa dipetakan secara otomatis** oleh MapStruct karena namanya berbeda dengan field di entity `Mail`:

| Field di `MailResponse` | Sumber di `Mail` entity | Keterangan |
|---|---|---|
| `type` (`MailTypeLookup`) | `mailType` (`MailType`) | Perlu konversi + encode Sqid |
| `category` (`MailCategoryLookup`) | `mailCategory` (`MailCategory`) | Perlu konversi + encode Sqid |
| `thread` (`MailThreadInfoDto`) | `rootMail`, `parentMail` | Perlu komposisi dari 2 field |
| `summary` (`MailSummaryInfoDto`) | `attachmentQty`, `toStr` | Perlu komposisi dari 2 field |
| `audit` (`MailAuditInfoDto`) | `createdBy`, `createdByName`, `createdDate`, `updatedDate` | Perlu komposisi + encode Sqid |

Karena method `toResponse(Mail entity)` tidak menyertakan anotasi `@Mapping`, MapStruct mencoba memetakan secara otomatis dan **gagal** untuk kelima field di atas — sehingga field tersebut akan bernilai `null` di output, dan MapStruct hanya bisa memberikan peringatan (bukan error).

---

## 2. Dampak

| Aspek | Dampak |
|---|---|
| **Fungsionalitas** | Method `toResponse(Mail entity)` menghasilkan `MailResponse` dengan 5 field selalu `null`: `type`, `category`, `thread`, `summary`, `audit` |
| **Data integrity** | Konsumen API yang memanggil endpoint yang menggunakan method ini akan menerima data tidak lengkap |
| **Kualitas kode** | Warning kompilasi yang terus muncul dan berpotensi menutupi warning lain yang lebih kritis |

---

## 3. Temuan Investigasi

Hasil penelusuran pemanggil `mailMapper.toResponse(Mail entity)` menunjukkan bahwa method ini dipanggil **eksklusif dari `MailCommandService`** di 3 skenario operasi CRUD:

| Lokasi Pemanggil | Operasi | Konteks |
|---|---|---|
| `MailCommandService.createDraft()` baris 91 | Buat draft baru | Response setelah surat disimpan sebagai draft |
| `MailCommandService.updateDraft()` baris 135 | Update draft | Response setelah data draft diperbarui |
| `MailCommandService.send()` baris 141 | Kirim surat | Response setelah surat dikirim |

Ketiga skenario ini adalah **operasi command yang mengembalikan `MailResponse` lengkap ke client** — bukan untuk listing/preview. Artinya, client yang melakukan `createDraft`, `updateDraft`, dan `send` **mengharapkan data `type`, `category`, `thread`, `summary`, `audit` terisi** di response-nya, namun saat ini kelima field tersebut bernilai `null`.

Sebagai perbandingan, pemanggil overload dengan attachments (`toResponse(Mail entity, List<Attachment>)`) hanya ada di `MailQueryService.java` baris 34 — yang secara eksplisit menyediakan list attachments dari query terpisah.

**Temuan kunci:** Overload kedua (`toResponse(Mail entity)`) **tidak pernah dirancang untuk use case yang berbeda** — ia hanya kekurangan anotasi `@Mapping`, sehingga 5 field kompleks yang seharusnya terisi justru dibiarkan `null`.

---

## 4. Rencana Perbaikan

### Opsi A — Tambahkan @Mapping pada method `toResponse(Mail entity)` *(Direkomendasikan)*

Tambahkan anotasi `@Mapping` yang sama seperti method pertama ke method `toResponse(Mail entity)`, **kecuali** untuk `attachments` yang memang tidak tersedia karena tidak ada parameter `List<Attachment>`. Field `attachments` perlu di-ignore secara eksplisit.

**Kapan dipilih:** Jika overload kedua ini diharapkan menghasilkan `MailResponse` yang memiliki data `type`, `category`, `thread`, `summary`, `audit` lengkap — hanya saja tanpa data attachments.

---

### Opsi B — Gunakan `@BeanMapping(ignoreByDefault = true)` dan petakan hanya field yang diperlukan

Jika overload kedua memang hanya untuk keperluan ringkas/preview dan secara sengaja hanya butuh sebagian field, maka explisit-ignorekan semua field yang tidak dipetakan agar warning hilang tanpa mengubah perilaku.

**Kapan dipilih:** Jika ada konfirmasi bahwa method ini memang hanya boleh mengembalikan sebagian field.

---

### Opsi C — Hapus method `toResponse(Mail entity)` dan ganti dengan default value untuk attachments

Jika semua pemanggil sebenarnya bisa menerima `null` atau `List.of()` untuk attachments, cukup satu method saja. Method pertama bisa dipanggil dengan `null` sebagai argumen attachments.

**Kapan dipilih:** Jika investigasi menunjukkan bahwa tidak ada kebutuhan khusus untuk overload tanpa attachments.

---

## 5. Rekomendasi

**Opsi A** adalah satu-satunya pilihan yang tepat berdasarkan temuan investigasi. Berdasarkan konteks penggunaan, ketiga pemanggil di `MailCommandService` adalah operasi command yang **harus mengembalikan response lengkap** ke client (createDraft, updateDraft, send). 

Perbaikan yang diperlukan:
- Tambahkan semua anotasi `@Mapping` yang sama dengan method pertama ke method `toResponse(Mail entity)`.
- Tambahkan `@Mapping(target = "attachments", ignore = true)` secara eksplisit karena tidak ada parameter `List<Attachment>` di overload ini.
- Tidak perlu merge atau menghapus salah satu overload; keduanya tetap relevan dengan perbedaan yang jelas: overload pertama untuk **query detail** (dengan attachments), overload kedua untuk **response command** (tanpa attachments).

---

## 6. Urutan Langkah Perbaikan (High-Level)

1. **Tambahkan anotasi `@Mapping`** yang sama dengan method pertama ke method `toResponse(Mail entity)` di `MailMapper.java`, ditambah satu anotasi ignore untuk field `attachments`.
2. **Verifikasi build bersih** — pastikan warning MapStruct untuk method ini hilang setelah perubahan.
3. **Validasi fungsional** — panggil ketiga endpoint yang menggunakan overload ini (`createDraft`, `updateDraft`, `send`) dan pastikan response-nya kini memiliki data `type`, `category`, `thread`, `summary`, `audit` yang terisi benar.
4. **Update/tambah unit test** untuk `MailCommandService` yang memverifikasi bahwa `MailResponse` dari operasi-operasi tersebut tidak pernah mengandung `null` di kelima field tersebut.

---

## 7. Catatan Tambahan

> Tidak ada pertanyaan yang perlu dikonfirmasi — investigasi sudah menghasilkan jawaban yang konklusif. Perbaikan dapat langsung diimplementasikan menggunakan **Opsi A**.

# Plan: Master Data — MailCategory (Kategori Surat)

> **Urutan: TAHAP 2B — setelah MailType selesai**
> MailCategory memiliki relasi FK ke `mail_type`, jadi MailType harus ada lebih dulu.

---

## ⚡ Wajib: Gunakan Context7

> [!IMPORTANT]
> Query Context7 sebelum implementasi:
> - `"Spring Data JPA findBy optional parameter"`
> - `"Spring Boot @RequestParam Optional"`
> - `"Spring Data JPA Specification dynamic filter"`
> - `"Spring Boot @Cacheable Redis with key parameter"`
> - `"Spring Boot @CacheEvict all entries"`

---

## Fungsi

Sub-kategorisasi surat di bawah MailType. Digunakan saat membuat surat dan sebagai filter di inbox.

**Tabel DB:** `mail_category`  
**Relasi:** `mail_category.mail_type_id` → `mail_type.mail_type_id`

---

## Alur Kerja

```
GET /api/mail/categories?typeId={optional}&page=&size=

MailCategoryController → MailCategoryService:
  if typeId != null → findByMailTypeId(typeId, Pageable)
  else             → findAll(Pageable)
  → Response: Page<MailCategoryDto>
```

---

## Endpoint

| HTTP | URL | Deskripsi |
|---|---|---|
| GET | `/api/mail/categories` | Semua kategori |
| GET | `/api/mail/categories?typeId={id}` | Filter by jenis surat |

---

## Optimasi

- Data jarang berubah → wajib pakai `@Cacheable` dengan Redis.
- Perhatikan **cache key** karena ada parameter filter `typeId`:
  ```
  @Cacheable(value = "mail-categories", key = "#typeId.orElse('all')")
  ```
- Ini memastikan cache untuk `typeId=1` dan `typeId=null` tidak bentrok.
- TTL: 1 jam (sama dengan konfigurasi Redis di `02a-master-mail-type.md`).
- Saat data category berubah, evict semua entry:
  ```
  @CacheEvict(value = "mail-categories", allEntries = true)
  ```
- Gunakan `@RequestParam Optional<Long> typeId` di controller agar parameter benar-benar optional tanpa branching manual.

---

## Package

```
controller/ MailCategoryController.java
service/    MailCategoryService.java
repository/ MailCategoryRepository.java
dto/        MailCategoryDto.java
```

---

## Catatan Migrasi

Di kode lama (`getMailCategory`), parameter `mail_type_id` dihandle secara manual dengan `isset()`. Di Spring Boot, gunakan `Optional<Long>` di method signature controller — lebih aman dan eksplisit.

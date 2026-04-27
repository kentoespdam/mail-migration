# Plan: Master Data — MailType (Jenis Surat)

> **Urutan: TAHAP 2A — kerjakan pertama di antara master data**
> MailCategory bergantung pada MailType, jadi ini harus selesai lebih dulu.

---

## ⚡ Wajib: Gunakan Context7

> [!IMPORTANT]
> Query Context7 sebelum implementasi:
> - `"Spring Boot 3 REST controller service repository pattern"`
> - `"Spring Data JPA findAll Pageable"`
> - `"Spring Boot @Cacheable Redis RedisCacheManager"`
> - `"Spring Boot spring-data-redis cache configuration"`

---

## Fungsi

Data referensi untuk tipe/jenis surat yang tersedia di sistem: Internal, Eksternal, dll.
Digunakan sebagai dropdown saat user membuat surat baru.

**Tabel DB:** `mail_type`

---

## Alur Kerja

```
GET /api/mail/types?page=&size=&sort=&filter=

MailTypeController → MailTypeService → MailTypeRepository
  → findAll(Pageable)
  → Response: Page<MailTypeDto>
```

---

## Endpoint

| HTTP | URL | Deskripsi |
|---|---|---|
| GET | `/api/mail/types` | List jenis surat (paginated) |

---

## Optimasi

- Data ini **jarang berubah** → wajib pakai `@Cacheable("mail-types")` dengan Redis.
- Konfigurasi di `application.yml`:
  ```yaml
  spring:
    data:
      redis:
        host: localhost
        port: 6379
    cache:
      type: redis
      redis:
        time-to-live: 3600000   # 1 jam (ms)
        cache-null-values: false
  ```
- Saat data master berubah (via admin), wajib evict cache:
  ```
  @CacheEvict(value = "mail-types", allEntries = true)
  ```
- Gunakan `RedisCacheManager` dengan `RedisCacheConfiguration` agar TTL bisa dikonfigurasi per cache key.

---

## Package

```
controller/ MailTypeController.java
service/    MailTypeService.java
repository/ MailTypeRepository.java
dto/        MailTypeDto.java
```

---

## Catatan Migrasi

Di kode lama (`getMailType`), model di-load *on-demand* di dalam method dengan `$this->load->model(...)`. Di Spring Boot, `MailTypeRepository` diinjeksi via **constructor injection** — tidak ada lazy load di dalam method.

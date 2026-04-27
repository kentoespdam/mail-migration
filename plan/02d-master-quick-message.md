# Plan: Master Data — PesanSingkat (Quick Messages)

> **Urutan: TAHAP 2D — bisa dikerjakan kapan saja setelah Infrastructure selesai**
> Tidak bergantung pada master data lain. Entitas paling sederhana.

---

## ⚡ Wajib: Gunakan Context7

> [!IMPORTANT]
> Query Context7 sebelum implementasi:
> - `"Spring Boot @Cacheable Redis RedisCacheManager"`
> - `"Spring Data JPA findAll order by"`
> - `"Spring Boot customize TTL per cache name Redis"`

---

## Fungsi

Template pesan singkat yang bisa dipilih user saat menulis surat agar tidak perlu mengetik ulang.

**Tabel DB:** `pesan_singkat`  
**Kolom utama:** `pesan` (teks pesan)

---

## Alur Kerja

```
GET /api/mail/quick-messages

QuickMessageService.getAll():
  → findAll(Sort.by("pesan").ascending())
  → Return List<QuickMessageDto>
```

---

## Endpoint

| HTTP | URL | Deskripsi |
|---|---|---|
| GET | `/api/mail/quick-messages` | Daftar semua pesan singkat |

---

## Optimasi

- Data ini **hampir tidak pernah berubah** → kandidat cache terbaik.
- Gunakan Redis dengan TTL **lebih panjang** dari master lain (24 jam):
  ```
  @Cacheable("quick-messages")
  ```
- Konfigurasi TTL khusus per cache name di `RedisCacheManager`:
  ```yaml
  # Contoh konfigurasi per cache key
  # quick-messages: 24 jam
  # mail-types: 1 jam
  # mail-categories: 1 jam
  ```
- Karena data nyaris tidak berubah, pertimbangkan `@CacheEvict` hanya dipanggil saat ada operasi admin CRUD pada tabel `pesan_singkat`.

---

## Package

```
controller/ QuickMessageController.java
service/    QuickMessageService.java
repository/ QuickMessageRepository.java
dto/        QuickMessageDto.java
```

---

## Catatan Migrasi

Kode lama `getPesan($input)` menerima parameter `$input` tapi tidak digunakan sama sekali. Di Spring Boot, endpoint ini **tidak perlu parameter apapun** — cukup `@GetMapping` tanpa `@RequestParam`.

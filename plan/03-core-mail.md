# Plan: Core Mail Layer — Index

> **Urutan pengerjaan: TAHAP 3A (parallel dengan `04-core-recipient.md`)**
> Buka sub-file sesuai urutan di bawah.

---

## Urutan Pengerjaan

```
03a (CRUD/Draft) → 03b (Send+Nomor) → 03c (Query) & 03d (Threading)
                                       ↑ bisa parallel setelah 03b
```

**Alasan urutan:**
- `03b` (Send) memanggil method dari `03a` (CRUD) → CRUD harus selesai dulu
- `03c` (Query) membaca data saja, tidak bergantung pada send
- `03d` (Threading) adalah post-processing dari hasil `03c` → bisa dikerjakan bersamaan

---

## Daftar Sub-File

| File | Tanggung Jawab | Prioritas |
|---|---|---|
| [03a-core-mail-crud.md](03a-core-mail-crud.md) | Buat & update draft | **Pertama** |
| [03b-core-mail-send.md](03b-core-mail-send.md) | Kirim + generate nomor surat | Setelah 03a |
| [03c-core-mail-query.md](03c-core-mail-query.md) | Baca folder, cari, tracking, laporan | Setelah 03b |
| [03d-core-mail-threading.md](03d-core-mail-threading.md) | Build tree threaded view | Setelah / parallel 03c |

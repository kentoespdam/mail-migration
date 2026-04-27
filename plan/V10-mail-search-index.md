---
title: V10 — Index Pencarian pada Tabel `mail`
status: planned
target-migration: V10__mail_search_indexes.sql
author: Senior Lead Engineer & Database Integrity Auditor
date: 2026-04-27
---

# Rencana Migrasi V10 — Index Pencarian pada Tabel `mail`

## Konteks & Temuan

Tabel `mail` (lihat `V1__baseline_schema.sql`) menyimpan korespondensi utama. Saat ini kolom-kolom yang sering dipakai untuk pencarian/filter di `MailQueryRepository` (JOOQ) **belum memiliki index yang sesuai**:

| Kolom            | Tipe           | Pola Akses Dominan                                  | Index Saat Ini |
|------------------|----------------|-----------------------------------------------------|----------------|
| `m_subject`      | `varchar(256)` | `LIKE '%kata%'` / pencarian teks bebas              | (tidak ada)    |
| `m_content`      | `text`         | `LIKE '%kata%'` / pencarian teks bebas              | (tidak ada)    |
| `m_created_by`   | `int(11)`      | Filter "surat saya" (equality)                      | B-Tree (`KEY m_created_by`) — sudah ada |

Konsekuensi: query pencarian global (subject + content) saat ini **full-table scan**, dan akan memburuk seiring bertumbuhnya volume surat.

## Tujuan

1. Menambahkan **FULLTEXT index** pada `m_subject` dan `m_content` agar pencarian teks dapat menggunakan `MATCH ... AGAINST` (jauh lebih cepat daripada `LIKE '%...%'`).
2. Memastikan **index pada `m_created_by`** sudah optimal untuk filter kepemilikan (equality + join ke HR).
3. Tetap kompatibel dengan engine **InnoDB** + collation `utf8mb4_unicode_ci` (MariaDB 11.4 mendukung FULLTEXT pada InnoDB sejak 10.0.5).

## Langkah-Langkah High Level

1. **Buat file migrasi Flyway baru**: `V10__mail_search_indexes.sql` di `src/main/resources/db/migration/`.
2. **Tambah FULLTEXT index** pada kolom `m_subject` dan `m_content` (dapat digabung sebagai *composite fulltext* untuk mendukung pencarian gabungan, atau dipisah jika tiap kolom dicari independen — keputusan dijabarkan pada Architecture Decisions).
3. **Verifikasi index `m_created_by`** sudah ada (B-Tree dari V1). Karena requirement menyebut "fulltext index" untuk `m_created_by`, audit ulang: kolom ini bertipe `int` — FULLTEXT **tidak berlaku** untuk tipe numerik di MariaDB. Yang relevan adalah B-Tree biasa, dan ini **sudah ada**. Catat keputusan ini di Architecture Decisions.
4. **Validasi performa lokal**: jalankan `EXPLAIN` pada query JOOQ pencarian (`MailQueryRepository#search...`) sebelum & sesudah migrasi untuk memastikan optimizer memilih index baru.
5. **Sesuaikan layer query** (di luar scope migrasi tapi catat sebagai follow-up): refactor predicate `LIKE '%term%'` di `MailQueryRepository` menjadi `MATCH(m_subject, m_content) AGAINST (? IN BOOLEAN MODE)` agar fulltext index benar-benar terpakai.
6. **Dokumentasi**: update `memory.md` (root) dan `00-index.md` di folder `plan/` agar V10 tercatat.
7. **Smoke test**: jalankan `./gradlew flywayMigrate` di environment dev (Docker compose MariaDB 11.4), pastikan migrasi sukses tanpa lock berkepanjangan.

## Dampak & Risiko

- **Ukuran storage**: FULLTEXT index pada `text` (m_content) akan menambah overhead disk. Estimasi: ~30–50% dari ukuran kolom teks.
- **Lock saat ALTER**: pada tabel besar, `ALTER TABLE ... ADD FULLTEXT` bisa mem-block writes. Pada produksi disarankan window maintenance, atau gunakan `ALGORITHM=INPLACE, LOCK=NONE` (didukung MariaDB 10.0.5+).
- **Minimum word length**: default `innodb_ft_min_token_size = 3`. Kata pendek (≤2 huruf) tidak akan ter-index — perlu disosialisasikan ke tim QA/produk.
- **Tidak backward-incompatible**: tidak ada kolom yang diubah, hanya penambahan index.

## Architecture Decisions

> Blok ini wajib dibaca sebelum eksekusi.

1. **`m_created_by` TIDAK menggunakan FULLTEXT.**
   Kolom bertipe `int(11)` (referensi ke ID karyawan). FULLTEXT hanya valid untuk `CHAR`, `VARCHAR`, `TEXT`. Index B-Tree yang ada (`KEY m_created_by`) sudah optimal untuk pola equality/join. Permintaan asli ditafsirkan ulang: yang dibutuhkan adalah **memastikan filter "surat saya" cepat**, bukan FULLTEXT literal.

2. **Composite FULLTEXT (`m_subject`, `m_content`) vs index terpisah.**
   - **Pilihan: composite single FULLTEXT index `ft_mail_subject_content (m_subject, m_content)`.**
   - Alasan: pola pencarian dominan adalah *global search* (user mengetik kata, dicocokkan ke subject+content sekaligus). Composite index memungkinkan satu kali `MATCH` lintas kedua kolom dan lebih hemat storage daripada dua index terpisah.
   - Trade-off: jika di masa depan dibutuhkan pencarian per-kolom dengan ranking berbeda, perlu menambah index tambahan. Saat ini belum ada use case tersebut.

3. **Refactor query layer wajib menyusul.**
   Tanpa mengubah `MailQueryRepository` dari `LIKE '%...%'` menjadi `MATCH ... AGAINST`, FULLTEXT index **tidak akan dipakai optimizer** dan migrasi ini hanya menambah beban tulis. Karena ini perubahan logika query, ia diajukan sebagai **task terpisah** (mengikuti pemisahan rencana vs implementasi pada repo ini), namun harus dirilis bersamaan agar manfaat performa terealisasi.

4. **Engine & collation tidak diubah.**
   InnoDB + `utf8mb4_unicode_ci` tetap dipakai. Tidak ada migrasi ke MyISAM (legacy) maupun penggantian collation. Fulltext parser default sudah memadai untuk Bahasa Indonesia pada level kata; kebutuhan stemming/analyzer lanjutan (mis. n-gram) di-defer hingga ada bukti kebutuhan dari user.

## Acceptance Criteria

- [ ] File `V10__mail_search_indexes.sql` ada dan lulus `flywayMigrate` di lingkungan dev.
- [ ] `SHOW INDEX FROM mail` menampilkan index FULLTEXT baru.
- [ ] `EXPLAIN` query pencarian (setelah refactor query layer) menunjukkan penggunaan FULLTEXT.
- [ ] Tidak ada regresi pada test suite (`./gradlew test`).
- [ ] `00-index.md` di folder `plan/` sudah memuat referensi ke dokumen ini.

## Follow-Up (Out of Scope V10)

- Refactor `MailQueryRepository` untuk memakai `MATCH ... AGAINST` + relevance scoring.
- Evaluasi penambahan kolom `m_search_tsv` ter-generate jika dibutuhkan boolean mode kompleks.
- Monitoring ukuran index via `information_schema.INNODB_SYS_TABLES` setelah 1–2 minggu di produksi.

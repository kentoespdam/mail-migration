# Rencana Implementasi: Audit Proyek & Ekspansi CacheConfig

Dokumen ini merinci langkah-langkah implementasi tingkat tinggi (high-level) berdasarkan hasil analisa audit proyek (`22-project-audit-and-improvement-plan.md`) dan status terkini dari migrasi konfigurasi cache (`21-cacheconfig-jackson2json-migration.md`), di mana migrasi awal `Jackson2JsonRedisSerializer` telah selesai dikerjakan dan kini tahap ekspansi.

## Prinsip Implementasi
Semua langkah implementasi di bawah ini berpedoman pada prinsip berikut:
1. **Backward Compatibility & Zero Disruption:** Perubahan tidak boleh merusak fungsionalitas yang ada atau mengganggu pengguna akhir. Jika ada perubahan struktur data (seperti cache), strategi *versioning* atau pengelolaan state transisi harus digunakan.
2. **Tidak Menghambat Fitur Baru:** Pengerjaan dilakukan secara bertahap (*incremental*) bersamaan dengan alur pengembangan sprint (menggunakan prinsip *Boy Scout Rule*). Perbaikan besar dipecah menjadi tugas-tugas kecil yang disisipkan ke dalam sprint tanpa memblokir peta jalan (roadmap) produk.

---

## Bagian I: Ekspansi Implementasi Migrasi CacheConfig Baru

Mengingat perbaikan *CacheConfig* (`JacksonJsonRedisSerializer`) pada arsitektur dasar telah berhasil dikerjakan, langkah selanjutnya adalah memperluas standarisasi cache ini ke modul/service lain di dalam aplikasi.

1. **Pemetaan Modul/Service (Fokus Awal: Modul Master):**
   - Mengidentifikasi modul-modul lain seperti modul `master` yang menggunakan caching namun mungkin belum terintegrasi dengan standar serializer cache terbaru.
2. **Penyeragaman Instansiasi dan Penggunaan Cache:**
   - Menerapkan komponen serializer yang sudah diperbarui (`JacksonJsonRedisSerializer`) pada konfigurasi cache di tingkat service spesifik (seperti pada layer `MasterService` atau repositorinya).
3. **Penerapan *Versioning* (*Backward Compatibility*):**
   - Menyertakan *suffix* versi baru (misalnya `:v3` atau spesifik sesuai versi) pada *key* cache untuk service `master` dan service lainnya. Hal ini memastikan cache lama yang tersisa tidak bertabrakan dengan format serialisasi baru, dan cache akan terganti secara otomatis (*graceful cache invalidation*).
4. **Validasi dan Pengujian Operasional:**
   - Memastikan operasi CRUD pada modul master dapat berjalan normal, menyimpan ke Redis dengan format baru, dan berhasil dibaca kembali (deserialize) tanpa error.

---

## Bagian II: Langkah Implementasi Perbaikan Hasil Audit Proyek

Implementasi ini bertujuan meningkatkan kualitas, keamanan, dan performa arsitektur *Mail Service* secara umum.

### A. Peningkatan Kualitas & Maintainability Kode
1. **Dekomposisi Logika Kompleks (Refactoring Bertahap):**
   - **Tindakan:** Mengidentifikasi service-service pada *Command* yang terlalu besar (monolitik) dan memecahnya menjadi komponen *use-case* spesifik atau *helper domain*.
   - **Eksekusi:** Dilakukan sedikit demi sedikit setiap kali modul tersebut sedang ditugaskan untuk pengembangan fitur baru atau perbaikan *bug*.
2. **Penerapan Standar Dokumentasi (Javadoc):**
   - **Tindakan:** Memasukkan aturan standar dokumentasi untuk *interface* utama, kontrak API, dan struktur DTO.
   - **Eksekusi:** Diberlakukan melalui *Pull/Merge Request*; fitur baru atau *refactoring* tidak akan disetujui tanpa memenuhi standar dokumentasi ini.

### B. Optimasi Performa & Sumber Daya
1. **Review dan Tuning Database Query:**
   - **Tindakan:** Melakukan pemantauan query kompleks, khususnya pada implementasi JOOQ, dan memastikan MariaDB menggunakan indeks yang sesuai untuk mempercepat proses pencarian (tanpa *table scan* berlebih).
   - **Eksekusi:** Berjalan di latar belakang dengan menganalisis metrik performa (*slow query logs*) tanpa mengganggu rilis fungsional.
2. **Perluasan dan Optimalisasi Caching:**
   - **Tindakan:** Menggunakan Redis yang telah termigrasi formatnya untuk menyimpan lebih banyak data referensial atau data master (semi-statis) secara komprehensif, untuk menekan latensi pembacaan data.

### C. Peningkatan Keamanan Data & Aplikasi
1. **Integrasi Manajemen Kredensial Aman:**
   - **Tindakan:** Menghilangkan semua indikasi konfigurasi sensitif (*hardcoded*) di dalam kode dan menstandarisasi pengambilan rahasia (secrets) secara dinamis via *Environment Variables* atau *Secret Manager*.
2. **Penguatan Mekanisme *Audit Trail*:**
   - **Tindakan:** Mengkonsolidasikan proses pencatatan riwayat (log) untuk transaksi kritikal seperti perubahan status dan pembuatan pengiriman pesan agar mudah ditelusuri.
   - **Eksekusi:** Menggunakan pendekatan asinkron (misalnya, *Virtual Threads* atau antrean *event*) agar penulisan log tidak menambah *response time* (latensi) bagi pengguna.

### D. Peningkatan Kolaborasi dan Penjaminan Mutu
1. **Standardisasi Proses Code Review:**
   - **Tindakan:** Memperkuat *Pull Request Checklist* yang berfokus pada ketahanan arsitektur (pola CQRS, obfuscation dengan Sqids) bukan hanya pengecekan sintaksis, sehingga menurunkan *Technical Debt*.
2. **Kolaborasi Pengembangan (Pair/Mob Programming):**
   - **Tindakan:** Menjadwalkan sesi pengkodean kolaboratif untuk pengerjaan area krusial seperti integrasi JOOQ atau ekspansi arsitektur ke modul lain demi penyebaran pemahaman yang merata.
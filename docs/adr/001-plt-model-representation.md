# ADR 001: Plt (Pelaksana Tugas) Model Representation

## Status
Proposed (Blocker for `mail-service-4z6`)

## Context
Aplikasi Mail Service perlu merepresentasikan posisi/jabatan aktif pengguna untuk menentukan *visibility* surat (inbox/disposisi) dan *signature* saat mengirim surat. 
Beberapa pengguna dapat memiliki peran **Plt (Pelaksana Tugas)**, di mana mereka menjalankan tugas di posisi lain (biasanya lebih tinggi atau sejajar) selain posisi definitif mereka.

Ada dua opsi model representasi di data HR legacy/service:
1. **Opsi 1: Direct Update `employee.emp_pos_id`**. Kolom tunggal di tabel employee di-update langsung ke ID posisi Plt. Posisi lama dikosongkan atau di-overwrite sementara.
2. **Opsi 2: `employee_assignment` Table**. Tabel baru untuk menyimpan banyak penugasan (M:N) antara pegawai dan posisi, dengan flag tipe (Definitif, Plt, Plh).

## Decision
Kami memutuskan untuk menggunakan **Opsi 1: Direct Update `employee.emp_pos_id`** (Single Active Position).

## Reasoning
1. **Penyederhanaan Logika (Convention over Configuration)**: Berdasarkan `CONTEXT.md` (§Role-in-context UX), proyek mengasumsikan "Tidak ada jabatan rangkap" (1 user = 1 posisi aktif pada saat tertentu). Plt diperlakukan sebagai pergantian posisi aktif sepenuhnya.
2. **Kesesuaian dengan HR Service API**: `EmployeeDto` yang dikonsumsi via OpenFeign saat ini hanya mendukung satu objek `jabatan` dan `organisasi`. Menggunakan model multi-assignment akan memerlukan perubahan besar pada kontrak integrasi dan caching.
3. **Data Legacy Consistency**: Hasil probe DB legacy menunjukkan `SELECT COUNT(*) FROM employee GROUP BY emp_id HAVING COUNT(*) > 1` adalah 0, dan tidak ditemukan tabel `%assignment%`. Ini mengonfirmasi bahwa sistem legacy saat ini mengandalkan pemutakhiran langsung pada kolom posisi.
4. **Zero Schema Change Policy**: Sesuai `docs/PRD-migrasi-mail-disposisi.md`, terdapat batasan ketat untuk tidak menambah kolom/tabel baru pada entitas inti jika bisa dihindari.
5. **Impact pada Role-in-Context Resolver**: Dengan model single-position, `MailRoleContextResolver` cukup melakukan cache lookup ke `hrEmployee::{empId}` untuk mendapatkan `activePosId` tanpa perlu logika pemilihan (dropdown switcher) yang kompleks di MVP.

## Consequences
- **User Experience**: Pengguna tidak perlu (dan tidak bisa) menukar role secara manual. Role aktif otomatis mengikuti data terkini di HR.
- **Audit Trail**: History "sebagai apa" saat surat dikirim tidak tersimpan di tabel `mail` (hanya `m_created_by_name`). Audit historis recipient tetap terjaga karena `mail_recipient` menggunakan snapshot `pos_name` saat surat diterima.
- **Handover**: Jika pengguna kembali ke posisi definitif, surat di Inbox Plt mereka tetap ada di UserTask mereka (tidak otomatis pindah ke pejabat baru), sehingga diperlukan handover manual jika substansi surat masih berjalan.

## References
- `CONTEXT.md` §Role-in-context UX (L1038-L1045)
- `docs/PRD-migrasi-mail-disposisi.md` Open Question #1
- `mail-service-6eb` (Beads issue)

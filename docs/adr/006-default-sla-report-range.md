# ADR 006: Default SLA Report Range

## Status
Proposed (Resolves Open Question #5 in PRD)

## Context
Aplikasi Mail Service menyediakan fitur pelaporan SLA (Response Time) untuk memantau kinerja unit dan kategori surat. 
Dengan volume data legacy mencapai 1,8 juta+ record, permintaan laporan tanpa filter tanggal (default) dapat menyebabkan beban kueri yang sangat berat pada database MariaDB dan memperlambat respon *dashboard* (User Story 24).

Kita perlu menentukan rentang waktu *default* ketika pengguna mengakses halaman laporan tanpa menentukan parameter tanggal secara eksplisit.

Beberapa opsi rentang waktu:
1. **Bulan Berjalan (Current Month)**: Menampilkan data dari tanggal 1 bulan saat ini hingga hari ini.
2. **Kuartal Berjalan (Current Quarter)**: Menampilkan data dalam 3 bulan terakhir.
3. **Tahun Berjalan (Current Year)**: Menampilkan data sejak 1 Januari tahun ini.
4. **Tanpa Filter (All Time)**: Menampilkan seluruh data historis (sangat lambat).

## Decision
Kami memutuskan untuk menggunakan **Bulan Berjalan (Current Month)** sebagai rentang waktu *default* untuk laporan SLA.

## Reasoning
1. **Performa (Fast Load)**: Kueri data dalam rentang satu bulan jauh lebih ringan dibandingkan rentang yang lebih luas, memastikan *dashboard* dimuat dengan cepat meskipun volume data total sangat besar.
2. **Relevansi Operasional**: Pengguna dan pemangku kepentingan biasanya lebih mementingkan kinerja terkini untuk pemantauan harian dan bulanan.
3. **Konvensi Pelaporan**: Siklus pelaporan rutin di organisasi (PDAM) umumnya berbasis bulanan.
4. **Fleksibilitas Pengguna**: Pengguna masih memiliki kemampuan untuk memilih rentang waktu lain (kuartal, tahun, atau kustom) melalui filter yang tersedia di UI.

## Consequences
- **Service Layer Implementation**: `MailQueryService` harus menyuntikkan rentang tanggal bulan berjalan (1 hingga akhir bulan/hari ini) jika `MailReportRequest` tidak menyertakan `startDate` dan `endDate`.
- **User Interface**: Frontend harus menampilkan label yang jelas bahwa data yang ditampilkan adalah "Bulan Berjalan" secara default.
- **Data Completeness**: Laporan yang dimuat pertama kali tidak akan menampilkan data historis lama kecuali diminta secara eksplisit, yang merupakan kompromi yang tepat antara kelengkapan dan performa.

## References
- `docs/PRD-migrasi-mail-disposisi.md` Open Question #5 & User Story 24
- `CONTEXT.md` § MailResponseTime SLA — Tata Kelola
- `mail-service-arb` (Beads issue)

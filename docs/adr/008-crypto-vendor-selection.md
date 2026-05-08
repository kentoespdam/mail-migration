# ADR 008: Crypto Vendor Selection (Post-MVP)

## Status
Decided (Post-MVP Marker)

## Context
Mail Service bertujuan menyediakan tanda tangan elektronik tersertifikasi (TTE Tersertifikasi) untuk dokumen dinas. Meskipun MVP berfokus pada *print-verification* (16-char UUID `auth_code` + QR code) tanpa kriptografi tersertifikasi, keputusan vendor CA (*Certification Authority*) diperlukan untuk menyusun roadmap pasca-MVP.

Kandidat utama adalah **BSrE (BSSN)** dan **PrivyID**.

## Comparison Table

| Aspek | BSrE (BSSN) | PrivyID |
| :--- | :--- | :--- |
| **Biaya (Cost)** | **Gratis (Rp 0)** untuk BUMD/Instansi | Berbayar (Subscription atau Per-Sign) |
| **Infrastruktur** | On-premise (*Esign Client Service*) | Cloud/SaaS (Low infrastructure) |
| **Integrasi** | REST API, proses PKS ketat | Modern REST API, *Plug and Play* |
| **Status Legal** | Standar Pemerintah, Level 4 | PSrE Tersertifikasi, UU ITE |
| **Pemeliharaan** | Tinggi (Server & perpanjangan PKS) | Rendah (Managed oleh vendor) |

## Decision
Kami memutuskan untuk memilih **BSrE (BSSN)** sebagai vendor utama untuk tanda tangan elektronik di `mail-service` pasca-MVP.

## Reasoning
1. **Efisiensi Anggaran (Zero Cost)**: Sebagai BUMD (Perumdam TS), volume surat yang mencapai ribuan per bulan akan menimbulkan biaya operasional yang signifikan jika menggunakan vendor berbayar. BSrE menyediakan layanan gratis bagi instansi pemerintah dan entitas afiliasinya.
2. **Kepatuhan SPBE**: BSrE adalah standar nasional untuk Sistem Pemerintahan Berbasis Elektronik (SPBE). Penggunaan BSrE mempermudah interoperabilitas dokumen dengan instansi pemerintah daerah/pusat lainnya.
3. **Kedaulatan Data**: Model *on-premise* yang ditawarkan BSrE memungkinkan infrastruktur penandatanganan berada di bawah kendali penuh IT Perumdam TS, sesuai dengan kebijakan keamanan data internal.
4. **Cakupan MVP**: Mengingat proses birokrasi PKS (Perjanjian Kerja Sama) dengan BSSN yang memakan waktu (3-6 bulan), fitur ini secara sadar diletakkan di luar scope MVP, dengan *print-verification* sebagai solusi sementara yang memadai.

## Consequences
- **Legal/Procurement**: Harus memulai proses permohonan ke BSSN melalui portal SIMANTAPS sesegera mungkin untuk mengejar target post-MVP.
- **Infrastruktur**: IT perlu menyiapkan VM khusus untuk instalasi *Esign Client Service* (Min. RAM 8GB, OS Linux).
- **Arsitektur**: `MailSignatureService` pada kode `mail-service` harus diimplementasikan dengan *Strategy Pattern* agar siap menerima provider TTE di masa depan tanpa mengubah logika bisnis inti.

## Post-MVP Roadmap
1. **Q3 2026**: Pengajuan PKS formal ke BSrE BSSN dan penyediaan infrastruktur server.
2. **Q4 2026**: Uji coba teknis integrasi API BSrE (Esign Client) pada lingkungan staging.
3. **Q1 2027**: Implementasi penuh TTE Tersertifikasi untuk kategori surat prioritas tinggi.

## References
- `docs/PRD-migrasi-mail-disposisi.md` Open Question #9 & User Story 32.
- `mail-service-421` (Decision issue).
- `mail-service-2qv` (Implementation issue - Post-MVP).

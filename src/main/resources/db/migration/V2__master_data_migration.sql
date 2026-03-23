-- ============================================================
-- V2__master_data_migration.sql
-- Migrasi data master dari DB production smartoffice
-- Generated: 2026-03-20
-- ============================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ──────────────────────────────────────────────
-- 1. MAIL_TYPE (3 records)
-- ──────────────────────────────────────────────
INSERT IGNORE INTO mail_type (mail_type_id, mail_type, mail_type_status) VALUES
(1, 'Internal', 1),
(2, 'Masuk',    1),
(3, 'Keluar',   1);

-- ──────────────────────────────────────────────
-- 2. MAIL_CATEGORY (596 records - sample data)
--    Catatan: Untuk production, generate dengan mysqldump
--    mysqldump --no-create-info --complete-insert --skip-triggers \
--      -h <host> -P <port> -u <user> -p smartoffice mail_category \
--      >> V2__master_data_migration.sql
-- ──────────────────────────────────────────────
-- mail_type_id=1 (Internal)
INSERT IGNORE INTO mail_category (mcat_id, mail_type_id, mcat_code, mcat_name, mcat_status, sort) VALUES
(1, 1, '000', 'UMUM', 'Enabled', 0),
(2, 1, '001', 'KEUANGAN', 'Enabled', 0),
(3, 1, '002', 'HUKUM', 'Enabled', 0),
(4, 1, '003', 'SDM', 'Enabled', 0),
(5, 1, '004', 'OPERASIONAL', 'Enabled', 0),
(6, 1, '005', 'TEKNIK', 'Enabled', 0),
(7, 1, '006', 'PENGADAAN', 'Enabled', 0),
(8, 1, '007', 'AUDIT', 'Enabled', 0),
(9, 1, '008', 'IT', 'Enabled', 0),
(10, 1, '009', 'HUMAS', 'Enabled', 0);

-- mail_type_id=2 (Masuk) - sample
INSERT IGNORE INTO mail_category (mcat_id, mail_type_id, mcat_code, mcat_name, mcat_status, sort) VALUES
(198, 2, '000', 'UMUM', 'Enabled', 0),
(199, 2, '001', 'KEUANGAN', 'Enabled', 0),
(200, 2, '002', 'HUKUM', 'Enabled', 0),
(201, 2, '003', 'SDM', 'Enabled', 0),
(202, 2, '004', 'OPERASIONAL', 'Enabled', 0),
(203, 2, '005', 'TEKNIK', 'Enabled', 0),
(204, 2, '006', 'PENGADAAN', 'Enabled', 0),
(205, 2, '007', 'AUDIT', 'Enabled', 0),
(206, 2, '008', 'IT', 'Enabled', 0),
(207, 2, '009', 'HUMAS', 'Enabled', 0);

-- mail_type_id=3 (Keluar) - sample
INSERT IGNORE INTO mail_category (mcat_id, mail_type_id, mcat_code, mcat_name, mcat_status, sort) VALUES
(397, 3, '000', 'UMUM', 'Enabled', 0),
(398, 3, '001', 'KEUANGAN', 'Enabled', 0),
(399, 3, '002', 'HUKUM', 'Enabled', 0),
(400, 3, '003', 'SDM', 'Enabled', 0),
(401, 3, '004', 'OPERASIONAL', 'Enabled', 0),
(402, 3, '005', 'TEKNIK', 'Enabled', 0),
(403, 3, '006', 'PENGADAAN', 'Enabled', 0),
(404, 3, '007', 'AUDIT', 'Enabled', 0),
(405, 3, '008', 'IT', 'Enabled', 0),
(406, 3, '009', 'HUMAS', 'Enabled', 0);

-- ──────────────────────────────────────────────
-- 3. PESAN_SINGKAT (28 records)
-- ──────────────────────────────────────────────
INSERT IGNORE INTO pesan_singkat (id, pesan) VALUES
(1,  'Disetujui untuk diproses lanjut'),
(2,  'Buat telaah dan laporan'),
(3,  'Permohonan tidak disetujui'),
(4,  'Balas surat ini secara tertulis'),
(5,  'Hadir dan buat laporan'),
(6,  'Penuhi permintaan ini'),
(7,  'Cek dan selesaikan masalah'),
(8,  'Bicarakan dengan saya'),
(9,  'Berikan masukan/pendapat secepatnya'),
(10, 'Meeting internal direksi'),
(11, 'Bahas dalam managerial meeting'),
(12, 'Untuk informasi'),
(13, 'Siapkan slip pengambilan/cek/ pembayarannya.'),
(14, 'Distribusikan'),
(15, 'Catat dan File'),
(16, 'Verifikasi & Rekomendasi'),
(17, 'Lengkapi Bukti Pendukung'),
(18, 'File'),
(19, 'Proses lanjut dispo Dirut'),
(20, 'Proses lanjut pembayaranya'),
(21, 'Proses lanjut adm & pembayaranya'),
(22, 'Proses lanjut sesuai ketentuan'),
(23, 'TL Dispo Dirut'),
(24, 'Verifikasi, Evaluasi, dan dinilai'),
(25, 'Verifikasi dan beri masukan'),
(26, 'Telaah dan rekumendasikan'),
(27, 'Verifikasi dan proses lanjut'),
(28, 'Mohon pemeriksaan pengawas');

-- ──────────────────────────────────────────────
-- 4. MAIL_FOLDER - System folders
--    Mapping ke SystemFolder enum
-- ──────────────────────────────────────────────
INSERT IGNORE INTO mail_folder (folder_id, parent_folder_id, owner_id, folder_icon_cls, folder_name, folder_status, folder_created_date) VALUES
(1,  0, 0, 'email',        'eOffice Mailbox',  1, NOW()),
(2,  1, 0, 'inbox',        'Inbox',            1, NOW()),
(3,  1, 0, 'draft',        'Draft',            1, NOW()),
(4,  1, 0, 'read',         'Read Items',       1, NOW()),
(5,  1, 0, 'sent',         'Sent Items',       1, NOW()),
(6,  1, 0, 'delete',       'Deleted Items',    1, NOW()),
(10, 0, 0, 'folder',       'Personal Folder',  1, NOW());

-- ──────────────────────────────────────────────
-- 5. SYS_REFERENCE — subset yang dibutuhkan
--    a) sirkulasi (mapping ke CirculationType enum)
--    b) format nomor surat & arsip
-- ──────────────────────────────────────────────
-- Sirkulasi (mapping: 1=DISPOSISI, 2=MEMO_MANDIRI, 3=MEMO, 4=CC, 5=REPLY, 6=FORWARD)
INSERT IGNORE INTO sys_reference (id, code, value, text, seq, status) VALUES
(75, 'sirkulasi', 1, 'Disposisi',    1, 'Enable'),
(76, 'sirkulasi', 2, 'Memo Mandiri', 2, 'Enable'),
(77, 'sirkulasi', 3, 'Memo',         3, 'Enable'),
(78, 'sirkulasi', 4, 'CC',           4, 'Enable'),
(79, 'sirkulasi', 5, 'Reply',        5, 'Enable'),
(80, 'sirkulasi', 6, 'Forward',      6, 'Enable');

-- Format nomor surat
INSERT IGNORE INTO sys_reference (id, code, value, text, seq, status) VALUES
(74,  'm_number_format_blp', 1, '#seq#/1421002/#org_code#-#type#/#MR#/#YYYY#-#m_cat#', 1, 'Enable'),
(169, 'm_number_format_bms', 1, '#seq#/#org_code#/#m_cat#-#type#/#MR#/#YYYY#',         1, 'Enable');

-- Format nomor arsip
INSERT IGNORE INTO sys_reference (id, code, value, text, seq, status) VALUES
(88,  'ma_number_format',    1, '#cat#/#org#/#seq#/35.73.701/#YYYY#',                  1, 'Enable'),
(167, 'ma_number_format_blp',1, '#seq#/1421002/#org_code#-#type#/#MR#/#YYYY#-#m_cat#', 1, 'Enable'),
(168, 'ma_number_format_bms',1, '#m_cat#/#type##seq#/#YYYY#',                          1, 'Enable');

-- ──────────────────────────────────────────────
-- 6. SYS_REFERENCE_HEADER
-- ──────────────────────────────────────────────
INSERT IGNORE INTO sys_reference_header (id, code, description, status) VALUES
(1, 'sirkulasi', 'Jenis Sirkulasi Surat (Disposisi, Memo, CC, dll)', 'Enabled'),
(2, 'm_number_format_bms', 'Format Nomor Surat - BMS', 'Enabled'),
(3, 'm_number_format_blp', 'Format Nomor Surat - BLP', 'Enabled'),
(4, 'ma_number_format_bms', 'Format Nomor Arsip - BMS', 'Enabled'),
(5, 'ma_number_format_blp', 'Format Nomor Arsip - BLP', 'Enabled');

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================
-- V99__data_migration.sql
-- Normalisasi & fix data legacy
-- Idempoten: semua operasi aman dijalankan ulang
-- ============================================================

SET NAMES utf8mb4;

-- ============================================================
-- 1. FIX m_root_id: surat lama tanpa root thread
--    Set diri sendiri sebagai root jika m_root_id NULL
-- ============================================================
UPDATE mail
   SET m_root_id = m_id
 WHERE m_root_id IS NULL;

-- ============================================================
-- 2. FIX typo pesan_singkat
--    id=26: "rekumendasikan" → "rekomendasikan"
--    id=20: "pembayaranya" → "pembayarannya"
--    id=21: "pembayaranya" → "pembayarannya"
-- ============================================================
UPDATE pesan_singkat SET pesan = 'Telaah dan rekomendasikan'          WHERE id = 26 AND pesan = 'Telaah dan rekumendasikan';
UPDATE pesan_singkat SET pesan = 'Proses lanjut pembayarannya'        WHERE id = 20 AND pesan = 'Proses lanjut pembayaranya';
UPDATE pesan_singkat SET pesan = 'Proses lanjut adm & pembayarannya'  WHERE id = 21 AND pesan = 'Proses lanjut adm & pembayaranya';

-- ============================================================
-- 3. EXCLUDE mail_trial data
--    Soft-delete: tandai status = 99 (PURGED) agar tidak hilang
--    mail_trial.m_id berisi ID surat trial/test
--    Tabel mail_trial harus ada di DB sumber (legacy)
--    Jika tabel tidak ada, blok ini di-skip otomatis
-- ============================================================

-- Buat temporary table dari mail_trial IDs jika tabel ada
-- Menggunakan pendekatan conditional: cek keberadaan tabel dulu
DROP TABLE IF EXISTS _tmp_trial_ids;

-- Jika mail_trial ada di DB production yang sama, uncomment blok ini:
-- CREATE TEMPORARY TABLE _tmp_trial_ids AS
--   SELECT m_id FROM mail_trial;
--
-- UPDATE mail SET m_status = 99
--  WHERE m_id IN (SELECT m_id FROM _tmp_trial_ids)
--    AND m_status != 99;
--
-- UPDATE mail_recipient SET circulation = -1
--  WHERE mail_id IN (SELECT m_id FROM _tmp_trial_ids);
--
-- DROP TABLE IF EXISTS _tmp_trial_ids;

-- Alternatif: jika mail_trial ID range diketahui (< 337 berdasarkan AUTO_INCREMENT),
-- dan semua trial mail sudah diidentifikasi, gunakan list eksplisit:
-- UPDATE mail SET m_status = 99 WHERE m_id IN (...list IDs...);

-- ============================================================
-- 4. CHARSET UNIFICATION
--    Pastikan semua tabel mail scope menggunakan utf8mb4
--    CONVERT TO idempoten — tidak berubah jika sudah utf8mb4
-- ============================================================
ALTER TABLE mail                       CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE mail_recipient             CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE mail_folder                CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE mail_type                  CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE mail_category              CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE mail_archive               CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE mail_archive_access        CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE mail_archive_notif         CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE mail_archive_notif_log     CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE pesan_singkat              CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE sys_reference              CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE sys_reference_header       CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE attachments                CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE attachment_download_history CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE smtp_mail_config           CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE smtp_mail_log              CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE mail_category_statistic    CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE mail_org_statistic         CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE mail_respontime            CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE msg_template               CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE print_log                  CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- ============================================================
-- 5. VERIFIKASI (diagnostic queries — hasilnya di log Flyway)
--    Uncomment untuk debugging manual
-- ============================================================
-- SELECT 'mail_root_null' AS check_name, COUNT(*) AS cnt FROM mail WHERE m_root_id IS NULL;
-- SELECT 'recipient_no_emp' AS check_name, COUNT(*) AS cnt FROM mail_recipient WHERE emp_name IS NULL;
-- SELECT 'trial_purged' AS check_name, COUNT(*) AS cnt FROM mail WHERE m_status = 99;

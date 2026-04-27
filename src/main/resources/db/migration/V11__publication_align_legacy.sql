-- ============================================================
-- V11: Selaraskan tabel area_publik dengan struktur legacy smartoffice
--   - Rename kolom: judul -> title, desk -> description
--   - Pisah file storage: file_name + file_path -> original_file_name + system_file_name
--   - Naikkan panjang created_by_name & created_by_title ke VARCHAR(128)
-- ============================================================

-- 1. Rename kolom konten utama agar match nama legacy
ALTER TABLE `area_publik`
  CHANGE COLUMN `judul` `title`       VARCHAR(255) NOT NULL,
  CHANGE COLUMN `desk`  `description` TEXT NULL;

-- 2. Tambah kolom file pasangan legacy (original + system)
ALTER TABLE `area_publik`
  ADD COLUMN `original_file_name` VARCHAR(256) NULL AFTER `notif_flag`,
  ADD COLUMN `system_file_name`   VARCHAR(256) NULL AFTER `original_file_name`;

-- 3. Backfill kolom baru dari kolom lama (file_name = original; file_path = relative path → ambil basename sebagai system)
UPDATE `area_publik`
   SET `original_file_name` = `file_name`,
       `system_file_name`   = SUBSTRING_INDEX(`file_path`, '/', -1)
 WHERE `file_name` IS NOT NULL
    OR `file_path` IS NOT NULL;

-- 4. Drop kolom lama yang sudah dipindah datanya
ALTER TABLE `area_publik`
  DROP COLUMN `file_name`,
  DROP COLUMN `file_path`;

-- 5. Naikkan panjang kolom snapshot pembuat agar match legacy (VARCHAR(128))
ALTER TABLE `area_publik`
  MODIFY COLUMN `created_by_name`  VARCHAR(128) NULL,
  MODIFY COLUMN `created_by_title` VARCHAR(128) NULL;

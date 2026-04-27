-- =============================================================================
-- V13: Normalisasi system_file_name pada area_publik
--   - Menghilangkan prefix path (folder) yang mungkin terbawa saat migrasi.
--   - Menangani separator Windows (\) dan Linux (/).
--   - Memastikan hanya basename yang tersimpan di kolom system_file_name.
-- =============================================================================

UPDATE `area_publik`
   SET `system_file_name` = TRIM(SUBSTRING_INDEX(REPLACE(`system_file_name`, '\\', '/'), '/', -1))
 WHERE `system_file_name` IS NOT NULL 
   AND (`system_file_name` LIKE '%/%' OR `system_file_name` LIKE '%\\%');

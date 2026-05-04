-- V21__mail_archive_access_pos_id_int_and_fk.sql
-- mail-service-8ly: Convert pos_id varchar->int + FK + align columns

SET FOREIGN_KEY_CHECKS = 0;

-- 1. Align columns to legacy schema
-- Rename position_id to pos_id (it was changed in V4)
ALTER TABLE mail_archive_access
  CHANGE COLUMN position_id pos_id INT NOT NULL DEFAULT 0;

-- Add legacy permission columns
ALTER TABLE mail_archive_access
  ADD COLUMN access   CHAR(1) NOT NULL DEFAULT 'Y' AFTER pos_id,
  ADD COLUMN download CHAR(1) NOT NULL DEFAULT 'Y' AFTER access,
  ADD COLUMN history  CHAR(1) NOT NULL DEFAULT 'Y' AFTER download;

-- 2. Drop non-legacy columns
ALTER TABLE mail_archive_access
  DROP COLUMN IF EXISTS access_level,
  DROP COLUMN IF EXISTS granted_date,
  DROP COLUMN IF EXISTS granted_by;

-- 3. Add Foreign Key
ALTER TABLE mail_archive_access
  ADD CONSTRAINT fk_archive_access_archive 
  FOREIGN KEY (mail_archive_id) REFERENCES mail_archive(ma_id) ON DELETE CASCADE;

SET FOREIGN_KEY_CHECKS = 1;

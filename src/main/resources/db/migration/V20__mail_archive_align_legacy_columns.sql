-- V20__mail_archive_align_legacy_columns.sql
-- mail-service-rim: align mail_archive with legacy schema + FK + indexes

SET FOREIGN_KEY_CHECKS = 0;

-- 1. Align column names to legacy schema
-- We use CHANGE COLUMN to rename while preserving types and order where possible.
ALTER TABLE mail_archive 
  CHANGE COLUMN ma_date ma_mail_date date DEFAULT NULL,
  CHANGE COLUMN ma_mail_id ma_ref_id bigint(20) DEFAULT NULL,
  CHANGE COLUMN ma_category ma_mcat_id int(11) DEFAULT NULL,
  CHANGE COLUMN ma_created_date ma_archive_date datetime DEFAULT NULL,
  CHANGE COLUMN ma_created_by_name ma_archive_by_name varchar(64) DEFAULT NULL,
  CHANGE COLUMN ma_office_code office_code varchar(32) DEFAULT NULL,
  CHANGE COLUMN ma_rack ma_loc_rack varchar(32) DEFAULT NULL,
  CHANGE COLUMN ma_shelf ma_loc_tier varchar(32) DEFAULT NULL,
  CHANGE COLUMN ma_box ma_loc_box varchar(32) DEFAULT NULL,
  CHANGE COLUMN ma_keyword_flag ma_keyword_index_flag varchar(256) DEFAULT NULL;

-- 2. Add missing legacy columns
ALTER TABLE mail_archive
  ADD COLUMN ma_mcat_type INT DEFAULT NULL AFTER ma_mcat_id,
  ADD COLUMN ma_mcat_code VARCHAR(32) DEFAULT NULL AFTER ma_mcat_type,
  ADD COLUMN ma_org_code VARCHAR(16) DEFAULT NULL AFTER ma_mcat_code,
  ADD COLUMN ma_org_id INT DEFAULT NULL AFTER ma_org_code,
  ADD COLUMN ma_ref_no VARCHAR(45) DEFAULT NULL AFTER ma_org_id,
  ADD COLUMN ma_sent_to VARCHAR(128) DEFAULT NULL AFTER ma_ref_no,
  ADD COLUMN ma_note VARCHAR(512) DEFAULT NULL AFTER ma_subject,
  ADD COLUMN ma_secret_type VARCHAR(45) DEFAULT NULL AFTER ma_note,
  ADD COLUMN ma_loc_building INT DEFAULT NULL AFTER ma_loc_box,
  ADD COLUMN ma_loc_floor INT DEFAULT NULL AFTER ma_loc_building,
  ADD COLUMN ma_loc_room INT DEFAULT NULL AFTER ma_loc_floor,
  ADD COLUMN ma_keyword TEXT DEFAULT NULL AFTER ma_keyword_index_flag;

-- Ensure ma_updated_date exists and is populated
ALTER TABLE mail_archive ADD COLUMN IF NOT EXISTS ma_updated_date DATETIME NULL AFTER ma_archive_date;
UPDATE mail_archive SET ma_updated_date = ma_archive_date WHERE ma_updated_date IS NULL;

-- 3. Drop columns not present in legacy
ALTER TABLE mail_archive
  DROP COLUMN IF EXISTS ma_year,
  DROP COLUMN IF EXISTS ma_folder_pos,
  DROP COLUMN IF EXISTS ma_published_at,
  DROP COLUMN IF EXISTS ma_created_by,
  DROP COLUMN IF EXISTS ma_attachment_qty;

-- 4. Cleanup orphan category references
UPDATE mail_archive SET ma_mcat_id = NULL
  WHERE ma_mcat_id IS NOT NULL
    AND ma_mcat_id NOT IN (SELECT mcat_id FROM mail_category);

-- 5. Add Foreign Key
ALTER TABLE mail_archive
  ADD CONSTRAINT fk_archive_category FOREIGN KEY (ma_mcat_id) REFERENCES mail_category(mcat_id) ON DELETE SET NULL;

-- 6. Add Indexes for performance
CREATE INDEX idx_archive_ref_mail ON mail_archive(ma_ref_id);
CREATE INDEX idx_archive_status_date ON mail_archive(ma_status, ma_mail_date);
CREATE INDEX idx_archive_org ON mail_archive(ma_org_id);

SET FOREIGN_KEY_CHECKS = 1;

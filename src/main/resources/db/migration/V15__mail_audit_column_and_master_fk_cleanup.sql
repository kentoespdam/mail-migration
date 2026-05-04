-- V15__mail_audit_column_and_master_fk_cleanup.sql
-- Add audit columns to mail table and clean up master data foreign keys
-- Decision: Add created_at, updated_at, is_deleted to mail table

ALTER TABLE `mail`
  ADD COLUMN IF NOT EXISTS `m_updated_date` DATETIME NULL AFTER `m_penerima_surat_keluar`,
  ADD COLUMN IF NOT EXISTS `created_at` DATETIME NULL DEFAULT CURRENT_TIMESTAMP AFTER `m_updated_date`,
  ADD COLUMN IF NOT EXISTS `updated_at` DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP AFTER `created_at`,
  ADD COLUMN IF NOT EXISTS `is_deleted` TINYINT(1) DEFAULT 0 AFTER `updated_at`;

-- Clean up invalid mail_type and mail_category in mail table (set to NULL or default if orphaned)
-- This ensures FK creation won't fail
UPDATE `mail` SET `m_type` = NULL WHERE `m_type` IS NOT NULL AND `m_type` NOT IN (SELECT `mail_type_id` FROM `mail_type`);
UPDATE `mail` SET `m_category` = NULL WHERE `m_category` IS NOT NULL AND `m_category` NOT IN (SELECT `mcat_id` FROM `mail_category`);

-- Cleanup orphan m_root_id (if not exists in mail table, set to NULL)
UPDATE `mail` SET `m_root_id` = NULL WHERE `m_root_id` IS NOT NULL AND `m_root_id` NOT IN (SELECT `m_id` FROM (SELECT `m_id` FROM `mail`) as tmp);

-- Populate m_updated_date if null from m_created_date
UPDATE `mail` SET `m_updated_date` = `m_created_date` WHERE `m_updated_date` IS NULL AND `m_created_date` IS NOT NULL;

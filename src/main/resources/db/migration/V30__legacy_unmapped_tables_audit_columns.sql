-- V30__legacy_unmapped_tables_audit_columns.sql
-- mail-service-ses: audit columns for 6 legacy unmapped tables

SET FOREIGN_KEY_CHECKS = 0;

-- Adding audit/standard columns to tables that will have entities later.
-- This ensures they are ready for the new application's standard fields.

-- 1. mail_archive_notif
ALTER TABLE mail_archive_notif 
  ADD COLUMN IF NOT EXISTS updated_at DATETIME NULL;

-- 2. mail_archive_notif_log
-- (Already has notif_date, which acts as created_at)

-- 3. mail_respontime
ALTER TABLE mail_respontime
  ADD COLUMN IF NOT EXISTS created_at DATETIME NULL,
  ADD COLUMN IF NOT EXISTS updated_at DATETIME NULL;

-- 4. mail_org_statistic
ALTER TABLE mail_org_statistic
  ADD COLUMN IF NOT EXISTS updated_at DATETIME NULL;

-- 5. mail_category_statistic
ALTER TABLE mail_category_statistic
  ADD COLUMN IF NOT EXISTS updated_at DATETIME NULL;

-- 6. msg_template
ALTER TABLE msg_template
  ADD COLUMN IF NOT EXISTS created_at DATETIME NULL,
  ADD COLUMN IF NOT EXISTS updated_at DATETIME NULL;

SET FOREIGN_KEY_CHECKS = 1;

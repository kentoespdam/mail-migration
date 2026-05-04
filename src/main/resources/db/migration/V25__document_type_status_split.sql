-- V25__document_type_status_split.sql
-- mail-service-bmn: jenis_dokumen status split + DocumentType refactor

SET FOREIGN_KEY_CHECKS = 0;

-- 1. Add new columns for refactored status if they don't exist
-- We keep 'status' (varchar) for legacy compatibility but use 'status_new' (ENUM) and 'is_deleted' for the new app.
ALTER TABLE jenis_dokumen
  ADD COLUMN IF NOT EXISTS status_new ENUM('ACTIVE','INACTIVE') NOT NULL DEFAULT 'ACTIVE' AFTER status,
  ADD COLUMN IF NOT EXISTS is_deleted TINYINT(1) NOT NULL DEFAULT 0 AFTER status_new;

-- 2. Migrate existing data
-- status in legacy: 'ACTIVE', 'INACTIVE'
UPDATE jenis_dokumen SET
  status_new = CASE WHEN status = 'ACTIVE' OR status = '1' THEN 'ACTIVE' ELSE 'INACTIVE' END,
  is_deleted = CASE WHEN status = 'DELETED' OR status = '0' THEN 1 ELSE 0 END;

-- 3. Add Index IF NOT EXISTS (MariaDB 10.5+)
CREATE OR REPLACE INDEX idx_doctype_active ON jenis_dokumen(status_new, is_deleted);

SET FOREIGN_KEY_CHECKS = 1;

-- V26__quick_message_innodb_audit_fulltext.sql
-- mail-service-7z7: pesan_singkat InnoDB + audit + FULLTEXT

SET FOREIGN_KEY_CHECKS = 0;

-- 1. Convert engine to InnoDB for Foreign Key support
ALTER TABLE pesan_singkat ENGINE=InnoDB;

-- 2. Add new columns for refactored status and audit
-- We keep 'status' (varchar) added in V5 for legacy compatibility but use 'status_new' (ENUM) and 'is_deleted' for the new app.
ALTER TABLE pesan_singkat
  ADD COLUMN status_new ENUM('ACTIVE','INACTIVE') NOT NULL DEFAULT 'ACTIVE' AFTER status,
  ADD COLUMN is_deleted TINYINT(1) NOT NULL DEFAULT 0 AFTER status_new,
  ADD COLUMN IF NOT EXISTS created_date DATETIME NULL,
  ADD COLUMN IF NOT EXISTS updated_date DATETIME NULL;

-- 3. Migrate existing data
UPDATE pesan_singkat SET
  status_new = CASE WHEN status = 'INACTIVE' THEN 'INACTIVE' ELSE 'ACTIVE' END,
  is_deleted = CASE WHEN status = 'DELETED' THEN 1 ELSE 0 END,
  created_date = IFNULL(created_date, NOW()),
  updated_date = IFNULL(updated_date, NOW());

-- 4. Add Full-text Index
ALTER TABLE pesan_singkat
  ADD FULLTEXT INDEX ft_pesan (pesan);

SET FOREIGN_KEY_CHECKS = 1;

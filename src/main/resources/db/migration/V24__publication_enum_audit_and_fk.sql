-- V24__publication_enum_audit_and_fk.sql
-- mail-service-t1b: area_publik enum migration + audit + FK

SET FOREIGN_KEY_CHECKS = 0;

-- 1. Migrate ENUM values for status
-- Using VARCHAR as intermediate to avoid case-insensitive duplication error in ENUM definition
ALTER TABLE area_publik MODIFY status VARCHAR(32);

UPDATE area_publik SET status='DRAFT' WHERE status='Draft';
UPDATE area_publik SET status='PUBLISHED' WHERE status='Ditampilkan';
UPDATE area_publik SET status='DRAFT' WHERE status NOT IN ('DRAFT', 'PUBLISHED', 'DELETED');

ALTER TABLE area_publik
  MODIFY status ENUM('DRAFT','PUBLISHED','DELETED') NOT NULL DEFAULT 'DRAFT';

-- 2. Add Audit Columns
-- These columns are expected by the Publication entity but might be missing in legacy.
ALTER TABLE area_publik 
  ADD COLUMN IF NOT EXISTS created_at DATETIME NULL,
  ADD COLUMN IF NOT EXISTS updated_at DATETIME NULL;

-- Backfill from published_date if empty
UPDATE area_publik SET created_at = published_date WHERE created_at IS NULL;
UPDATE area_publik SET updated_at = published_date WHERE updated_at IS NULL;

-- 3. Add Foreign Key
-- Linking Publication to DocumentType (jenis_dokumen)
ALTER TABLE area_publik
  ADD CONSTRAINT fk_publication_doctype FOREIGN KEY (type) REFERENCES jenis_dokumen(id) ON DELETE SET NULL;

SET FOREIGN_KEY_CHECKS = 1;

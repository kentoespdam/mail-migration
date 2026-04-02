-- ============================================================
-- V9: DocumentType (jenis_dokumen) enhancement
-- ============================================================

-- 1. Add status_new column (to migrate from Integer to VARCHAR Enum)
ALTER TABLE `jenis_dokumen` 
ADD COLUMN `status_new` VARCHAR(20) DEFAULT 'ACTIVE' AFTER `jenis_dokumen`;

-- 2. Migrate existing status values (1: ACTIVE, 0: INACTIVE)
UPDATE `jenis_dokumen` 
SET `status_new` = CASE 
    WHEN `status` = 1 THEN 'ACTIVE'
    WHEN `status` = 0 THEN 'INACTIVE'
    ELSE 'ACTIVE'
END;

-- 3. Drop old status and rename status_new
ALTER TABLE `jenis_dokumen` 
DROP COLUMN `status`;

ALTER TABLE `jenis_dokumen`
CHANGE COLUMN `status_new` `status` VARCHAR(20) NOT NULL;

-- 4. Ensure name length is consistent and migrate ID to BIGINT for Sqids compatibility
-- Need to drop FK first to change column type
ALTER TABLE `area_publik` DROP FOREIGN KEY `fk_publik_type`;

ALTER TABLE `jenis_dokumen`
MODIFY COLUMN `jenis_dokumen` VARCHAR(100) NOT NULL,
MODIFY COLUMN `id` BIGINT AUTO_INCREMENT;

ALTER TABLE `area_publik`
MODIFY COLUMN `id` BIGINT AUTO_INCREMENT,
MODIFY COLUMN `type` BIGINT;

-- Re-add FK
ALTER TABLE `area_publik`
ADD CONSTRAINT `fk_publik_type` FOREIGN KEY (`type`) REFERENCES `jenis_dokumen`(`id`);

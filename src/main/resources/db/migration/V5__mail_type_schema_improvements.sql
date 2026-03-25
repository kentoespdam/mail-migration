-- ============================================================
-- V5__mail_type_schema_improvements.sql
-- Fix legacy schema issues identified in analysis:
-- 1. Drop composite PK, use single PK on mail_type_id
-- 2. Convert mail_type_status from int to ENUM string
-- 3. Add FK constraint mail_category → mail_type
-- 4. Add partial unique index via generated column (MariaDB workaround)
-- ============================================================

-- 1. Fix composite PK → single PK
ALTER TABLE mail_type DROP PRIMARY KEY, ADD PRIMARY KEY (mail_type_id);

-- 2. Convert mail_type_status: int → ENUM('ACTIVE','INACTIVE','DELETED')
--    Legacy mapping: 1→ACTIVE, 2→INACTIVE, 3→DELETED
ALTER TABLE mail_type ADD COLUMN mail_type_status_new ENUM('ACTIVE','INACTIVE','DELETED') NOT NULL DEFAULT 'ACTIVE';

UPDATE mail_type SET mail_type_status_new = CASE mail_type_status
    WHEN 1 THEN 'ACTIVE'
    WHEN 2 THEN 'INACTIVE'
    WHEN 3 THEN 'DELETED'
    ELSE 'ACTIVE'
END;

ALTER TABLE mail_type DROP COLUMN mail_type_status;
ALTER TABLE mail_type CHANGE COLUMN mail_type_status_new mail_type_status ENUM('ACTIVE','INACTIVE','DELETED') NOT NULL DEFAULT 'ACTIVE';

-- 3. Partial unique index via generated column (MariaDB doesn't support WHERE in CREATE INDEX)
ALTER TABLE mail_type
    ADD COLUMN mail_type_unique_name VARCHAR(32) AS (
        CASE WHEN mail_type_status != 'DELETED' THEN mail_type ELSE NULL END
    ) VIRTUAL,
    ADD UNIQUE INDEX uq_mail_type_active (mail_type_unique_name);

-- 4. FK constraint: mail_category.mail_type_id → mail_type.mail_type_id
ALTER TABLE mail_category
    ADD CONSTRAINT fk_mcat_mail_type
    FOREIGN KEY (mail_type_id) REFERENCES mail_type(mail_type_id)
    ON DELETE RESTRICT ON UPDATE CASCADE;

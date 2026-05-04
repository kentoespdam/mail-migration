-- V28__mail_category_fk_mail_type.sql
-- mail-service-mcat: Ensure mail_category FK to mail_type

SET FOREIGN_KEY_CHECKS = 0;

-- Drop if exists and recreate to ensure correct definition
ALTER TABLE mail_category DROP FOREIGN KEY IF EXISTS fk_mcat_mail_type;

ALTER TABLE mail_category
  ADD CONSTRAINT fk_mcat_mail_type FOREIGN KEY (mail_type_id) REFERENCES mail_type(mail_type_id) ON DELETE CASCADE;

SET FOREIGN_KEY_CHECKS = 1;

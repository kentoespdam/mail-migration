-- V27__mail_folder_parent_nullable_and_fk.sql
-- mail-service-128: parent_folder_id nullable + FK

SET FOREIGN_KEY_CHECKS = 0;

-- 1. Modify parent_folder_id to be nullable
-- Legacy schema has parent_folder_id NOT NULL. 
-- In the new application, root folders (parent_folder_id = 0) should have NULL parent.
ALTER TABLE mail_folder MODIFY parent_folder_id INT(11) NULL;

-- 2. Convert root sentinel value (0) to NULL
UPDATE mail_folder SET parent_folder_id = NULL WHERE parent_folder_id = 0;

-- 3. Add Foreign Key for self-reference
ALTER TABLE mail_folder
  ADD CONSTRAINT fk_folder_parent FOREIGN KEY (parent_folder_id) REFERENCES mail_folder(folder_id) ON DELETE RESTRICT;

SET FOREIGN_KEY_CHECKS = 1;

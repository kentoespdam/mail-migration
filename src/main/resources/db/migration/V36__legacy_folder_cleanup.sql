-- V36__legacy_folder_cleanup.sql
-- mail-service-aaj: Rescue tasks from status=3 folders and hard-delete them.

SET FOREIGN_KEY_CHECKS = 0;

-- 1. Create backup for folders to be deleted
CREATE TABLE IF NOT EXISTS `mail_folder_deleted_backup_20260508` AS
SELECT * FROM mail_folder WHERE folder_status = 3;

-- 2. Move tasks from deleted personal folders (status=3) to System DELETED folder (ID 6)
-- SystemFolder.DELETED.id = 6
UPDATE sys_user_task 
SET folder_id = 6 
WHERE folder_id IN (SELECT folder_id FROM mail_folder WHERE folder_status = 3);

-- 3. Hard-delete legacy personal folders with status 3 (soft-deleted)
DELETE FROM mail_folder WHERE folder_status = 3;

SET FOREIGN_KEY_CHECKS = 1;

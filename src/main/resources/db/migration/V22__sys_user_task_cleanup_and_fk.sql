-- V22__sys_user_task_cleanup_and_fk.sql
-- mail-service-pct: Cleanup 57 orphan + FK tm_id

SET FOREIGN_KEY_CHECKS = 0;

-- 1. Cleanup orphan user tasks (where tm_id no longer exists in mail table)
-- Estimated ~57 rows to be deleted.
DELETE FROM sys_user_task WHERE tm_id NOT IN (SELECT m_id FROM mail);

-- 2. Align types: mail.m_id is BIGINT(20), sys_user_task.tm_id must match
ALTER TABLE sys_user_task MODIFY COLUMN tm_id BIGINT(20) NULL;

-- 3. Add Foreign Key for tm_id
ALTER TABLE sys_user_task
  ADD CONSTRAINT fk_usertask_mail FOREIGN KEY (tm_id) REFERENCES mail(m_id) ON DELETE CASCADE;

-- 4. Note on folder_id FK:
-- folder_id = -1 (PURGED) is a sentinel value and does not exist in mail_folder.
-- Therefore, a standard FK on folder_id is not enforceable without moving purged tasks 
-- to a separate table or using a more complex validation.

SET FOREIGN_KEY_CHECKS = 1;

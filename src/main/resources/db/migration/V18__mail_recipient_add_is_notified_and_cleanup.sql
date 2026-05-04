-- V18__mail_recipient_add_is_notified_and_cleanup.sql
-- mail-service-haf: ADD is_notified + cleanup orphans + backfill

SET FOREIGN_KEY_CHECKS = 0;

-- 1. Ensure is_notified column exists
-- (Note: V3 already added this, but we use IF NOT EXISTS for safety as per Plan 24 strategy)
ALTER TABLE mail_recipient ADD COLUMN IF NOT EXISTS is_notified TINYINT(1) NOT NULL DEFAULT 0 AFTER sms;

-- 2. Cleanup orphan recipients (where mail_id no longer exists in mail table)
-- Estimated ~37,756 rows to be deleted.
DELETE FROM mail_recipient WHERE mail_id NOT IN (SELECT m_id FROM mail);

-- 3. Backfill is_notified=1
-- Based on sys_user_task.read_status or legacy notifications if available.
UPDATE mail_recipient mr
JOIN sys_user_task ut ON ut.tm_id = mr.mail_id AND ut.user_id = mr.user_id
SET mr.is_notified = 1 
WHERE ut.read_status = 1;

-- 4. Drop redundant columns that were added in previous migrations (V3) but are not in legacy schema
-- Plan 24 says these are redundant/not needed.
ALTER TABLE mail_recipient DROP COLUMN IF EXISTS is_read;
ALTER TABLE mail_recipient DROP COLUMN IF EXISTS folder_position;

SET FOREIGN_KEY_CHECKS = 1;

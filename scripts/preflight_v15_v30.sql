-- scripts/preflight_v15_v30.sql
-- Pre-flight verification script before running migrations V15 - V30
-- Run this on your target database to identify data issues that might cause migration failures.

-- 1. Backup Reminder
SELECT 'WARNING: Ensure you have backups for mail, mail_recipient, sys_user_task, and attachments tables!' AS info;

-- 2. Verify duplicates in attachments.id (Blocker for V23)
SELECT 'Checking for duplicates in attachments.id...' AS task;
SELECT id, COUNT(*) as duplicate_count 
FROM attachments 
GROUP BY id 
HAVING duplicate_count > 1;

-- 3. Verify non-numeric pos_id in mail_archive_access (Risk for V21)
-- Only relevant if we need to convert pos_id from VARCHAR to INT.
-- In V21 we use CHANGE COLUMN, so non-numeric values might fail or be truncated to 0.
SELECT 'Checking for non-numeric position_id in mail_archive_access...' AS task;
SELECT position_id, COUNT(*) as count
FROM mail_archive_access 
WHERE position_id NOT REGEXP '^[0-9]+$'
GROUP BY position_id;

-- 4. Verify duplicates in mail_recipient (mail_id, user_id) (Risk for V19)
SELECT 'Checking for duplicates in mail_recipient (mail_id, user_id)...' AS task;
SELECT mail_id, user_id, COUNT(*) as duplicate_count
FROM mail_recipient 
GROUP BY mail_id, user_id 
HAVING duplicate_count > 1;

-- 5. Orphan Count Analysis (Informational - will be cleaned by migrations)
SELECT 'Orphan Analysis (Expected to be cleaned by V15, V16, V18, V22, V29):' AS info;

SELECT 'mail.m_parent_id orphans' as label, COUNT(*) as count 
FROM mail WHERE m_parent_id IS NOT NULL AND m_parent_id NOT IN (SELECT m_id FROM mail);

SELECT 'mail_recipient.mail_id orphans' as label, COUNT(*) as count 
FROM mail_recipient WHERE mail_id NOT IN (SELECT m_id FROM mail);

SELECT 'sys_user_task.tm_id orphans' as label, COUNT(*) as count 
FROM sys_user_task WHERE tm_id NOT IN (SELECT m_id FROM mail);

SELECT 'print_log.mail_id orphans' as label, COUNT(*) as count 
FROM print_log WHERE mail_id IS NOT NULL AND mail_id NOT IN (SELECT m_id FROM mail);

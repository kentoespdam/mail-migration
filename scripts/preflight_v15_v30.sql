-- Pre-flight verification script for V15..V30 migration
-- This script validates assumptions about legacy data before running structural migrations.
-- It is READ-ONLY and does not modify any data.

-- 1. Duplikat attachments.id (blocker untuk V23)
SELECT 'attachments.id duplicates' AS label, COUNT(*) AS cnt
FROM (SELECT id FROM attachments GROUP BY id HAVING COUNT(*) > 1) x;

-- 2. position_id non-numeric di mail_archive_access (blocker untuk V21)
SELECT 'mail_archive_access.position_id non-numeric' AS label, COUNT(*) AS cnt
FROM mail_archive_access
WHERE position_id NOT REGEXP '^[0-9]+$' AND position_id IS NOT NULL AND position_id <> '';

-- 3. Duplikat (mail_id, user_id) di mail_recipient (blocker untuk V19)
SELECT 'mail_recipient (mail_id,user_id) duplicates' AS label, COUNT(*) AS cnt
FROM (SELECT mail_id, user_id FROM mail_recipient
      GROUP BY mail_id, user_id HAVING COUNT(*) > 1) x;

-- 4. Snapshot orphan count
SELECT 'mail.m_type orphan' AS label, COUNT(*) AS cnt
  FROM mail WHERE m_type IS NOT NULL
    AND m_type NOT IN (SELECT mail_type_id FROM mail_type);

SELECT 'mail.m_category orphan' AS label, COUNT(*) AS cnt
  FROM mail WHERE m_category IS NOT NULL
    AND m_category NOT IN (SELECT mcat_id FROM mail_category);

SELECT 'mail.m_root_id orphan' AS label, COUNT(*) AS cnt
  FROM mail WHERE m_root_id IS NOT NULL
    AND m_root_id NOT IN (SELECT m_id FROM mail);

SELECT 'mail.m_parent_id orphan' AS label, COUNT(*) AS cnt
  FROM mail WHERE m_parent_id IS NOT NULL
    AND m_parent_id NOT IN (SELECT m_id FROM mail);

SELECT 'mail_recipient.mail_id orphan' AS label, COUNT(*) AS cnt
  FROM mail_recipient WHERE mail_id NOT IN (SELECT m_id FROM mail);

SELECT 'sys_user_task.tm_id orphan' AS label, COUNT(*) AS cnt
  FROM sys_user_task WHERE tm_id NOT IN (SELECT m_id FROM mail);

SELECT 'print_log.mail_id orphan' AS label, COUNT(*) AS cnt
  FROM print_log WHERE mail_id IS NOT NULL
    AND mail_id NOT IN (SELECT m_id FROM mail);

SELECT 'attachment_download_history.attachment_id orphan' AS label, COUNT(*) AS cnt
  FROM attachment_download_history
    WHERE attachment_id NOT IN (SELECT id FROM attachments);

-- 5. Engine verification
SELECT 'pesan_singkat engine' AS label, ENGINE AS info
  FROM information_schema.TABLES
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'pesan_singkat';

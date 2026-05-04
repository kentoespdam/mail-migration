-- V16__mail_parent_id_orphan_cleanup_batch.sql
-- mail-service-rla: Batch cleanup m_parent_id orphans

-- 1. Create backup table for the orphans to be cleaned
CREATE TABLE IF NOT EXISTS `mail_parent_backup_20260504` AS
SELECT m.m_id, m.m_parent_id
FROM mail m
LEFT JOIN mail p ON m.m_parent_id = p.m_id
WHERE m.m_parent_id IS NOT NULL AND p.m_id IS NULL;

-- 2. Perform the cleanup using LEFT JOIN
UPDATE mail m
LEFT JOIN mail p ON m.m_parent_id = p.m_id
SET m.m_parent_id = NULL
WHERE m.m_parent_id IS NOT NULL AND p.m_id IS NULL;

-- 3. Verify cleanup (optional, but good for logging if run manually)
-- SELECT COUNT(*) FROM mail m LEFT JOIN mail p ON m.m_parent_id = p.m_id WHERE m.m_parent_id IS NOT NULL AND p.m_id IS NULL;

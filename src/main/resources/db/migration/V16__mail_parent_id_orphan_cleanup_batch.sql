-- V16__mail_parent_id_orphan_cleanup_batch.sql
-- mail-service-rla: Batch cleanup m_parent_id orphans
-- Optimized using LEFT JOIN for better performance and Flyway compatibility

UPDATE mail m
LEFT JOIN mail p ON m.m_parent_id = p.m_id
SET m.m_parent_id = NULL
WHERE m.m_parent_id IS NOT NULL AND p.m_id IS NULL;

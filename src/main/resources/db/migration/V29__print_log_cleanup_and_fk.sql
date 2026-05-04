-- V29__print_log_cleanup_and_fk.sql
-- mail-service-8ad: print_log cleanup + FK + entity length fix

SET FOREIGN_KEY_CHECKS = 0;

-- 1. Cleanup orphan print logs (where mail_id no longer exists)
-- Estimated ~10 rows.
DELETE FROM print_log 
WHERE mail_id IS NOT NULL 
  AND mail_id NOT IN (SELECT m_id FROM mail);

-- 2. Add Foreign Key
ALTER TABLE print_log
  ADD CONSTRAINT fk_printlog_mail FOREIGN KEY (mail_id) REFERENCES mail(m_id) ON DELETE CASCADE;

SET FOREIGN_KEY_CHECKS = 1;

-- V34__mail_org_statistic_index.sql
-- mail-service-7pr: Index for mail_org_statistic and add created_at if missing

SET FOREIGN_KEY_CHECKS = 0;

ALTER TABLE mail_org_statistic
  ADD COLUMN IF NOT EXISTS created_at DATETIME NULL,
  ADD INDEX idx_org_stat_period_org (period_month, created_by_org);

SET FOREIGN_KEY_CHECKS = 1;

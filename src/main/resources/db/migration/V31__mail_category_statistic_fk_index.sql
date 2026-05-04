-- V31__mail_category_statistic_fk_index.sql
-- mail-service-nb4: FK and index for mail_category_statistic

SET FOREIGN_KEY_CHECKS = 0;

-- Clean up any invalid category_id before adding FK
DELETE FROM mail_category_statistic 
WHERE category_id IS NOT NULL 
  AND category_id NOT IN (SELECT mcat_id FROM mail_category);

ALTER TABLE mail_category_statistic
  ADD CONSTRAINT fk_mcs_category FOREIGN KEY (category_id) REFERENCES mail_category(mcat_id) ON DELETE CASCADE,
  ADD INDEX idx_mcs_period_category (period_month, category_id);

SET FOREIGN_KEY_CHECKS = 1;

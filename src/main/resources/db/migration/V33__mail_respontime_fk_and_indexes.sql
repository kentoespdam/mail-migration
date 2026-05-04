-- V33__mail_respontime_fk_and_indexes.sql
-- Add foreign keys and indexes to mail_respontime table

SET FOREIGN_KEY_CHECKS = 0;

-- Pre-flight cleanup: set NULL for orphaned FK values
UPDATE mail_respontime SET orig_m_id = NULL
  WHERE orig_m_id IS NOT NULL AND orig_m_id NOT IN (SELECT m_id FROM mail);

UPDATE mail_respontime SET reply_m_id = NULL
  WHERE reply_m_id IS NOT NULL AND reply_m_id NOT IN (SELECT m_id FROM mail);

UPDATE mail_respontime SET m_type = NULL
  WHERE m_type IS NOT NULL AND m_type NOT IN (SELECT mail_type_id FROM mail_type);

UPDATE mail_respontime SET m_category = NULL
  WHERE m_category IS NOT NULL AND m_category NOT IN (SELECT mcat_id FROM mail_category);

-- Add foreign keys
ALTER TABLE mail_respontime
  ADD CONSTRAINT fk_respon_orig
    FOREIGN KEY (orig_m_id) REFERENCES mail(m_id) ON DELETE SET NULL,
  ADD CONSTRAINT fk_respon_reply
    FOREIGN KEY (reply_m_id) REFERENCES mail(m_id) ON DELETE SET NULL,
  ADD CONSTRAINT fk_respon_type
    FOREIGN KEY (m_type) REFERENCES mail_type(mail_type_id) ON DELETE SET NULL,
  ADD CONSTRAINT fk_respon_cat
    FOREIGN KEY (m_category) REFERENCES mail_category(mcat_id) ON DELETE SET NULL;

-- Add indexes
CREATE INDEX IF NOT EXISTS idx_respon_orig_date ON mail_respontime(orig_date);
CREATE INDEX IF NOT EXISTS idx_respon_type_cat ON mail_respontime(m_type, m_category);

SET FOREIGN_KEY_CHECKS = 1;

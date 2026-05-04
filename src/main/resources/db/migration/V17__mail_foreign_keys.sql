-- V17__mail_foreign_keys.sql
-- mail-service-9le: mail FK + indexes

-- Adding Foreign Keys to mail table
-- Prerequisites (orphan cleanup) were handled in V15 and V16.
ALTER TABLE mail
  ADD CONSTRAINT fk_mail_type FOREIGN KEY (m_type) REFERENCES mail_type(mail_type_id) ON DELETE SET NULL,
  ADD CONSTRAINT fk_mail_category FOREIGN KEY (m_category) REFERENCES mail_category(mcat_id) ON DELETE SET NULL,
  ADD CONSTRAINT fk_mail_root FOREIGN KEY (m_root_id) REFERENCES mail(m_id) ON DELETE SET NULL,
  ADD CONSTRAINT fk_mail_parent FOREIGN KEY (m_parent_id) REFERENCES mail(m_id) ON DELETE SET NULL;

-- New composite indexes for performance optimization (as replacements for partitioning)
CREATE INDEX idx_mail_status_date ON mail(m_status, m_date);
CREATE INDEX idx_mail_created_by_date ON mail(m_created_by, m_created_date);

-- V19__mail_recipient_dedupe_and_fk.sql
-- mail-service-34k: Dedupe (mail_id, user_id) + unique + FK

SET FOREIGN_KEY_CHECKS = 0;

-- 1. Deduplicate mail_recipient rows
-- We keep only the row with the smallest ID for each (mail_id, user_id) pair.
DELETE mr1 FROM mail_recipient mr1
INNER JOIN mail_recipient mr2
  ON mr1.mail_id = mr2.mail_id
 AND mr1.user_id = mr2.user_id
 AND mr1.id > mr2.id;

-- 2. Add Unique Constraint
-- This prevents future duplicates and is expected by the MailRecipient JPA entity.
ALTER TABLE mail_recipient
  ADD CONSTRAINT uq_recipient_mail_user UNIQUE (mail_id, user_id);

-- 3. Add Foreign Key
-- Links mail_recipient to the mail table.
ALTER TABLE mail_recipient
  ADD CONSTRAINT fk_recipient_mail FOREIGN KEY (mail_id) REFERENCES mail(m_id) ON DELETE CASCADE;

SET FOREIGN_KEY_CHECKS = 1;

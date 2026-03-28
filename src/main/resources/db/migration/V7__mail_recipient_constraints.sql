-- V5: Add unique constraint and FK cascade for mail_recipient (BUG-01, BUG-07)

-- BUG-01: Unique constraint to prevent duplicate recipients per mail
-- Drop existing non-unique index first, then add unique constraint
DROP INDEX IF EXISTS mail_with_user ON mail_recipient;
ALTER TABLE mail_recipient
    ADD CONSTRAINT uk_mail_recipient_mail_user UNIQUE (mail_id, user_id);

-- BUG-07: FK with cascade delete so deleting a mail cleans up recipients
ALTER TABLE mail_recipient
    DROP FOREIGN KEY IF EXISTS mr_mail_id,
    ADD CONSTRAINT fk_mail_recipient_mail
        FOREIGN KEY (mail_id) REFERENCES mail(m_id) ON DELETE CASCADE;

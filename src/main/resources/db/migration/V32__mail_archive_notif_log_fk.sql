-- V32 for MailArchiveNotifLog FK and index
-- Clean up orphan rows before adding FK
DELETE FROM mail_archive_notif_log
  WHERE mail_archive_id NOT IN (SELECT ma_id FROM mail_archive);

-- Add foreign key with cascade
ALTER TABLE mail_archive_notif_log
  ADD CONSTRAINT fk_archive_notiflog_archive
    FOREIGN KEY (mail_archive_id) REFERENCES mail_archive(ma_id)
    ON DELETE CASCADE ON UPDATE CASCADE;

-- Create index on user_id + mail_archive_id
CREATE INDEX idx_notiflog_user_archive ON mail_archive_notif_log(user_id, mail_archive_id);

-- V31__mail_archive_notif_fk.sql
-- Add foreign key to mail_archive_notif

SET FOREIGN_KEY_CHECKS = 0;

-- Cleanup orphan rows
DELETE FROM mail_archive_notif
  WHERE mail_archive_id NOT IN (SELECT ma_id FROM mail_archive);

ALTER TABLE mail_archive_notif
  ADD CONSTRAINT fk_archive_notif_archive
    FOREIGN KEY (mail_archive_id) REFERENCES mail_archive(ma_id)
    ON DELETE CASCADE ON UPDATE CASCADE;

SET FOREIGN_KEY_CHECKS = 1;

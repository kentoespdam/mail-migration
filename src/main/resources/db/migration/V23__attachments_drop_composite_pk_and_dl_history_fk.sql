-- V23__attachments_drop_composite_pk_and_dl_history_fk.sql
-- mail-service-8qv: PK simplification + FK download_history

SET FOREIGN_KEY_CHECKS = 0;

-- 1. Simplify attachments Primary Key
-- Legacy schema uses composite PK (id, upload_date). 
-- New application uses single 'id' PK with AUTO_INCREMENT.

-- Before running this in production, verify there are no duplicate IDs:
-- SELECT id, COUNT(*) c FROM attachments GROUP BY id HAVING c > 1;

ALTER TABLE attachments
  DROP PRIMARY KEY,
  MODIFY id INT(11) NOT NULL AUTO_INCREMENT,
  ADD PRIMARY KEY (id);

-- 2. Cleanup orphan download history
-- Ensure all history records point to a valid attachment.
DELETE FROM attachment_download_history
  WHERE attachment_id NOT IN (SELECT id FROM attachments);

-- 3. Add Foreign Key for download history
ALTER TABLE attachment_download_history
  ADD CONSTRAINT fk_dlhist_attachment 
  FOREIGN KEY (attachment_id) REFERENCES attachments(id) ON DELETE CASCADE;

-- 4. Optional: Add check constraint for ref_type
-- 1 = Mail, 2 = Archive
ALTER TABLE attachments
  ADD CONSTRAINT chk_ref_type CHECK (ref_type IN (1, 2));

SET FOREIGN_KEY_CHECKS = 1;

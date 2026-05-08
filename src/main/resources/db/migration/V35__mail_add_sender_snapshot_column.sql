-- V35__mail_add_sender_snapshot_column.sql
-- mail-service-293: Add m_sender_snapshot JSON column to mail table for historical audit

ALTER TABLE `mail`
  ADD COLUMN `m_sender_snapshot` JSON NULL AFTER `updated_at`;

-- Note: In MariaDB 11.4, adding a column is usually INSTANT if it's the last column or nullable.
-- JSON column is stored as LONGTEXT internally in MariaDB with JSON check constraint.

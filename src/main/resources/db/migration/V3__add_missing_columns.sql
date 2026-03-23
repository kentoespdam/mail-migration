-- ============================================================
-- V3: Add columns expected by JPA entities but missing from
--     baseline schema.
-- ============================================================

-- mail_recipient: is_notified, is_read, folder_position
ALTER TABLE mail_recipient
    ADD COLUMN is_notified TINYINT(1) NOT NULL DEFAULT 0 AFTER sms,
    ADD COLUMN is_read     TINYINT(1) NOT NULL DEFAULT 0 AFTER is_notified,
    ADD COLUMN folder_position INT DEFAULT NULL AFTER is_read;

-- pesan_singkat: status (for soft-delete, used by @SQLRestriction)
ALTER TABLE pesan_singkat
    ADD COLUMN status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE' AFTER pesan;

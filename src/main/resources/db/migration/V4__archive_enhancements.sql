-- ============================================================
-- V4: Add columns to mail_archive for location, year,
--     office_code, and keyword_flag.
-- ============================================================

ALTER TABLE mail_archive
    ADD COLUMN ma_year        SMALLINT     DEFAULT NULL AFTER ma_attachment_qty,
    ADD COLUMN ma_office_code VARCHAR(32)  DEFAULT NULL AFTER ma_year,
    ADD COLUMN ma_rack        VARCHAR(32)  DEFAULT NULL AFTER ma_office_code,
    ADD COLUMN ma_shelf       VARCHAR(32)  DEFAULT NULL AFTER ma_rack,
    ADD COLUMN ma_box         VARCHAR(32)  DEFAULT NULL AFTER ma_shelf,
    ADD COLUMN ma_folder_pos  VARCHAR(32)  DEFAULT NULL AFTER ma_box,
    ADD COLUMN ma_keyword_flag VARCHAR(256) DEFAULT NULL AFTER ma_folder_pos,
    ADD COLUMN ma_published_at DATETIME    DEFAULT NULL AFTER ma_keyword_flag,
    ADD COLUMN ma_updated_date DATETIME    DEFAULT NULL AFTER ma_published_at;

-- Index for office_code + status filtering
CREATE INDEX idx_ma_office_status ON mail_archive (ma_office_code, ma_status);
CREATE INDEX idx_ma_year ON mail_archive (ma_year);

-- mail_archive_access: change user_id to position_id for jabatan-based ACL
ALTER TABLE mail_archive_access
    CHANGE COLUMN `user_id` `position_id` INT(11) DEFAULT NULL,
    DROP KEY `maa_user_id`,
    ADD KEY `maa_position_id` (`position_id`);

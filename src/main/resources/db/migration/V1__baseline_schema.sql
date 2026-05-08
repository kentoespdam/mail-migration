-- ============================================================
-- V1__baseline_schema.sql
-- Baseline schema untuk Mail Service dari smartoffice.sql
-- Tabel master data untuk mail service
-- ============================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ============================================================
-- 1. MAIL_TYPE
-- ============================================================
DROP TABLE IF EXISTS `mail_type`;
CREATE TABLE `mail_type` (
  `mail_type_id` int(11) NOT NULL AUTO_INCREMENT,
  `mail_type` varchar(32) NOT NULL,
  `mail_type_status` int(11) DEFAULT 1 COMMENT '1: Active, 3: Deleted',
  PRIMARY KEY (`mail_type_id`, `mail_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 2. MAIL_CATEGORY
-- ============================================================
DROP TABLE IF EXISTS `mail_category`;
CREATE TABLE `mail_category` (
  `mcat_id` int(11) NOT NULL AUTO_INCREMENT,
  `mail_type_id` int(11) NOT NULL,
  `mcat_code` varchar(32) NOT NULL DEFAULT '',
  `mcat_name` varchar(64) NOT NULL DEFAULT ' ',
  `mcat_status` enum('Enabled','Disabled','Deleted') DEFAULT 'Enabled',
  `sort` int(11) DEFAULT 0,
  PRIMARY KEY (`mcat_id`),
  KEY `mail_type_id_idx` (`mail_type_id`),
  KEY `mcat_code_idx` (`mcat_code`),
  KEY `mcat_status_idx` (`mcat_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 3. MAIL_FOLDER
-- ============================================================
DROP TABLE IF EXISTS `mail_folder`;
CREATE TABLE `mail_folder` (
  `folder_id` int(11) NOT NULL AUTO_INCREMENT,
  `parent_folder_id` int(11) NOT NULL,
  `owner_id` int(11) NOT NULL COMMENT '0: System, > 0 : Personal Folder',
  `folder_icon_cls` varchar(45) DEFAULT NULL,
  `folder_name` varchar(45) NOT NULL,
  `folder_status` int(11) NOT NULL DEFAULT 1,
  `folder_created_date` datetime DEFAULT NULL,
  PRIMARY KEY (`folder_id`),
  KEY `folder_with_mail` (`folder_id`, `parent_folder_id`, `owner_id`),
  KEY `folder_owner_status` (`owner_id`, `folder_status`),
  KEY `parent_folder_idx` (`parent_folder_id`),
  KEY `folder_with_owner` (`folder_id`, `owner_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 4. MAIL
-- ============================================================
DROP TABLE IF EXISTS `mail`;
CREATE TABLE `mail` (
  `m_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `m_no` varchar(64) DEFAULT NULL,
  `m_date` date DEFAULT NULL,
  `m_root_id` bigint(20) DEFAULT NULL,
  `m_parent_id` bigint(20) DEFAULT NULL,
  `m_type` int(11) DEFAULT NULL,
  `m_category` int(11) DEFAULT NULL,
  `m_subject` varchar(256) DEFAULT NULL,
  `m_content` text DEFAULT NULL,
  `m_note` text DEFAULT NULL,
  `m_max_response_date` datetime DEFAULT NULL,
  `m_status` tinyint(4) DEFAULT NULL,
  `m_created_date` datetime DEFAULT NULL,
  `m_created_by` int(11) DEFAULT NULL,
  `m_created_by_name` varchar(64) DEFAULT NULL,
  `m_attachment_qty` int(11) DEFAULT 0,
  `m_to_str` text DEFAULT NULL,
  `m_rab_id` int(11) DEFAULT NULL,
  `m_ma_id` bigint(20) DEFAULT NULL,
  `m_no_surat_masuk` varchar(64) DEFAULT NULL,
  `m_asal_surat_masuk` varchar(128) DEFAULT NULL,
  `m_tgl_surat_masuk` date DEFAULT NULL,
  `m_tujuan_surat_keluar` varchar(128) DEFAULT NULL,
  `m_penerima_surat_keluar` varchar(128) DEFAULT NULL,
  `m_updated_date` datetime DEFAULT NULL,
  PRIMARY KEY (`m_id`),
  KEY `m_root_id` (`m_root_id`),
  KEY `m_parent_id` (`m_parent_id`),
  KEY `m_type` (`m_type`),
  KEY `m_category` (`m_category`),
  KEY `m_created_by` (`m_created_by`),
  KEY `m_status_idx` (`m_status`),
  KEY `m_created_date_idx` (`m_created_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 5. MAIL_RECIPIENT
-- ============================================================
DROP TABLE IF EXISTS `mail_recipient`;
CREATE TABLE `mail_recipient` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `mail_id` bigint(20) DEFAULT NULL,
  `user_id` int(11) DEFAULT NULL,
  `emp_id` int(11) DEFAULT NULL,
  `emp_name` varchar(64) DEFAULT NULL,
  `pos_id` int(11) DEFAULT NULL,
  `pos_name` varchar(64) DEFAULT NULL,
  `circulation` int(11) DEFAULT NULL,
  `email` tinyint(1) DEFAULT 0,
  `sms` tinyint(1) DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `mr_mail_id` (`mail_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_emp_id` (`emp_id`),
  KEY `idx_circulation` (`circulation`),
  KEY `mail_with_user` (`mail_id`, `user_id`),
  KEY `user_with_mail` (`user_id`, `mail_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 6. MAIL_ARCHIVE
-- ============================================================
DROP TABLE IF EXISTS `mail_archive`;
CREATE TABLE `mail_archive` (
  `ma_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `ma_no` varchar(64) DEFAULT NULL,
  `ma_date` date DEFAULT NULL,
  `ma_mail_id` bigint(20) DEFAULT NULL,
  `ma_category` int(11) DEFAULT NULL,
  `ma_subject` varchar(256) DEFAULT NULL,
  `ma_content` text DEFAULT NULL,
  `ma_status` tinyint(4) DEFAULT NULL,
  `ma_created_date` datetime DEFAULT NULL,
  `ma_created_by` int(11) DEFAULT NULL,
  `ma_created_by_name` varchar(64) DEFAULT NULL,
  `ma_attachment_qty` int(11) DEFAULT 0,
  PRIMARY KEY (`ma_id`),
  KEY `ma_mail_id` (`ma_mail_id`),
  KEY `ma_category` (`ma_category`),
  KEY `ma_created_date_idx` (`ma_created_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 7. MAIL_ARCHIVE_ACCESS
-- ============================================================
DROP TABLE IF EXISTS `mail_archive_access`;
CREATE TABLE `mail_archive_access` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `mail_archive_id` bigint(20) DEFAULT NULL,
  `user_id` int(11) DEFAULT NULL,
  `access_level` int(11) DEFAULT NULL COMMENT '1: Read, 2: Write, 3: Delete',
  `granted_date` datetime DEFAULT NULL,
  `granted_by` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `maa_archive_id` (`mail_archive_id`),
  KEY `maa_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 8. PESAN_SINGKAT
-- ============================================================
DROP TABLE IF EXISTS `pesan_singkat`;
CREATE TABLE `pesan_singkat` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `pesan` varchar(128) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 9. SYS_REFERENCE
-- ============================================================
DROP TABLE IF EXISTS `sys_reference`;
CREATE TABLE `sys_reference` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `code` varchar(32) DEFAULT NULL,
  `value` int(11) DEFAULT NULL,
  `text` varchar(64) DEFAULT NULL,
  `seq` int(11) DEFAULT NULL,
  `status` enum('Enable','Disable','Deleted') DEFAULT NULL,
  `num_1` int(11) DEFAULT 0,
  vchar1 varchar(32) DEFAULT '',
  `time_1` time DEFAULT NULL,
  `time_2` time DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `IDX_CODE` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 10. SYS_REFERENCE_HEADER
-- ============================================================
DROP TABLE IF EXISTS `sys_reference_header`;
CREATE TABLE `sys_reference_header` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `code` varchar(32) DEFAULT NULL,
  `description` varchar(64) DEFAULT NULL,
  `status` enum('Enabled','Disabled') DEFAULT 'Enabled',
  PRIMARY KEY (`id`),
  KEY `fk_code_idx` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 11. SMTP_MAIL_CONFIG
-- ============================================================
DROP TABLE IF EXISTS `smtp_mail_config`;
CREATE TABLE `smtp_mail_config` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `config` varchar(64) DEFAULT NULL,
  `value` text DEFAULT NULL,
  `last_update` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `config_UNIQUE` (`config`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 12. SMTP_MAIL_LOG
-- ============================================================
DROP TABLE IF EXISTS `smtp_mail_log`;
CREATE TABLE `smtp_mail_log` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `mail_id` bigint(20) DEFAULT NULL,
  `recipient_email` varchar(128) DEFAULT NULL,
  `sent_date` datetime DEFAULT NULL,
  `status` varchar(32) DEFAULT NULL,
  `error_message` text DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `sml_mail_id` (`mail_id`),
  KEY `sml_sent_date` (`sent_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 13. MAIL_ARCHIVE_NOTIF
-- ============================================================
DROP TABLE IF EXISTS `mail_archive_notif`;
CREATE TABLE `mail_archive_notif` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `mail_archive_id` bigint(20) DEFAULT NULL,
  `user_id` int(11) DEFAULT NULL,
  `notif_date` datetime DEFAULT NULL,
  `status` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `man_archive_id` (`mail_archive_id`),
  KEY `man_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 14. MAIL_ARCHIVE_NOTIF_LOG
-- ============================================================
DROP TABLE IF EXISTS `mail_archive_notif_log`;
CREATE TABLE `mail_archive_notif_log` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `mail_archive_id` bigint(20) DEFAULT NULL,
  `user_id` int(11) DEFAULT NULL,
  `notif_date` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY mmanlyarchive_id (`mail_archive_id`, `user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 15. MAIL_CATEGORY_STATISTIC
-- ============================================================
DROP TABLE IF EXISTS `mail_category_statistic`;
CREATE TABLE `mail_category_statistic` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `period_month` int(11) DEFAULT NULL,
  `category_id` int(11) DEFAULT NULL,
  `total` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 16. MAIL_ORG_STATISTIC
-- ============================================================
DROP TABLE IF EXISTS `mail_org_statistic`;
CREATE TABLE `mail_org_statistic` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `period_month` int(11) DEFAULT NULL,
  `created_by_org` int(11) DEFAULT NULL,
  `total` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 17. MAIL_RESPONTIME
-- ============================================================
DROP TABLE IF EXISTS `mail_respontime`;
CREATE TABLE `mail_respontime` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `orig_m_id` bigint(20) DEFAULT NULL,
  `orig_date` datetime DEFAULT NULL,
  `reply_m_id` bigint(20) DEFAULT NULL,
  `reply_date` datetime DEFAULT NULL,
  `m_type` int(11) DEFAULT NULL,
  `m_category` int(11) DEFAULT NULL,
  `respon_time` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 18. MSG_TEMPLATE
-- ============================================================
DROP TABLE IF EXISTS `msg_template`;
CREATE TABLE `msg_template` (
  `template_id` int(11) NOT NULL AUTO_INCREMENT,
  `message` text DEFAULT NULL,
  `description` varchar(128) DEFAULT NULL,
  PRIMARY KEY (`template_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 19. PRINT_LOG
-- ============================================================
DROP TABLE IF EXISTS `print_log`;
CREATE TABLE `print_log` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `auth_code` varchar(32) DEFAULT NULL,
  `date` datetime DEFAULT NULL,
  `mail_id` int(11) DEFAULT NULL,
  `username` varchar(128) DEFAULT NULL,
  `ip_address` varchar(32) DEFAULT '',
  PRIMARY KEY (`id`),
  KEY `idx_01` (`auth_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 20. SYS_USER_TASK
-- ============================================================
DROP TABLE IF EXISTS `sys_user_task`;
CREATE TABLE `sys_user_task` (
  `user_task_id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` int(11) DEFAULT NULL,
  `tm_id` int(11) DEFAULT NULL,
  `folder_id` int(11) DEFAULT NULL,
  `read_status` int(11) DEFAULT NULL,
  `read_date` datetime DEFAULT NULL,
  `restore_folder_id` int(11) DEFAULT NULL,
  `mail_created_date` datetime DEFAULT NULL,
  PRIMARY KEY (`user_task_id`),
  KEY `idx_ut_user_folder` (`user_id`, `folder_id`),
  KEY `idx_ut_user_mail` (`user_id`, `tm_id`),
  KEY `tm_id_idx` (`tm_id`),
  KEY `folder_id_idx` (`folder_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 22. ATTACHMENTS
-- ============================================================
DROP TABLE IF EXISTS `attachments`;
CREATE TABLE `attachments` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `ref_type` int(11) DEFAULT NULL COMMENT '1: Mail, 2: Arsip',
  `ref_id` bigint(20) DEFAULT NULL,
  `file_ext` varchar(8) DEFAULT NULL,
  `file_size` int(11) DEFAULT NULL,
  `original_filename` varchar(128) DEFAULT NULL,
  `system_filename` varchar(128) DEFAULT NULL,
  `doc_notes` varchar(128) DEFAULT NULL,
  `upload_date` datetime NOT NULL,
  `upload_by_name` varchar(64) DEFAULT NULL,
  `status` int(11) DEFAULT NULL COMMENT '1: Available, 2: Deleted',
  `rec_flag` int(11) DEFAULT NULL,
  `approve_date` datetime DEFAULT NULL,
  `approve_by` varchar(64) DEFAULT NULL,
  PRIMARY KEY (`id`, `upload_date`) USING BTREE,
  KEY `attachment_idx` (`ref_type`, `ref_id`),
  KEY `ref_type` (`ref_type`),
  KEY `ref_id` (`ref_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 23. ATTACHMENT_DOWNLOAD_HISTORY
-- ============================================================
DROP TABLE IF EXISTS `attachment_download_history`;
CREATE TABLE `attachment_download_history` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `attachment_id` int(11) DEFAULT NULL,
  `user_id` int(11) DEFAULT NULL,
  `emp_name` varchar(64) DEFAULT NULL,
  `emp_pos_name` varchar(64) DEFAULT NULL,
  `download_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `adh_attachment_id` (`attachment_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

SET FOREIGN_KEY_CHECKS = 1;

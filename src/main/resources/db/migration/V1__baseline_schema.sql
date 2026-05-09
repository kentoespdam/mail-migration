-- V1__baseline_schema.sql
-- Squashed baseline of schema after V1..V40 + V99 effects.
-- Generated from authoritative MariaDB schema dump (smartoffice_mail).
-- Notes: mail.m_subject -> TEXT, mail.m_content -> LONGTEXT.

SET FOREIGN_KEY_CHECKS = 0;

CREATE TABLE `allowed_file_type` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `context` varchar(50) NOT NULL,
  `extension` varchar(20) NOT NULL,
  `max_size_mb` int(11) NOT NULL DEFAULT 10,
  `is_active` tinyint(1) NOT NULL DEFAULT 1,
  `created_at` datetime DEFAULT current_timestamp(),
  `updated_at` datetime DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_ctx_ext` (`context`,`extension`)
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `area_publik` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `title` varchar(255) NOT NULL,
  `description` text DEFAULT NULL,
  `type` bigint(20) DEFAULT NULL,
  `status` enum('DRAFT','PUBLISHED','DELETED') NOT NULL DEFAULT 'DRAFT',
  `published_date` datetime DEFAULT NULL,
  `notif_flag` int(11) NOT NULL DEFAULT 0,
  `original_file_name` varchar(256) DEFAULT NULL,
  `system_file_name` varchar(256) DEFAULT NULL,
  `file_size` int(11) DEFAULT NULL,
  `created_by_name` varchar(128) DEFAULT NULL,
  `created_by_title` varchar(128) DEFAULT NULL,
  `created_by_user_id` int(11) DEFAULT NULL,
  `created_at` datetime DEFAULT current_timestamp(),
  `updated_at` datetime DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  PRIMARY KEY (`id`),
  KEY `idx_publik_status_date` (`status`,`published_date` DESC),
  KEY `idx_publik_notif` (`notif_flag`),
  KEY `fk_publication_doctype` (`type`),
  CONSTRAINT `fk_publication_doctype` FOREIGN KEY (`type`) REFERENCES `jenis_dokumen` (`id`) ON DELETE SET NULL,
  CONSTRAINT `fk_publik_type` FOREIGN KEY (`type`) REFERENCES `jenis_dokumen` (`id`),
  CONSTRAINT `chk_publik_status` CHECK (`status` in ('DRAFT','PUBLISHED','DELETED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `attachment_download_history` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `attachment_id` int(11) DEFAULT NULL,
  `user_id` int(11) DEFAULT NULL,
  `emp_name` varchar(64) DEFAULT NULL,
  `emp_pos_name` varchar(64) DEFAULT NULL,
  `download_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `adh_attachment_id` (`attachment_id`),
  CONSTRAINT `fk_dlhist_attachment` FOREIGN KEY (`attachment_id`) REFERENCES `attachments` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

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
  PRIMARY KEY (`id`),
  KEY `attachment_idx` (`ref_type`,`ref_id`),
  KEY `ref_type` (`ref_type`),
  KEY `ref_id` (`ref_id`),
  CONSTRAINT `chk_ref_type` CHECK (`ref_type` in (1,2))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `jenis_dokumen` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `jenis_dokumen` varchar(100) NOT NULL,
  `status` varchar(20) NOT NULL,
  `status_new` enum('ACTIVE','INACTIVE') NOT NULL DEFAULT 'ACTIVE',
  `is_deleted` tinyint(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_doctype_active` (`status_new`,`is_deleted`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `mail` (
  `m_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `m_no` varchar(64) DEFAULT NULL,
  `m_date` date DEFAULT NULL,
  `m_root_id` bigint(20) DEFAULT NULL,
  `m_parent_id` bigint(20) DEFAULT NULL,
  `m_type` int(11) DEFAULT NULL,
  `m_category` int(11) DEFAULT NULL,
  `m_subject` TEXT DEFAULT NULL,
  `m_content` LONGTEXT DEFAULT NULL,
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
  `created_at` datetime DEFAULT current_timestamp(),
  `updated_at` datetime DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  `m_sender_snapshot` longtext DEFAULT NULL CHECK (json_valid(`m_sender_snapshot`)),
  `is_deleted` tinyint(1) DEFAULT 0,
  PRIMARY KEY (`m_id`),
  KEY `m_root_id` (`m_root_id`),
  KEY `m_parent_id` (`m_parent_id`),
  KEY `m_type` (`m_type`),
  KEY `m_category` (`m_category`),
  KEY `m_created_by` (`m_created_by`),
  KEY `m_status_idx` (`m_status`),
  KEY `m_created_date_idx` (`m_created_date`),
  KEY `idx_mail_status_date` (`m_status`,`m_date`),
  KEY `idx_mail_created_by_date` (`m_created_by`,`m_created_date`),
  FULLTEXT KEY `ft_mail_subject_content` (`m_subject`,`m_content`),
  CONSTRAINT `fk_mail_category` FOREIGN KEY (`m_category`) REFERENCES `mail_category` (`mcat_id`) ON DELETE SET NULL,
  CONSTRAINT `fk_mail_parent` FOREIGN KEY (`m_parent_id`) REFERENCES `mail` (`m_id`) ON DELETE SET NULL,
  CONSTRAINT `fk_mail_root` FOREIGN KEY (`m_root_id`) REFERENCES `mail` (`m_id`) ON DELETE SET NULL,
  CONSTRAINT `fk_mail_type` FOREIGN KEY (`m_type`) REFERENCES `mail_type` (`mail_type_id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `mail_action_log` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `mail_id` bigint(20) NOT NULL,
  `action` varchar(32) NOT NULL,
  `description` varchar(255) DEFAULT NULL,
  `username` varchar(100) NOT NULL,
  `ip_address` varchar(50) DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT current_timestamp(),
  `old_value` varchar(100) DEFAULT NULL,
  `new_value` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_mail_action_log_mail_id` (`mail_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `mail_archive` (
  `ma_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `ma_no` varchar(64) DEFAULT NULL,
  `ma_mail_date` date DEFAULT NULL,
  `ma_ref_id` bigint(20) DEFAULT NULL,
  `ma_mcat_id` int(11) DEFAULT NULL,
  `ma_mcat_type` int(11) DEFAULT NULL,
  `ma_mcat_code` varchar(32) DEFAULT NULL,
  `ma_org_code` varchar(16) DEFAULT NULL,
  `ma_org_id` int(11) DEFAULT NULL,
  `ma_ref_no` varchar(45) DEFAULT NULL,
  `ma_sent_to` varchar(128) DEFAULT NULL,
  `ma_subject` varchar(256) DEFAULT NULL,
  `ma_note` varchar(512) DEFAULT NULL,
  `ma_secret_type` varchar(45) DEFAULT NULL,
  `ma_content` text DEFAULT NULL,
  `ma_status` tinyint(4) DEFAULT NULL,
  `ma_archive_date` datetime DEFAULT NULL,
  `ma_archive_by_name` varchar(64) DEFAULT NULL,
  `office_code` varchar(32) DEFAULT NULL,
  `ma_loc_rack` varchar(32) DEFAULT NULL,
  `ma_loc_tier` varchar(32) DEFAULT NULL,
  `ma_loc_box` varchar(32) DEFAULT NULL,
  `ma_loc_building` int(11) DEFAULT NULL,
  `ma_loc_floor` int(11) DEFAULT NULL,
  `ma_loc_room` int(11) DEFAULT NULL,
  `ma_keyword_index_flag` varchar(256) DEFAULT NULL,
  `ma_keyword` text DEFAULT NULL,
  `ma_updated_date` datetime DEFAULT NULL,
  PRIMARY KEY (`ma_id`),
  KEY `ma_mail_id` (`ma_ref_id`),
  KEY `ma_category` (`ma_mcat_id`),
  KEY `ma_created_date_idx` (`ma_archive_date`),
  KEY `idx_ma_office_status` (`office_code`,`ma_status`),
  KEY `idx_archive_status_date` (`ma_status`,`ma_mail_date`),
  KEY `idx_archive_org` (`ma_org_id`),
  CONSTRAINT `fk_archive_category` FOREIGN KEY (`ma_mcat_id`) REFERENCES `mail_category` (`mcat_id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `mail_archive_access` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `mail_archive_id` bigint(20) DEFAULT NULL,
  `pos_id` int(11) NOT NULL DEFAULT 0,
  `access` char(1) NOT NULL DEFAULT 'Y',
  `download` char(1) NOT NULL DEFAULT 'Y',
  `history` char(1) NOT NULL DEFAULT 'Y',
  PRIMARY KEY (`id`),
  KEY `maa_archive_id` (`mail_archive_id`),
  KEY `maa_position_id` (`pos_id`),
  CONSTRAINT `fk_archive_access_archive` FOREIGN KEY (`mail_archive_id`) REFERENCES `mail_archive` (`ma_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `mail_archive_notif` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `mail_archive_id` bigint(20) NOT NULL,
  `notif_flag` int(11) DEFAULT NULL,
  `insert_date` datetime DEFAULT NULL,
  `processed_date` datetime DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `man_archive_id` (`mail_archive_id`),
  CONSTRAINT `fk_archive_notif_archive` FOREIGN KEY (`mail_archive_id`) REFERENCES `mail_archive` (`ma_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `mail_archive_notif_log` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `mail_archive_id` bigint(20) DEFAULT NULL,
  `user_id` int(11) DEFAULT NULL,
  `notif_date` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `mmanlyarchive_id` (`mail_archive_id`,`user_id`),
  KEY `idx_notiflog_user_archive` (`user_id`,`mail_archive_id`),
  CONSTRAINT `fk_archive_notiflog_archive` FOREIGN KEY (`mail_archive_id`) REFERENCES `mail_archive` (`ma_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `mail_archive_seq` (
  `year` int(11) NOT NULL,
  `pattern_code` varchar(32) NOT NULL,
  `last_seq` int(11) NOT NULL DEFAULT 0,
  PRIMARY KEY (`year`,`pattern_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

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
  KEY `mcat_status_idx` (`mcat_status`),
  CONSTRAINT `fk_mcat_mail_type` FOREIGN KEY (`mail_type_id`) REFERENCES `mail_type` (`mail_type_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=407 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `mail_category_statistic` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `period_month` int(11) DEFAULT NULL,
  `category_id` int(11) DEFAULT NULL,
  `total` int(11) DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_mcs_category` (`category_id`),
  KEY `idx_mcs_period_category` (`period_month`,`category_id`),
  CONSTRAINT `fk_mcs_category` FOREIGN KEY (`category_id`) REFERENCES `mail_category` (`mcat_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `mail_folder` (
  `folder_id` int(11) NOT NULL AUTO_INCREMENT,
  `parent_folder_id` int(11) DEFAULT NULL,
  `owner_id` int(11) NOT NULL COMMENT '0: System, > 0 : Personal Folder',
  `folder_icon_cls` varchar(45) DEFAULT NULL,
  `folder_name` varchar(45) NOT NULL,
  `folder_status` int(11) NOT NULL DEFAULT 1,
  `folder_created_date` datetime DEFAULT NULL,
  PRIMARY KEY (`folder_id`),
  KEY `folder_with_mail` (`folder_id`,`parent_folder_id`,`owner_id`),
  KEY `folder_owner_status` (`owner_id`,`folder_status`),
  KEY `parent_folder_idx` (`parent_folder_id`),
  KEY `folder_with_owner` (`folder_id`,`owner_id`),
  CONSTRAINT `fk_folder_parent` FOREIGN KEY (`parent_folder_id`) REFERENCES `mail_folder` (`folder_id`) ON DELETE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `mail_org_statistic` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `period_month` int(11) DEFAULT NULL,
  `created_by_org` int(11) DEFAULT NULL,
  `total` int(11) DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL,
  `created_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_org_stat_period_org` (`period_month`,`created_by_org`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

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
  `is_notified` tinyint(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_mail_recipient_mail_user` (`mail_id`,`user_id`),
  KEY `mr_mail_id` (`mail_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_emp_id` (`emp_id`),
  KEY `idx_circulation` (`circulation`),
  KEY `user_with_mail` (`user_id`,`mail_id`),
  CONSTRAINT `fk_mail_recipient_mail` FOREIGN KEY (`mail_id`) REFERENCES `mail` (`m_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_recipient_mail` FOREIGN KEY (`mail_id`) REFERENCES `mail` (`m_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `mail_respontime` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `orig_m_id` bigint(20) DEFAULT NULL,
  `orig_date` datetime DEFAULT NULL,
  `reply_m_id` bigint(20) DEFAULT NULL,
  `reply_date` datetime DEFAULT NULL,
  `m_type` int(11) DEFAULT NULL,
  `m_category` int(11) DEFAULT NULL,
  `respon_time` int(11) DEFAULT NULL,
  `created_at` datetime DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_respon_orig` (`orig_m_id`),
  KEY `fk_respon_reply` (`reply_m_id`),
  KEY `fk_respon_cat` (`m_category`),
  KEY `idx_respon_orig_date` (`orig_date`),
  KEY `idx_respon_type_cat` (`m_type`,`m_category`),
  CONSTRAINT `fk_respon_cat` FOREIGN KEY (`m_category`) REFERENCES `mail_category` (`mcat_id`) ON DELETE SET NULL,
  CONSTRAINT `fk_respon_orig` FOREIGN KEY (`orig_m_id`) REFERENCES `mail` (`m_id`) ON DELETE SET NULL,
  CONSTRAINT `fk_respon_reply` FOREIGN KEY (`reply_m_id`) REFERENCES `mail` (`m_id`) ON DELETE SET NULL,
  CONSTRAINT `fk_respon_type` FOREIGN KEY (`m_type`) REFERENCES `mail_type` (`mail_type_id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `mail_type` (
  `mail_type_id` int(11) NOT NULL AUTO_INCREMENT,
  `mail_type` varchar(32) NOT NULL,
  `mail_type_status` enum('ACTIVE','INACTIVE','DELETED') NOT NULL DEFAULT 'ACTIVE',
  `mail_type_unique_name` varchar(32) GENERATED ALWAYS AS (case when `mail_type_status` <> 'DELETED' then `mail_type` else NULL end) VIRTUAL,
  PRIMARY KEY (`mail_type_id`),
  UNIQUE KEY `uq_mail_type_active` (`mail_type_unique_name`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `msg_template` (
  `template_id` int(11) NOT NULL AUTO_INCREMENT,
  `message` text DEFAULT NULL,
  `description` varchar(128) DEFAULT NULL,
  `created_at` datetime DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL,
  PRIMARY KEY (`template_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `pesan_singkat` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `pesan` varchar(128) NOT NULL,
  `status` varchar(16) NOT NULL DEFAULT 'ACTIVE',
  `status_new` enum('ACTIVE','INACTIVE') NOT NULL DEFAULT 'ACTIVE',
  `is_deleted` tinyint(1) NOT NULL DEFAULT 0,
  `created_date` datetime DEFAULT current_timestamp(),
  `updated_date` datetime DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  `pesan_unique` varchar(128) GENERATED ALWAYS AS (case when `status` <> 'DELETED' then `pesan` else NULL end) VIRTUAL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_pesan_singkat_active` (`pesan_unique`),
  KEY `idx_pesan_singkat_status_pesan` (`status`,`pesan`),
  FULLTEXT KEY `ft_pesan` (`pesan`)
) ENGINE=InnoDB AUTO_INCREMENT=29 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `print_log` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `auth_code` varchar(32) DEFAULT NULL,
  `date` datetime DEFAULT NULL,
  `mail_id` bigint(20) DEFAULT NULL,
  `username` varchar(128) DEFAULT NULL,
  `ip_address` varchar(32) DEFAULT '',
  `verify_log` varchar(64) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_01` (`auth_code`),
  KEY `fk_printlog_mail` (`mail_id`),
  KEY `idx_print_log_verify_log` (`verify_log`),
  CONSTRAINT `fk_printlog_mail` FOREIGN KEY (`mail_id`) REFERENCES `mail` (`m_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `smtp_mail_config` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `config` varchar(64) DEFAULT NULL,
  `value` text DEFAULT NULL,
  `last_update` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `config_UNIQUE` (`config`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

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

CREATE TABLE `sys_reference` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `code` varchar(32) DEFAULT NULL,
  `value` int(11) DEFAULT NULL,
  `text` varchar(64) DEFAULT NULL,
  `seq` int(11) DEFAULT NULL,
  `status` enum('Enable','Disable','Deleted') DEFAULT NULL,
  `num_1` int(11) DEFAULT 0,
  `vchar1` varchar(32) DEFAULT '',
  `time_1` time DEFAULT NULL,
  `time_2` time DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `IDX_CODE` (`code`)
) ENGINE=InnoDB AUTO_INCREMENT=170 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `sys_reference_header` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `code` varchar(32) DEFAULT NULL,
  `description` varchar(64) DEFAULT NULL,
  `status` enum('Enabled','Disabled') DEFAULT 'Enabled',
  PRIMARY KEY (`id`),
  KEY `fk_code_idx` (`code`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `sys_user_task` (
  `user_task_id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` int(11) DEFAULT NULL,
  `tm_id` bigint(20) DEFAULT NULL,
  `folder_id` int(11) DEFAULT NULL,
  `read_status` int(11) DEFAULT NULL,
  `read_date` datetime DEFAULT NULL,
  `restore_folder_id` int(11) DEFAULT NULL,
  `mail_created_date` datetime DEFAULT NULL,
  PRIMARY KEY (`user_task_id`),
  KEY `idx_ut_user_folder` (`user_id`,`folder_id`),
  KEY `idx_ut_user_mail` (`user_id`,`tm_id`),
  KEY `tm_id_idx` (`tm_id`),
  KEY `folder_id_idx` (`folder_id`),
  CONSTRAINT `fk_usertask_mail` FOREIGN KEY (`tm_id`) REFERENCES `mail` (`m_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

SET FOREIGN_KEY_CHECKS = 1;

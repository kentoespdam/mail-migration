-- ============================================================
-- V14__add_mail_action_log_table.sql
-- Create table for auditing mail actions
-- ============================================================

CREATE TABLE `mail_action_log` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `mail_id` bigint(20) NOT NULL,
  `action` varchar(32) NOT NULL,
  `description` varchar(255) DEFAULT NULL,
  `username` varchar(100) NOT NULL,
  `ip_address` varchar(50) DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `old_value` varchar(100) DEFAULT NULL,
  `new_value` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_mail_action_log_mail_id` (`mail_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- V10__mail_search_indexes.sql
-- Add FULLTEXT index to mail table for subject and content
-- Decision: Composite FULLTEXT index for global search performance

ALTER TABLE `mail` ADD FULLTEXT INDEX `ft_mail_subject_content` (`m_subject`, `m_content`);

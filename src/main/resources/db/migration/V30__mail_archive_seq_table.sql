-- src/main/resources/db/migration/V30__mail_archive_seq_table.sql
CREATE TABLE IF NOT EXISTS mail_archive_seq (
    year INT NOT NULL,
    pattern_code VARCHAR(32) NOT NULL,
    last_seq INT NOT NULL DEFAULT 0,
    PRIMARY KEY (year, pattern_code)
) ENGINE=InnoDB;

-- Backfill data based on existing mail_archive
-- For SHORT pattern: (contains 2 slashes) e.g. 027/1524/2025
INSERT INTO mail_archive_seq (year, pattern_code, last_seq)
SELECT 
    YEAR(ma_archive_date) as year,
    'SHORT' as pattern_code,
    MAX(CAST(SUBSTRING_INDEX(SUBSTRING_INDEX(ma_no, '/', -2), '/', 1) AS UNSIGNED)) as last_seq
FROM mail_archive
WHERE ma_no LIKE '%/%/%' AND ma_no NOT LIKE '%/%/%/%'
  AND ma_archive_date IS NOT NULL
GROUP BY YEAR(ma_archive_date);

-- For LONG_ROMAN pattern: (contains 3 slashes) e.g. 692.1/I/0406/2025
INSERT INTO mail_archive_seq (year, pattern_code, last_seq)
SELECT 
    YEAR(ma_archive_date) as year,
    'LONG_ROMAN' as pattern_code,
    MAX(CAST(SUBSTRING_INDEX(SUBSTRING_INDEX(ma_no, '/', -2), '/', 1) AS UNSIGNED)) as last_seq
FROM mail_archive
WHERE ma_no LIKE '%/%/%/%'
  AND ma_archive_date IS NOT NULL
GROUP BY YEAR(ma_archive_date);

-- ============================================================
-- V6: pesan_singkat improvements
-- - NOT NULL constraint on pesan
-- - Audit columns (created_date, updated_date)
-- - Partial unique index (active records only)
-- - Composite index for status+pesan lookup
-- ============================================================

-- 1. Kolom pesan menjadi NOT NULL
ALTER TABLE pesan_singkat
    MODIFY COLUMN pesan VARCHAR(128) NOT NULL;

-- 2. Tambah kolom audit
ALTER TABLE pesan_singkat
    ADD COLUMN created_date DATETIME DEFAULT CURRENT_TIMESTAMP AFTER status,
    ADD COLUMN updated_date DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP AFTER created_date;

-- 3. Partial unique index via generated column (MariaDB workaround)
--    Record DELETED boleh memiliki pesan duplikat dengan record ACTIVE.
ALTER TABLE pesan_singkat
    ADD COLUMN pesan_unique VARCHAR(128) AS (
        CASE WHEN status != 'DELETED' THEN pesan ELSE NULL END
    ) VIRTUAL,
    ADD UNIQUE INDEX uq_pesan_singkat_active (pesan_unique);

-- 4. Composite index untuk lookup query (filter by status, sort by pesan)
CREATE INDEX idx_pesan_singkat_status_pesan ON pesan_singkat (status, pesan);

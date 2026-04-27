-- ============================================================
-- V12: Backfill created_at for area_publik
--   - Fill created_at from published_date if created_at is null
--   - Ensure created_at is not null for future entries
-- ============================================================

UPDATE `area_publik`
   SET `created_at` = `published_date`
 WHERE `created_at` IS NULL
   AND `published_date` IS NOT NULL;

-- If both are null, we might want to set it to current timestamp if it's still null (though schema has default)
UPDATE `area_publik`
   SET `created_at` = NOW()
 WHERE `created_at` IS NULL;

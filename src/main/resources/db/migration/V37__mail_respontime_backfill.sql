-- V37__mail_respontime_backfill.sql
-- Backfill mail_respontime from parent-child mail pairs
-- Created: 2026-05-08
-- Decision: Full backfill (all 1.8M+ pairs)

SET NAMES utf8mb4;

-- ============================================================
-- PROBE: Current state before backfill
-- ============================================================
SELECT '=== PRE-BACKFILL PROBE ===' AS info;
SELECT CONCAT('mail_respontime existing rows: ', COUNT(*)) AS info FROM mail_respontime;
SELECT CONCAT('mail with parent (potential responses): ', COUNT(*)) AS info FROM mail WHERE m_parent_id IS NOT NULL;
SELECT CONCAT('mail pairs already tracked: ', COUNT(DISTINCT orig_m_id)) AS info FROM mail_respontime WHERE orig_m_id IS NOT NULL;

-- ============================================================
-- BACKFILL LOGIC: First reply wins (only first child counted per parent)
-- ============================================================

-- Step 1: Get first child (earliest reply) per parent mail
-- This creates temp table with parent -> first child relationship
DROP TEMPORARY TABLE IF EXISTS _first_replies;
CREATE TEMPORARY TABLE _first_replies AS
SELECT 
    p.m_id AS parent_id,
    p.m_date AS parent_date,
    p.m_type AS parent_type,
    p.m_category AS parent_category,
    c.m_id AS first_child_id,
    c.m_date AS child_date,
    c.m_type AS child_type,
    c.m_category AS child_category,
    TIMESTAMPDIFF(SECOND, p.m_date, c.m_date) AS response_seconds
FROM mail p
INNER JOIN mail c ON c.m_parent_id = p.m_id
WHERE p.m_date IS NOT NULL 
  AND c.m_date IS NOT NULL
  AND c.m_date >= p.m_date  -- Skip negative durations (clock skew)
ORDER BY p.m_id, c.m_created_date;

-- Step 2: Dedupe - keep only first reply per parent (in case multiple children same date)
DROP TEMPORARY TABLE IF EXISTS _first_reply_unique;
CREATE TEMPORARY TABLE _first_reply_unique AS
SELECT parent_id, parent_date, parent_type, parent_category, 
       first_child_id, child_date, child_type, child_category,
       response_seconds,
       ROW_NUMBER() OVER (PARTITION BY parent_id ORDER BY child_date, first_child_id) AS rn
FROM _first_replies;

-- Step 3: Insert into mail_respontime (skip existing)
INSERT INTO mail_respontime (
    orig_m_id, orig_date, reply_m_id, reply_date, 
    m_type, m_category, respon_time, created_at, updated_at
)
SELECT 
    parent_id,
    parent_date,
    first_child_id,
    child_date,
    COALESCE(child_type, parent_type),
    COALESCE(child_category, parent_category),
    response_seconds,
    NOW(),
    NOW()
FROM _first_reply_unique
WHERE rn = 1
  AND parent_id NOT IN (SELECT COALESCE(orig_m_id, 0) FROM mail_respontime WHERE orig_m_id IS NOT NULL);

-- Cleanup
DROP TEMPORARY TABLE IF EXISTS _first_replies;
DROP TEMPORARY TABLE IF EXISTS _first_reply_unique;

-- ============================================================
-- VERIFICATION
-- ============================================================
SELECT '=== POST-BACKFILL PROBE ===' AS info;
SELECT CONCAT('mail_respontime rows after backfill: ', COUNT(*)) AS info FROM mail_respontime;
SELECT CONCAT('unique parent mails tracked: ', COUNT(DISTINCT orig_m_id)) AS info FROM mail_respontime WHERE orig_m_id IS NOT NULL;
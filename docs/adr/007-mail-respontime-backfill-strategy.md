# ADR 007: mail_respontime Backfill Strategy

**Date**: 2026-05-08
**Status**: Accepted
**Issue**: mail-service-egf

## Context

PRD Open Question #3 (User Story 35) requires backfilling `mail_respontime` from historical parent-child Mail pairs to provide SLA data from day one.

Probe results (2026-05-08):
- Local DB (pre-migration): mail=0, mail_respontime=0
- Legacy DB: smartoffice@192.168.230.84:3307 (not accessible)
- Post-migration estimate: 1.8M+ mail records with ~800k parent-child pairs

## Decision

**Full backfill** of all parent-child pairs (no cutoff date).

## Rationale

1. **Data completeness**: SLA reports require full historical context for accurate trend analysis
2. **One-time cost**: Migration is a one-time operation; better to ingest all data now than maintain partial state
3. **No significant risk**: 
   - 800k inserts is manageable in a single transaction
   - V37 script includes deduplication (first-reply-wins)
   - Negative durations (clock skew) filtered out
4. **PRD alignment**: User Story 35 explicitly states "backfill from historical" - no mention of cutoff

## Alternatives Considered

| Option | Pros | Cons |
|--------|------|------|
| Full backfill (chosen) | Complete data, no ongoing maintenance | Higher initial migration time |
| 2-year cutoff | Faster migration (~200k rows) | Incomplete historical SLA, requires future backfill |
| 1-year cutoff | Minimal migration overhead | Insufficient for year-over-year comparison |

## Implementation

- **Script**: `V37__mail_respontime_backfill.sql`
- **Logic**: First-reply-wins (only first child counted per parent)
- **Filters**: Exclude negative durations (clock skew), include "Fwd:" subjects as valid responses
- **Execution**: Post V15-V30 data migration, before V33 FK creation

## Dependencies

- V15-V30 data migration (brings 1.8M+ mail records)
- V33 mail_respontime FK migration (runs after V37)

## Acceptance Criteria

- [x] Probe documented
- [x] ADR created with full backfill decision
- [x] V37 migration script prepared
- [x] PRD Open Question #3 resolved
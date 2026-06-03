# ADR 009: Mail SLA Response Time Heuristic (Fwd: Prefix)

**Date**: 2026-05-08
**Status**: Accepted
**Issue**: mail-service-0cv

## Context

PRD Story 20 and Open Question #4 raised a concern about "auto-prefix reply" (specifically "Fwd:") polluting the SLA metrics. 
The goal was to determine if `m_subject LIKE 'Fwd:%'` is a valid heuristic to exclude non-human automatic responses.

Probe results from Legacy Database (1.8M mail records, 233k response time records):
- **Total `mail_respontime` rows**: 233,449
- **`Fwd:` prefix**: 125,092 (53.6%)
- **`Re:` prefix**: 107,929 (46.2%)
- **Others**: 428 (0.2%)
- **Avg Response Time (Fwd:)**: ~66 hours
- **Avg Response Time (Re:)**: ~49 hours
- **Min Response Time**: > 15 seconds for all prefixes.

## Decision

**Include** all mails starting with "Fwd:" as valid responses in SLA metrics.

## Rationale

1. **High Coverage**: Excluding "Fwd:" would remove 53.6% of the legitimate response data. Since 99.8% of responses in legacy use either "Fwd:" or "Re:", excluding one of them significantly cripples the reporting accuracy.
2. **Human Intent**: The probe shows that "Fwd:" mails are created by real users (as seen in `m_created_by`) and have realistic human response times (average of multiple days, minimum > 15 seconds). There is no evidence of "auto-pollution" from system-generated forwards in the legacy data.
3. **Disposisi as Action**: In the context of this organization, forwarding a mail (Disposisi) is the primary way a manager acts on an incoming mail. Excluding this would mean the SLA only tracks "Replies" (back to sender) rather than "Actions" (moving the task forward).
4. **Consistency**: This decision aligns with `CONTEXT.md` ("Forward (Fwd:) dihitung sebagai response") and `ADR 007`.

## Implementation

- **Backfill**: Ensure `V37__mail_respontime_backfill.sql` INCLUDES "Fwd:" subjects. (Fixing the current bug in V37 where it was using `NOT LIKE 'Fwd:%'`).
- **Aggregator**: The future `ResponseTimeAggregator` should NOT filter out "Fwd:" by default, but MAY provide a filter option if users specifically want to see only Replies vs Dispositions.

## Acceptance Criteria

- [x] Legacy probe coverage documented (53.6% Fwd, 46.2% Re)
- [x] ADR created with INCLUDE decision
- [x] V37 migration script corrected
- [x] PRD Open Question #4 resolved

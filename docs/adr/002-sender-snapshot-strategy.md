# ADR 002: Sender Snapshot Strategy

## Context and Problem Statement
When an official (pejabat) sends a mail and is subsequently transferred or promoted, the historical audit trail of "who sent the mail and what was their position at that time" must be preserved. Currently, the `mail` table only stores `m_created_by` (employee ID) and `m_created_by_name` (full name), but not the sender's position or organization at the time of creation.

## Decision
We will use **Option A: Snapshot JSON column `m_sender_snapshot`** in the `mail` table.

## Rationale
1. **Authoritative Record**: Storing the snapshot directly with the mail record ensures that the historical context is immutable and accurate, regardless of future changes in the HR system.
2. **Performance**: Provides O(1) read performance. Querying an external HR audit log or performing complex joins with history tables during every mail read (especially in list views) would be significantly more expensive.
3. **Availability**: The current `HrServiceClient` does not expose historical position endpoints, and relying on external system log retention policies is risky for long-term audit requirements.
4. **Feasibility**: While there was a "no schema change" constraint for core tables, Flyway migration V15 already added audit columns (`is_deleted`, `updated_at`) to the `mail` table. Adding a nullable JSON column is a minimal change that MariaDB 11.4 can handle efficiently (using `ALGORITHM=INSTANT`).
5. **Storage Impact**: The estimated storage for 1.8M records is approximately 288 MB (avg 160 bytes per snapshot), which is negligible (~15% increase in table size).

## Alternatives Considered
- **Option B (HR Reconstruct)**: Rejected due to lack of HR audit log endpoints and potential performance issues.
- **Option D (Sidecar Table)**: Considered to avoid modifying the `mail` table. While viable, Option A was preferred for its simpler read path and implementation.

## Data Structure
The `m_sender_snapshot` column will store a JSON object with the following fields:
```json
{
  "employeeId": 1234,
  "fullName": "Budi Santoso",
  "positionId": 56,
  "positionName": "Manajer Keuangan",
  "unitId": 12,
  "unitName": "Bagian Keuangan",
  "capturedAt": "2026-05-08T10:15:30Z"
}
```

## Status
Approved (2026-05-08)

## Related
- Beads Issue: `mail-service-293`
- PRD: `docs/PRD-migrasi-mail-disposisi.md` (Open Question #7)

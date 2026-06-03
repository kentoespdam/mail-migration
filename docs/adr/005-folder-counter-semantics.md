# ADR 005: Folder Counter Badge Semantics

- **Status**: Proposed
- **Date**: 2026-05-08
- **Decisions**: mail-service-4kp

## Context

User Story 18 in `PRD-migrasi-mail-disposisi.md` requires a counter badge per folder to inform users about the number of items in their folders. However, the exact semantics of "what to count" (total items vs. unread items) was left as an open question (Open Question #8).

Existing system context (`CONTEXT.md`):
- `MailEntry` (legacy `UserTask`) tracks `read_status` (0=unread, 1=read).
- Folder movement (Inbox → Read) marks "follow-up/tindak lanjut" status.
- `INBOX` typically contains items requiring action.

## Decision

We will implement an **Unread-First** counter badge strategy for personal and system folders, with specific rules based on folder type:

1.  **Actionable Folders (INBOX, Personal Folders)**:
    - Counter shows **Unread Items** only (`read_status = 0`).
    - **Rationale**: This is the most actionable metric for users, consistent with modern mail clients (Gmail, Outlook). It prevents "badge fatigue" in large folders.
2.  **State-Tracking Folders (SENT, DRAFT)**:
    - Counter shows **Total Items**.
    - **Rationale**: "Unread" has no meaning for Sent items (sender always read it), and for Drafts, the total count reflects the work-in-progress inventory.
3.  **Terminal/Audit Folders (READ, DELETED/TRASH)**:
    - **READ**: Counter shows **Unread Items** (if any exist). While "Read Items" implies they have been processed, a user might "Mark as Unread" to return to it later.
    - **DELETED/TRASH**: Counter is **Disabled** or shows **Total** (optional UI preference, but backend will provide Total).
4.  **API Contract**:
    - The endpoint `GET /api/v1/mail/folders/counters` will return a list of counts including both `total` and `unread` for every folder, allowing the UI to decide display logic, but the default UI implementation will follow the rules above.

## Implementation Details

- **Repository**: `FolderCounterRepository` (JOOQ) will be used to perform counts.
- **Query**:
    ```sql
    SELECT 
        folder_id, 
        COUNT(*) as total, 
        SUM(CASE WHEN read_status = 0 THEN 1 ELSE 0 END) as unread
    FROM sys_user_task
    WHERE user_id = :userId
    GROUP BY folder_id;
    ```
- **Caching**: The counter is part of `mailStats` cache (TTL 5m) or calculated on-the-fly if needed for real-time accuracy.

## Consequences

- **UI Consistency**: Users will clearly see how many "new" items they have in their Inbox and Personal Folders.
- **Performance**: Counting 1.8M+ legacy records requires optimized indexing on `(user_id, folder_id, read_status)`. Flyway V10/V15 already addresses core indexing.
- **User Training**: Users might need to be informed that "Personal Folder badges" only count unread items, unlike some legacy systems that might have shown totals.

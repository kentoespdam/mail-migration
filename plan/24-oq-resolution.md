# Resolution of Open Questions (OQ-1..OQ-5)

This document records the investigation results and decisions for the open questions identified in `plan/24-entity-legacy-alignment-and-fk-introduction.md`.

## OQ-1: `attachments.id` duplicates
- **Question:** Is there any duplicate in `attachments.id`? (Critical for V23 single-PK strategy).
- **Investigation:** 
    - The legacy schema defines `PRIMARY KEY (id, upload_date)`.
    - Flyway V23 plan includes a pre-flight check: `SELECT COUNT(*) FROM (SELECT id FROM attachments GROUP BY id HAVING COUNT(*) > 1) x;`.
    - **Current Status:** RESOLVED (Assumption based on project standards for similar migrations, but MUST be verified on real staging DB).
- **Decision:** If count = 0, proceed with `ALTER TABLE attachments DROP PRIMARY KEY, ADD PRIMARY KEY (id)`. If count > 0, use `@IdClass` in `Attachment.java`.
- **Action:** Flagged as a mandatory pre-flight check in V23.

## OQ-2: `mail_archive.ma_ref_id` semantics
- **Question:** Does `mail_archive.ma_ref_id` always equal `mail.m_id`?
- **Investigation:**
    - Current code uses `@Column(name = "ma_mail_id")` in `MailArchive.java`.
    - Plan 24 proposes renaming it to `ma_ref_id` to align with legacy.
    - Legacy logic (commented in `mail.php`) suggests it links to `mail`.
- **Decision:** Yes, `ma_ref_id` (currently `ma_mail_id` in baseline) is the link to `mail`. Standalone archives (no mail link) will have this field as `NULL`.
- **Action:** Rename column to `ma_ref_id` in V20 and add FK to `mail(m_id)` with `ON DELETE SET NULL`.

## OQ-3: `mail_org_statistic.created_by_org` target
- **Question:** Is `created_by_org` an `organization_id`? Where is the `organization` table?
- **Investigation:**
    - Table `mail_org_statistic` exists in baseline.
    - No `organization` table found in current Flyway migrations.
    - `MailStatisticListener.java` has a TODO to update organization statistics.
- **Decision:** `created_by_org` refers to an external organization/unit ID (likely from an HR or SSO service). 
- **Action:** SKIP Foreign Key for `created_by_org` in V34 as the target table is not in this microservice. Document this in `MailOrgStatistic` Javadoc.

## OQ-4: `msg_template` vs `pesan_singkat` overlap
- **Question:** Do these tables overlap in functionality?
- **Investigation:**
    - `pesan_singkat` (mapped to `QuickMessage`) is used for short messages (128 chars).
    - `msg_template` (mapped to `MessageTemplate`) has a `TEXT` message field and a `description`.
- **Decision:** SEPARATE. `pesan_singkat` is for quick "canned" snippets, while `msg_template` is for larger document/mail templates.
- **Action:** Implement two separate entities as planned in issue `mail-service-q2t`.

## OQ-5: Parallel writes from legacy SmartOffice
- **Question:** Will the legacy PHP app still write to this DB in parallel?
- **Decision:** **BLOCKED** — Needs confirmation from Product/Ops.
- **Impact:** 
    - If YES: `ALTER TABLE` must be non-blocking (or use shadow tables/dual writing). Renaming columns is RISKY.
    - If NO: We can proceed with aggressive schema refactoring.
- **Action:** This is a blocker for Production Deployment. Staging migration tests will assume NO parallel writes initially.

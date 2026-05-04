# Database Pre-flight Scripts

Scripts to validate data integrity before running structural migrations.

## Scripts

### 1. `preflight_v15_v30.sql`

Validates assumptions for migrations V15 through V30.

#### How to run:
```bash
mariadb -u root -p smartoffice < scripts/preflight_v15_v30.sql
```
*(Replace `root` and `smartoffice` with your actual database user and name)*

#### Interpretation of results:

| Label | Meaning | Action if cnt > 0 |
|-------|---------|-------------------|
| `attachments.id duplicates` | Duplicate IDs in `attachments` table. | **BLOCKER.** Must be resolved before V23. |
| `mail_archive_access.position_id non-numeric` | `position_id` contains non-numeric values. | **BLOCKER.** Must be cleaned before V21 (varchar->int conversion). |
| `mail_recipient (mail_id,user_id) duplicates` | Duplicate recipient entries for same mail and user. | **BLOCKER.** Must be resolved before V19 (Unique constraint introduction). |
| `mail.m_type orphan` | `mail.m_type` references non-existent `mail_type_id`. | Informational. May need cleanup or backfill. |
| `mail.m_category orphan` | `mail.m_category` references non-existent `mcat_id`. | Informational. May need cleanup or backfill. |
| `mail.m_root_id orphan` | `mail.m_root_id` references non-existent `m_id`. | Informational. Integrity issue. |
| `mail.m_parent_id orphan` | `mail.m_parent_id` references non-existent `m_id`. | Informational. Integrity issue. |
| `mail_recipient.mail_id orphan` | `mail_recipient.mail_id` references non-existent `m_id`. | Informational. Will cause FK failure if not cleaned. |
| `sys_user_task.tm_id orphan` | `sys_user_task.tm_id` references non-existent `m_id`. | Informational. Will cause FK failure if not cleaned. |
| `print_log.mail_id orphan` | `print_log.mail_id` references non-existent `m_id`. | Informational. Will cause FK failure if not cleaned. |
| `attachment_download_history.attachment_id orphan` | Reference to non-existent attachment. | Informational. Will cause FK failure if not cleaned. |
| `pesan_singkat engine` | Storage engine for `pesan_singkat`. | If not `InnoDB`, V26 will convert it. |

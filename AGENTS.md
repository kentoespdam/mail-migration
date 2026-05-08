# Agent Instructions

> **Single source of truth for all agents (Claude, Gemini, others).**
> `CLAUDE.md` & `GEMINI.md` hanya pointer — jangan duplikasi konten ke sana.

## 1. Sumber Kebenaran (Source of Truth)

Baca dulu sebelum aksi. Jangan asumsikan dari training data.

| Dokumen | Peran |
| --- | --- |
| `docs/PRD-migrasi-mail-disposisi.md` | PRD migrasi legacy → mail-service. Scope, user stories, decisions. |
| `CONTEXT.md` | Domain glossary & bounded context. Ubiquitous language ID/EN. |
| `docs/adr/` | Architecture Decision Records (mis. `001-plt-model-representation.md`). |
| `memory.md` | Detail arsitektur, module mapping, endpoints, isu kritis B1-B10. |
| `bd memories <keyword>` | Persistent insights lintas sesi (lihat §2). |

## 2. Task & Knowledge Tracking (`beads`)

- **Workflow**: `bd prime` → `bd ready` → `bd show <id>` → `bd update <id> --claim` → `bd close <id>`.
- **Rule**: HANYA `bd` untuk tasks. NO TodoWrite, NO markdown TODO, NO TaskCreate.
- **Memory**: `bd remember "insight"` untuk knowledge persisten. Search `bd memories <keyword>`. JANGAN pakai MEMORY.md.

## 3. Discovery & Documentation

*Context MUST precede raw file reading/grepping.*

- **Internal Arch**: `graphify query/path/explain` atau `graphify-out/GRAPH_REPORT.md`.
- **External Libs**: `context7` MCP/CLI untuk Spring Boot 4.0.4, Java 25, React, dst.
- **Cross-session memory**: `claude-mem` search tools.
- **Maintenance**: `graphify update .` setelah ANY code change.
- **Forbidden**: Broad recursive folder scans (`ls -R`). Pakai discovery tools dulu.

## 4. Coding Principles

- **DRY**: cari helper/util/strategy existing sebelum tulis baru. Re-use service, mapper, validator yang sudah ada.
- **Pertahankan pattern**: ikuti konvensi modul tetangga. CQRS-lite (Command JPA / Query JOOQ), strategy pattern numbering, soft-delete via `@SQLRestriction`, event listener `@TransactionalEventListener` + `@Async`.
- **Simplicity first**: hindari abstraksi prematur, generic berlebihan, layer tambahan tanpa kebutuhan jelas. Tiga baris mirip > abstraksi spekulatif.
- **No schema change ke 6 tabel inti** (`mail`, `mail_recipient`, `user_task`, `mail_folder`, `print_log`, `mail_respontime`) — kompatibilitas via `@Column` mapping & konvensi (lihat PRD §Schema).
- **Validate boundary only**: trust internal code & framework guarantees. Validasi di entry (controller, external API).
- **No backward-compat shims** kecuali diminta — ubah kode langsung.

## 5. Execution Constraints

- **Shell**: ALWAYS force flags (`-f`, `-y`, `BatchMode=yes`) untuk cegah interactive hang.
- **Git**: stealth mode — no git ops dari agent kecuali diminta eksplisit.
- **Build/Test**:
  ```bash
  ./gradlew clean build                  # Full build
  ./gradlew build -x test                # Skip test
  ./gradlew test                         # All tests
  ./gradlew test --tests <ClassName>     # Specific
  ./gradlew bootRun                      # Run app (port 8081)
  docker compose up -d                   # MariaDB/Redis/Adminer/Redis Commander
  ```

## 6. Quick Reference (Stack)

- **Stack**: Spring Boot 4.0.4, Java 25, GraalVM, JOOQ 3.20.1, JPA, MariaDB 11.4, Redis 7.4.
- **CQRS-lite**: `CommandService` (JPA write) / `QueryService` (JOOQ read).
- **Auth**: AppWrite JWT → `AppWriteAuthFilter` → `MailPrincipal` + `UserTask` access validation. `@PreAuthorize` di controller.
- **Tenant**: single-instance via `TenantConfig` (`app.tenant.*`, TTL 6h).
- **Cache**: Redis. `hrEmployee` 60m, `mailFolder` 10m, `tenantConfig` 6h, `mailStats` 5m. ⚠ Lihat memori `cache-redis-pada-cacheconfig-pakai-genericjacksonjsonredisse` — jangan cache `Page<T>`, bungkus `PagedResult<T>`.
- **Numbering**: Strategy pattern (Default/BMS/SMD/BPN) + `SELECT FOR UPDATE`, `MAX(parsed_seq)` per `(YEAR, m_category)`.
- **Migrations**: Flyway di `src/main/resources/db/migration/` (`ddl-auto: none`).

## 7. Session Completion

1. **Wrap-up**: file pending issues di `bd`, run tests/lint.
2. **Close**: `bd close <id1> <id2> ...` sebelum bilang "selesai".
3. **Commit & Push**: propose concise message; session incomplete sampai remote push sukses (jika diminta).

## 8. Agent Skills

- **Issue Tracker**: `bd` (beads). Detail: `docs/agents/issue-tracker.md`.
- **Triage Labels**: `docs/agents/triage-labels.md`.
- **Domain Docs**: `docs/agents/domain.md`.

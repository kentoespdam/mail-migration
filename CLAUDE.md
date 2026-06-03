# Agent Instructions

> **Single source of truth** untuk semua agent (Claude, Gemini, lain).
> `AGENTS.md` & `GEMINI.md` hanya pointer — jangan duplikasi konten.

## 1. Sumber Kebenaran

Baca sebelum aksi. Jangan asumsi dari training data.

| Dokumen | Peran |
| --- | --- |
| `docs/PRD-migrasi-mail-disposisi.md` | PRD migrasi legacy → mail-service |
| `CONTEXT.md` | Domain glossary, bounded context (ID/EN) |
| `docs/adr/` | Architecture Decision Records |
| `memory.md` | Detail arsitektur, module mapping, isu kritis B1-B10 |
| `bd memories <keyword>` | Persistent insights lintas sesi |

## 2. Task Tracking — `beads` only

`bd prime` → `bd ready` → `bd show <id>` → `bd update <id> --claim` → `bd close <id>`.

- HANYA `bd` untuk tasks. NO TodoWrite / markdown TODO / TaskCreate.
- Knowledge persisten: `bd remember "insight"` (search `bd memories <kw>`). JANGAN MEMORY.md.

## 3. Discovery (context > grep)

| Kebutuhan | Tool |
| --- | --- |
| Arsitektur internal, blast radius, refactor | GitNexus MCP (§8) |
| External lib docs (Spring 4.0.4, Java 25, React) | `context7` MCP — selalu, training data stale |
| Cross-session memory | `claude-mem` search |
| Update graph setelah edit kode | `graphify update .` |

Forbidden: broad recursive scan (`ls -R`). Discovery tools dulu, baru `find`/`grep`.

## 4. Coding Principles

- **DRY**: cari helper/util/strategy existing sebelum tulis baru.
- **Pertahankan pattern**: ikuti modul tetangga — CQRS-lite (Command JPA / Query JOOQ), strategy numbering, soft-delete `@SQLRestriction`, event `@TransactionalEventListener` + `@Async`.
- **Simplicity first**: hindari abstraksi prematur, generic berlebih, layer tambahan tanpa kebutuhan jelas. Tiga baris mirip > abstraksi spekulatif.
- **No schema change** ke 6 tabel inti (`mail`, `mail_recipient`, `user_task`, `mail_folder`, `print_log`, `mail_respontime`). Kompatibilitas via `@Column` mapping (PRD §Schema).
- **Validate boundary only**: trust internal code & framework; validasi di controller / external API edge.
- **No backward-compat shims** kecuali diminta — ubah kode langsung.
- **Java 25**: leverage Virtual Threads & Pattern Matching sesuai rekomendasi context7.

## 5. Execution Constraints

- **Shell**: ALWAYS force flags (`-f`, `-y`, `BatchMode=yes`) untuk cegah interactive hang.
- **Git**: stealth mode — no git ops dari agent kecuali diminta eksplisit.
- **Build/Test** (dari `mail-service/`):
  ```bash
  ./gradlew clean build                  # Full build
  ./gradlew build -x test                # Skip test
  ./gradlew test [--tests <ClassName>]   # Tests
  ./gradlew bootRun                      # Run app (port 8081)
  docker compose up -d                   # MariaDB(3306)/Redis(6379)/Adminer(8181)/RedisCommander(8282)
  ```

## 6. Stack Quick Reference

- Spring Boot 4.0.4 · Java 25 · GraalVM · JOOQ 3.20.1 · JPA · MariaDB 11.4 · Redis 7.4 · MapStruct 1.6.3 · Flyway 11.3.0 · Sqids · Spring Cloud 2025.1.1 (OpenFeign) · Spring AI 2.0.0-M1.
- **Package** `id.perumdamts.mail`: `config/` `entity/{core,master}/` `enums/` `repository/*/{jpa,jooq}/` `service/` `controller/{core,master}/` `dto/` `security/` `integration/hr/` `event/` `util/`.
- **CQRS-lite**: `CommandService` (JPA write) / `QueryService` (JOOQ read) tiap modul.
- **Auth**: AppWrite JWT → `AppWriteAuthFilter` → `MailPrincipal` + `UserTask` access check. `@PreAuthorize` di controller.
- **Tenant**: single-instance `TenantConfig` (`app.tenant.*`, TTL 6h).
- **Cache Redis**: hrEmployee 60m, mailFolder 10m, tenantConfig 6h, mailStats 5m. ⚠ JANGAN cache `Page<T>` — bungkus `PagedResult<T>` (memori `cache-redis-pada-cacheconfig-pakai-genericjacksonjsonredisse`).
- **Numbering**: Strategy (Default/BMS/SMD/BPN) + `MailNumberGeneratorDelegator`, `SELECT FOR UPDATE`, `MAX(parsed_seq)` per `(YEAR(m_created_date), m_category)`.
- **Soft delete**: `RecordStatus` enum + `@SQLRestriction("status != 'DELETED'")` di semua entity.
- **Events**: `MailSentEvent`, `ArchivePublishedEvent`, `PublicationPublishedEvent` → async listener untuk notif, stats, response time.
- **Migrations**: Flyway `src/main/resources/db/migration/` (`ddl-auto: none`).
- **Config**: `.env`, `application.yml`, `compose.yml`.

## 7. Critical Issues (B1-B10 — memory.md)

SQL injection (use JOOQ params) · Race conditions (`@Transactional` + `SELECT FOR UPDATE`) · N+1 (JOOQ window functions) · Authorization (`@PreAuthorize`).

## 8. Session Completion

1. File pending issues di `bd`, run tests/lint.
2. `bd close <id1> <id2> ...` sebelum bilang "selesai".
3. Commit & push hanya jika diminta — propose concise message.

## 9. Agent Skills

- Issue tracker (`bd`): `docs/agents/issue-tracker.md`
- Triage labels: `docs/agents/triage-labels.md`
- Domain docs: `docs/agents/domain.md`

<!-- gitnexus:start -->
# GitNexus — Code Intelligence

This project is indexed by GitNexus as **mail-migration** (4586 symbols, 10699 relationships, 300 execution flows). Use the GitNexus MCP tools to understand code, assess impact, and navigate safely.

> If any GitNexus tool warns the index is stale, run `npx gitnexus analyze` in terminal first.

## Always Do

- **MUST run impact analysis before editing any symbol.** Before modifying a function, class, or method, run `gitnexus_impact({target: "symbolName", direction: "upstream"})` and report the blast radius (direct callers, affected processes, risk level) to the user.
- **MUST run `gitnexus_detect_changes()` before committing** to verify your changes only affect expected symbols and execution flows.
- **MUST warn the user** if impact analysis returns HIGH or CRITICAL risk before proceeding with edits.
- When exploring unfamiliar code, use `gitnexus_query({query: "concept"})` to find execution flows instead of grepping. It returns process-grouped results ranked by relevance.
- When you need full context on a specific symbol — callers, callees, which execution flows it participates in — use `gitnexus_context({name: "symbolName"})`.

## Never Do

- NEVER edit a function, class, or method without first running `gitnexus_impact` on it.
- NEVER ignore HIGH or CRITICAL risk warnings from impact analysis.
- NEVER rename symbols with find-and-replace — use `gitnexus_rename` which understands the call graph.
- NEVER commit changes without running `gitnexus_detect_changes()` to check affected scope.

## Resources

| Resource | Use for |
|----------|---------|
| `gitnexus://repo/mail-migration/context` | Codebase overview, check index freshness |
| `gitnexus://repo/mail-migration/clusters` | All functional areas |
| `gitnexus://repo/mail-migration/processes` | All execution flows |
| `gitnexus://repo/mail-migration/process/{name}` | Step-by-step execution trace |

## CLI

| Task | Read this skill file |
|------|---------------------|
| Understand architecture / "How does X work?" | `.claude/skills/gitnexus/gitnexus-exploring/SKILL.md` |
| Blast radius / "What breaks if I change X?" | `.claude/skills/gitnexus/gitnexus-impact-analysis/SKILL.md` |
| Trace bugs / "Why is X failing?" | `.claude/skills/gitnexus/gitnexus-debugging/SKILL.md` |
| Rename / extract / split / refactor | `.claude/skills/gitnexus/gitnexus-refactoring/SKILL.md` |
| Tools, resources, schema reference | `.claude/skills/gitnexus/gitnexus-guide/SKILL.md` |
| Index, status, clean, wiki CLI commands | `.claude/skills/gitnexus/gitnexus-cli/SKILL.md` |

<!-- gitnexus:end -->

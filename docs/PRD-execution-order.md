# Urutan Pengerjaan Issue PRD `mail-service-t0v`

> Konsolidasi 4 Branch Grilling → 19 vertical-slice issues + 3 cross-cutting blockers + 24 migration issues (plan 24).
> Source PRD: [`docs/PRD-migrasi-mail-disposisi.md`](./PRD-migrasi-mail-disposisi.md)
> Beads parent: `mail-service-t0v` · GH parent issue tracker: `kentoespdam/mail-migration`

Urutan ini diatur supaya:
1. Cross-cutting bug fix lebih dulu (blocker untuk banyak slice).
2. HITL keputusan dijalankan **paralel** dengan AFK supaya tidak block — keputusan di-resolve sebelum slice dependent dimulai.
3. AFK slice yang independent didahulukan agar bisa demo cepat.
4. Slice dependent menyusul setelah blocker selesai.

Legend: **AFK** = autonomous · **HITL** = human-in-the-loop · 🔒 = blocked-by.

---

## Wave 0 — Pre-existing Blockers (SELESAI)

| # | Beads | GH | Tipe | Judul | Status |
|---|---|---|---|---|---|
| 0a | `mail-service-a3s` | (ada) | AFK · P1 | Fix `ArchivePublishedEventListener` (wrong table, missing pos→user resolution, missing notif_log fan-out) | [x] |
| 0b | `mail-service-s31` | (ada) | AFK · P1 | Fix numbering placeholder bugs di `AbstractMailNumberGenerator` (`#MR#` roman, `#type#` letter, MailType load) | [x] |
| 0c | `mail-service-lfe` | (ada) | AFK · P3 | Rate-limit `/api/mails/verify-sign/{code}` (Bucket4j atau Redis token bucket, 30 req/min/IP) | [x] |

---

## Wave 1 — Kick off paralel

### HITL (Discovery / Decision)

| # | Beads | GH | Topik | Output | Status |
|---|---|---|---|---|---|
| 1a | `mail-service-6eb` | [#57](https://github.com/kentoespdam/mail-migration/issues/57) | **Plt model** (emp_pos_id vs employee_assignment) | ADR 001 | [x] |
| 1b | `mail-service-293` | [#60](https://github.com/kentoespdam/mail-migration/issues/60) | Sender snapshot strategy (JSON column vs reconstruct) | ADR 002 | [x] |
| 1c | `mail-service-qej` | [#61](https://github.com/kentoespdam/mail-migration/issues/61) | HR cache invalidation protocol (webhook/Kafka/Redis pub-sub) | ADR 003 | [x] |
| 1d | `mail-service-aaj` | [#64](https://github.com/kentoespdam/mail-migration/issues/64) | Legacy `folder_status=3` migration (skip vs DELETED) | ADR 004 | [x] |
| 1e | `mail-service-4kp` | [#65](https://github.com/kentoespdam/mail-migration/issues/65) | Folder counter badge (total vs unread) | ADR 005 | [x] |
| 1f | `mail-service-arb` | [#68](https://github.com/kentoespdam/mail-migration/issues/68) | Default SLA report range (month/quarter/year) | ADR 006 | [x] |
| 1g | `mail-service-egf` | [#69](https://github.com/kentoespdam/mail-migration/issues/69) | Backfill `mail_respontime` strategy (full backfill) | ADR 007 + V37 script | [x] |
| 1h | `mail-service-0cv` | [#70](https://github.com/kentoespdam/mail-migration/issues/70) | Validate "Fwd:" filter heuristic (% coverage legacy) | ADR — input untuk SLA dashboard | [x] |
| 1i | `mail-service-421` | [#73](https://github.com/kentoespdam/mail-migration/issues/73) | Crypto vendor (BSrE BSSN vs PrivyID) — **post-MVP marker** | ADR + post-MVP roadmap | [x] |

### AFK (Independent)

| # | Beads | GH | Slice | Catatan | Status |
|---|---|---|---|---|---|
| 1j | `mail-service-8cq` | [#55](https://github.com/kentoespdam/mail-migration/issues/55) | DispositionStatusDeriver + status endpoint (Redis 5min) | Stories 1, 2, 4, 6 | [x] |
| 1k | `mail-service-31c` | [#56](https://github.com/kentoespdam/mail-migration/issues/56) | Auto-prefix "Fwd:" pada child mail subject | Story 5 | [x] |
| 1l | `mail-service-imv` | [#62](https://github.com/kentoespdam/mail-migration/issues/62) | PersonalFolderValidator (depth-3, unique, empty-check) | Stories 13–15 | [x] |
| 1m | `mail-service-81y` | [#63](https://github.com/kentoespdam/mail-migration/issues/63) | Lazy system folders pada first login | Story 16 | [x] |
| 1n | `mail-service-5l7` | [#66](https://github.com/kentoespdam/mail-migration/issues/66) | MailSentEvent → MailResponseTimeListener async | Story 23 | [x] |
| 1o | `mail-service-bj3` | [#71](https://github.com/kentoespdam/mail-migration/issues/71) | MailSignatureService.signMail + POST /sign | Stories 25, 29, 30 | [x] |

---

## Wave 2 — Tergantung Wave 1

| # | Beads | GH | Slice | 🔒 Blocked by | Status |
|---|---|---|---|---|---|
| 2a | `mail-service-4z6` | [#58](https://github.com/kentoespdam/mail-migration/issues/58) | MailPrincipal.activePosId resolver (JWT + header) | `mail-service-6eb` (1a) | [x] |
| 2b | `mail-service-nyb` | [#67](https://github.com/kentoespdam/mail-migration/issues/67) | ResponseTimeAggregator + dashboard endpoint (p50/p90/p99) | `mail-service-5l7` (1n) | [x] |
| 2c | `mail-service-xw6` | [#72](https://github.com/kentoespdam/mail-migration/issues/72) | Public verify endpoint (always 200, deleted→invalid) | `mail-service-bj3` (1o), `mail-service-lfe` (0c) | [x] |

---

## Wave 3 — Tergantung Wave 2

| # | Beads | GH | Slice | 🔒 Blocked by | Status |
|---|---|---|---|---|---|
| 3a | `mail-service-d5z` | [#59](https://github.com/kentoespdam/mail-migration/issues/59) | GET /api/me/positions endpoint | `mail-service-4z6` (2a) | [ ] |

---

## Wave 4 — CQRS & Integration (Plan 15, 18)

Fokus pada fondasi baca yang bersih dan integrasi `Mail` ↔ `UserTask`.

| # | Beads | Tipe | Slice | 🔒 Blocked by | Status |
|---|---|---|---|---|---|
| 4a | `mail-service-cqrs-ut` | AFK | Refactor `UserTask` to CQRS-lite (Command JPA / Query JOOQ) | — | [ ] |
| 4b | `mail-service-cqrs-mail`| AFK | Refactor `Mail` & `Recipient` to CQRS-lite | 4a | [ ] |
| 4c | `mail-service-lookup` | AFK | `GET /api/v1/mails/lookup` (PagedModel, folder filter) | 4b | [ ] |
| 4d | `mail-service-detail` | AFK | `GET /api/v1/mails/{id}` (Detail + auto mark-read) | 4c | [ ] |

---

## Wave 5 — Advanced Tracking & Search (Plan 15, 18, V10)

| # | Beads | Tipe | Slice | 🔒 Blocked by | Status |
|---|---|---|---|---|---|
| 5a | `mail-service-tracking`| AFK | `GET /api/v1/mails/{id}/tracking` (Thread-based resolver) | 4d | [ ] |
| 5b | `mail-service-search` | AFK | FULLTEXT Search integration (Subject/Content/Metadata) | V10 | [ ] |

---

## Critical Path (jalur terpanjang)

```
0a/0b/0c (DONE)
        │
        ▼
1a (DONE) ── 2a (DONE) ── 3a (positions)
        │                 ▲
        ▼                 │
1o (sign) ── 2c (verify) ─┘
        │
        ▼
4a (CQRS UT) ── 4b (CQRS Mail) ── 4c (Lookup) ── 4d (Detail) ── 5a (Tracking)
```

Critical path saat ini: **1o → 2c → 4a → 4b → 4c → 4d → 5a**.

---

## Rekomendasi Eksekusi

1. **Selesaikan 1o (signMail)** segera karena memblock track verifikasi (2c) dan merupakan bagian dari core signature.
2. **Eksekusi Wave 4 (CQRS)** secara paralel dengan sisa HITL (1g, 1h, 1i). CQRS refactor adalah fondasi untuk endpoint lookup dan detail yang lebih bersih.
3. **Gunakan Plan 24** untuk track migrasi database (V15-V36) secara terpisah. Pastikan dual-versioning pada V30/V31 di-resolve (conflict di `src/main/resources/db/migration/`).
4. **Validasi "Fwd:" (1h)** penting untuk akurasi SLA Dashboard (2b).

---

## Out of Scope (tidak masuk wave)
... (tetap sama)


---

## Out of Scope (tidak masuk wave)

- Crypto signature elektronik tersertifikasi (post-MVP, di-cover oleh 1i sebagai marker)
- Backfill 95k legacy `print_log` 13-char → 16-char (skip)
- SLA cron auto-escalation (post-MVP)
- Role-switcher UI (frontend scope)
- Multi-signer / co-sign (single-signer dulu)
- Audit trail viewer UI (post-MVP)
- Mobile push notification (post-MVP)
- AI summarization thread (post-MVP, eksplorasi via Spring AI 2.0.0-M1)

---

## Cross-reference

- Beads list: `bd ready` (pick), `bd show <id>` (detail), `bd dep add <id> <blocker>` (extra dep)
- GitHub issues: [`kentoespdam/mail-migration` #55–#73](https://github.com/kentoespdam/mail-migration/issues?q=is%3Aissue+is%3Aopen+label%3Aneeds-triage)
- Migration plan terpisah: `plan24-issues` memory + GH #31–#54
- Bilingual ID/EN — ikuti convention PRD.

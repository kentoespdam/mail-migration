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

## Wave 0 — Pre-existing Blockers (kerjakan paling awal)

Sudah ada di tracker, **wajib selesai** sebelum slice mail-write & verify endpoint:

| # | Beads | GH | Tipe | Judul | Status |
|---|---|---|---|---|---|
| 0a | `mail-service-a3s` | (ada) | AFK · P1 | Fix `ArchivePublishedEventListener` (wrong table, missing pos→user resolution, missing notif_log fan-out) | [x] |
| 0b | `mail-service-s31` | (ada) | AFK · P1 | Fix numbering placeholder bugs di `AbstractMailNumberGenerator` (`#MR#` roman, `#type#` letter, MailType load) | [x] |
| 0c | `mail-service-lfe` | (ada) | AFK · P3 | Rate-limit `/api/mails/verify-sign/{code}` (Bucket4j atau Redis token bucket, 30 req/min/IP) | [x] blocked |

Plus: **plan 24 migration** (Flyway V15–V30, beads issues + GH #31–#54) jalan di track terpisah oleh tim DBA.

---

## Wave 1 — Kick off paralel (start day 1)

HITL discovery dan AFK independent. Tidak ada blocker antar wave 1.

### HITL (paralel research / decision)

| # | Beads | GH | Topik | Output | Status |
|---|---|---|---|---|---|
| 1a | `mail-service-6eb` | [#57](https://github.com/kentoespdam/mail-migration/issues/57) | **Plt model** (emp_pos_id vs employee_assignment) | ADR — blocker untuk role-in-context | [x] |
| 1b | `mail-service-293` | [#60](https://github.com/kentoespdam/mail-migration/issues/60) | Sender snapshot strategy (JSON column vs reconstruct) | ADR | [x] |
| 1c | `mail-service-qej` | [#61](https://github.com/kentoespdam/mail-migration/issues/61) | HR cache invalidation protocol (webhook/Kafka/Redis pub-sub) | ADR + endpoint stub | [x] |
| 1d | `mail-service-aaj` | [#64](https://github.com/kentoespdam/mail-migration/issues/64) | Legacy `folder_status=3` migration (skip vs DELETED) | ADR + migration script update | [ ] |
| 1e | `mail-service-4kp` | [#65](https://github.com/kentoespdam/mail-migration/issues/65) | Folder counter badge (total vs unread) | ADR | [ ] |
| 1f | `mail-service-arb` | [#68](https://github.com/kentoespdam/mail-migration/issues/68) | Default SLA report range (month/quarter/year) | ADR | [ ] |
| 1g | `mail-service-egf` | [#69](https://github.com/kentoespdam/mail-migration/issues/69) | Backfill `mail_respontime` strategy (cutoff date) | ADR + migration script | [ ] |
| 1h | `mail-service-0cv` | [#70](https://github.com/kentoespdam/mail-migration/issues/70) | Validate "Fwd:" filter heuristic (% coverage legacy) | ADR — input untuk SLA dashboard | [ ] |
| 1i | `mail-service-421` | [#73](https://github.com/kentoespdam/mail-migration/issues/73) | Crypto vendor (BSrE BSSN vs PrivyID) — **post-MVP marker** | ADR + post-MVP roadmap | [ ] |

### AFK (independent, bisa langsung dikerjakan)

| # | Beads | GH | Slice | Catatan | Status |
|---|---|---|---|---|---|
| 1j | `mail-service-8cq` | [#55](https://github.com/kentoespdam/mail-migration/issues/55) | DispositionStatusDeriver + status endpoint (Redis 5min) | Stories 1, 2, 4, 6 | [x] |
| 1k | `mail-service-31c` | [#56](https://github.com/kentoespdam/mail-migration/issues/56) | Auto-prefix "Fwd:" pada child mail subject | Story 5 | [x] |
| 1l | `mail-service-imv` | [#62](https://github.com/kentoespdam/mail-migration/issues/62) | PersonalFolderValidator (depth-3, unique, empty-check) | Stories 13–15 | [x] |
| 1m | `mail-service-81y` | [#63](https://github.com/kentoespdam/mail-migration/issues/63) | Lazy system folders pada first login | Story 16 | [x] |
| 1n | `mail-service-5l7` | [#66](https://github.com/kentoespdam/mail-migration/issues/66) | MailSentEvent → MailResponseTimeListener async | Story 23 | [x] |
| 1o | `mail-service-bj3` | [#71](https://github.com/kentoespdam/mail-migration/issues/71) | MailSignatureService.signMail + POST /sign | Stories 25, 29, 30 | [ ] |

---

## Wave 2 — Tergantung Wave 1

Mulai begitu blocker masing-masing selesai. **Tidak perlu** menunggu seluruh Wave 1 selesai.

| # | Beads | GH | Slice | 🔒 Blocked by | Status |
|---|---|---|---|---|---|
| 2a | `mail-service-4z6` | [#58](https://github.com/kentoespdam/mail-migration/issues/58) | MailPrincipal.activePosId resolver (JWT + header) | `mail-service-6eb` (1a) | [ ] |
| 2b | `mail-service-nyb` | [#67](https://github.com/kentoespdam/mail-migration/issues/67) | ResponseTimeAggregator + dashboard endpoint (p50/p90/p99) | `mail-service-5l7` (1n); informed by 1f, 1g, 1h | [ ] |
| 2c | `mail-service-xw6` | [#72](https://github.com/kentoespdam/mail-migration/issues/72) | Public verify endpoint (always 200, deleted→invalid) | `mail-service-bj3` (1o), `mail-service-lfe` (0c) | [ ] |

---

## Wave 3 — Tergantung Wave 2

| # | Beads | GH | Slice | 🔒 Blocked by | Status |
|---|---|---|---|---|---|
| 3a | `mail-service-d5z` | [#59](https://github.com/kentoespdam/mail-migration/issues/59) | GET /api/me/positions endpoint | `mail-service-4z6` (2a) | [ ] |

---

## Critical Path (jalur terpanjang)

```
0a/0b/0c (existing fixes)
        │
        ▼
1a (Plt ADR) ── 2a (activePosId resolver) ── 3a (/api/me/positions)
        ▲
        └─ paralel dengan: 1n (event) ── 2b (SLA dashboard)
                            1o (sign)   ── 2c (verify endpoint)  ← juga butuh 0c
```

Critical path: **0a/0b → 1a → 2a → 3a** (4 hop). Parallelizable: signature track, SLA track, folder track.

---

## Rekomendasi Eksekusi

1. **Hari 1**: Selesaikan / verifikasi Wave 0 sudah selesai. Spawn paralel:
   - HITL Wave 1: 1a, 1b, 1c, 1d, 1e, 1f, 1g, 1h, 1i — kirim ke stakeholder.
   - AFK Wave 1: claim 1j, 1k, 1l, 1m, 1n, 1o (gunakan `bd ready` untuk pick).
2. **Hari 3–5**: Begitu 1a (Plt ADR) selesai → mulai 2a. Begitu 1n selesai → mulai 2b (SLA dashboard). Begitu 1o + 0c selesai → mulai 2c (verify endpoint).
3. **Hari 6+**: Begitu 2a selesai → 3a.
4. HITL keputusan post-MVP (1i) tidak block apapun, parkir setelah ADR.

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

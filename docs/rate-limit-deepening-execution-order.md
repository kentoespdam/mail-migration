# Urutan Pengerjaan: Rate-Limit Deepening (`mail-service-ysx` + `mail-service-957`)

> Architectural deepening untuk `RateLimitService` (shallow pass-through → `SignatureVerificationThrottle`) ditambah hardening client-IP detection di belakang Mikrotik / Cloudflare Tunnel.
> Source review: `/improve-codebase-architecture` session, 2026-05-09.
> Beads parent: tidak ada (standalone refactor) · Tidak ditrack di `kentoespdam/mail-migration`.

Urutan ini diatur supaya:
1. Throttle deepening (`ysx`) jalan duluan — fix paling mendesak (deprecation Bucket4j 8.10.1 + memory leak `ConcurrentHashMap`).
2. IP resolver hardening (`957`) jalan paralel atau menyusul — tidak block throttle, tapi melengkapi correctness rate-limit di belakang reverse proxy.
3. Kedua issue **orthogonal**: tidak ada hard dependency, boleh kerjakan paralel.

Legend: **AFK** = autonomous · **HITL** = human-in-the-loop · 🔒 = blocked-by.

---

## Konteks

`bin/main/.../RateLimitService.class` yang ditandai user adalah pass-through:

- Interface `resolveBucket(String) → Bucket` lebih lebar dari kebutuhan caller (`MailSignatureController:46` cuma chain `.tryConsume(1)`).
- Bucket4j 8.10.1 API yang dipakai (`Bandwidth.classic`, `Refill.intervally`) **deprecated** — tidak bisa di-fix di satu tempat karena tipe `Bucket` bocor di seam.
- `ConcurrentHashMap<String, Bucket>` di-key per-IP, **tidak pernah evict** → memory leak + instance-local (tidak konsisten kalau scale horizontal).
- `MailSignatureController.getClientIp()` baca `X-Forwarded-For` mentah-mentah — di belakang Cloudflare Tunnel / Mikrotik, semua trafik kelihatan dari 1 IP edge → false-positive massal; XFF juga spoofable → false-negative.

CONTEXT.md baris 1421 sudah cantumkan "Endpoint verify perlu di-rate-limit?" sebagai open question — kode existing sudah jawab, tapi shallow + leaky.

---

## Wave 0 — Pre-existing Foundation (SELESAI)

| #  | Beads              | Tipe     | Judul                                                               | Status |
|----|--------------------|----------|---------------------------------------------------------------------|--------|
| 0a | `mail-service-lfe` | AFK · P3 | Rate-limit `/api/mails/verify-sign/{code}` (Bucket4j 30 req/min/IP) | [x]    |

> Wave 0 melahirkan `RateLimitService` versi pertama. Refactor di bawah men-deepen hasilnya.

---

## Wave 1 — Kick off paralel

### AFK (Independent)

| #  | Beads              | Tipe     | Slice                                                                                                               | Catatan                                                                                                                                                                                      | Status |
|----|--------------------|----------|---------------------------------------------------------------------------------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|--------|
| 1a | `mail-service-ysx` | AFK · P2 | Replace `RateLimitService` → `SignatureVerificationThrottle` (Redis-backed, fix Bucket4j deprecation + memory leak) | Pakai `bucket4j_jdk17-lettuce` 8.10.1 + `LettuceConnectionFactory` yang sudah ada · key `rate-limit:verify-sign:{ip}` · `refillIntervally` (preserve behavior) · fail-open kalau Redis error | [ ]    |
| 1b | `mail-service-957` | AFK · P2 | Extract `ClientIpResolver` (CF-Connecting-IP / X-Real-IP / XFF / `remoteAddr`)                                      | Header priority eksplisit · single source of truth untuk throttle + `print_log` audit · update CONTEXT.md bagian Signature/Print-Verification                                                | [ ]    |

> 1a dan 1b orthogonal. Kalau 1a dikerjakan duluan, 1b nanti tinggal swap sumber IP tanpa nyentuh `SignatureVerificationThrottle`.

---

## Wave 2 — Follow-up opsional (post-MVP)

| #  | Beads          | Tipe     | Slice                                                                                                               | 🔒 Blocked by           | Status |
|----|----------------|----------|---------------------------------------------------------------------------------------------------------------------|-------------------------|--------|
| 2a | _belum dibuat_ | AFK · P3 | `app.security.trusted-proxies` CIDR allow-list — XFF/CF-Connecting-IP cuma dihormati saat `remoteAddr` masuk daftar | `mail-service-957` (1b) | [ ]    |
| 2b | _belum dibuat_ | AFK · P3 | Compound bucket key (`ip + hash(authCode prefix)` atau `ip + userAgent`) untuk skenario CGNAT / shared edge         | `mail-service-ysx` (1a) | [ ]    |

> Buat issue baru saat benar-benar di-deploy di belakang reverse-proxy yang stabil & terdaftar (kalau di-deploy di belakang CF Tunnel, 2a wajib).

---

## Critical Path

```
0a (DONE: lfe — initial Bucket4j rate-limit)
        │
        ▼
1a (ysx) ──────── (opsional) 2b
        │
        ▼
1b (957) ──────── (opsional) 2a
```

Critical path saat ini: **1a → 1b**. Kalau di-deploy di belakang CF Tunnel, tambah **1b → 2a** sebagai blocker production.

---

## Rekomendasi Eksekusi

1. **Mulai dengan `mail-service-ysx`** — fix deprecation Bucket4j + memory leak duluan. Caller cuma 1 (`MailSignatureController:46`), risiko regresi kecil. Tests existing (`MailSignatureControllerRateLimitTest`) bisa pakai `@MockBean` setelah deepening.
2. **Lanjut `mail-service-957`** — extract `ClientIpResolver`. Setelah ini, sumber IP yang dipakai `SignatureVerificationThrottle.allow(ip)` jadi konsisten dengan IP yang ditulis ke `print_log.ip_address`.
3. **Tunda Wave 2** sampai ada kebutuhan deploy di belakang reverse-proxy stabil. Jangan over-engineer trusted-proxies sebelum infrastruktur deploy diputuskan.
4. **Tidak ada perubahan schema** — kedua refactor murni di lapisan service/controller. Tidak butuh migrasi Flyway.

---

## Out of Scope

- Migrasi data legacy (`print_log` lama 13-char auth_code) — tetap di luar scope, sesuai CONTEXT.md baris 1373.
- Crypto signature elektronik tersertifikasi (post-MVP, di-track terpisah lewat ADR-008 / `mail-service-421`).
- Rate-limit untuk endpoint lain (login, HR fetch, publication download) — tidak diangkat sebagai seam `Throttle` generik sampai muncul adapter kedua. **One adapter = hypothetical seam. Two adapters = real seam.**
- Audit trail "siapa lihat verifikasi" — di-acknowledge di CONTEXT.md baris 1415, tetap out of scope.

---

## Cross-reference

- Beads list: `bd ready`, `bd show mail-service-ysx`, `bd show mail-service-957`.
- Skill: `/improve-codebase-architecture` (session 2026-05-09 menghasilkan dua issue ini).
- Glossary architectural: `~/.claude/skills/improve-codebase-architecture/LANGUAGE.md` (definisi *deep*, *seam*, *adapter*, *deletion test*).
- CONTEXT.md bagian terkait: **Signature / Print-Verification — Tata Kelola** (baris 1333–1428).
- ADR yang relevan: tidak ada konflik dengan ADR existing (001–010). Tidak butuh ADR baru kecuali Wave 2 trusted-proxies di-promote (saat itu butuh ADR-011 untuk policy "siapa proxy yang dipercaya").

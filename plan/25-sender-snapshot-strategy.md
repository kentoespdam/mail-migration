# Plan 25 — Sender Snapshot Strategy (HITL Decision)

> Beads: `mail-service-293` · Parent PRD: `mail-service-t0v` (User Story 9, Open Question #7)
> Type: HITL decision-required, output adalah ADR + resolution PRD OQ#7.
> Hard constraint PRD: NO backend schema change ke 4 tabel inti
> (`mail`, `mail_recipient`, `user_task`, `mail_folder`, `print_log`, `mail_respontime`).

---

## 1. Problem Statement

Saat user (pejabat) mengirim surat lalu kemudian dimutasi/promosi, audit
historis "siapa pengirim & apa jabatannya saat surat dikirim" harus tetap
dapat direkonstruksi. Saat ini `Mail` hanya menyimpan `m_created_by` (FK ke
employee) dan `m_created_by_name` (varchar 64). Tidak ada snapshot jabatan.

Pertanyaan: bagaimana kita pertahankan snapshot jabatan sender dengan biaya
storage minimal dan reliability maksimal?

## 2. Pilihan yang Dievaluasi

### Opsi A — Snapshot JSON column ke `mail`
Tambahkan kolom `m_sender_snapshot JSON NULL` ke tabel `mail`. Saat insert
mail (status DRAFT → SENT), populate dengan struktur:

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

- **Pro:** read-time O(1), tidak depend HR availability, immutable audit,
  simple read path (langsung dari row mail).
- **Con:** schema change ke `mail` (HARD CONSTRAINT PRD!), storage cost
  ~250–400 byte × 1.8M row ≈ 450MB–720MB tambahan + index/row overhead.

### Opsi B — Reconstruct dari HR audit log + timestamp
Tidak ada kolom baru. Saat butuh sender snapshot, query HR service:
"posisi user X pada timestamp T (= mail.m_created_date)".

- **Pro:** 0 schema change (compliance HARD CONSTRAINT PRD), 0 storage
  tambahan di mail-service.
- **Con:** depend pada cakupan & retensi HR audit log; HTTP call per row
  (mahal di list view 1.8M); cache invalidation rumit; rekonstruksi
  bisa gagal kalau HR audit incomplete.

### Opsi C — Snapshot ke kolom existing `m_created_by_name` extended
Re-purpose `m_created_by_name` (varchar 64) untuk simpan
`"Budi Santoso · Manajer Keuangan"` format snapshot.

- **Pro:** 0 schema change, backward compatible.
- **Con:** parsing string fragile, length 64 kurang untuk nama+posisi+unit,
  irreversible (overload semantik kolom legacy), tidak immutable kalau
  dimisuse.

### Opsi D — Tabel baru `mail_sender_history` (sidecar)
Tabel baru terpisah, bukan kolom di `mail`. Komponen: `(mail_id, employee_id,
position_id_snapshot, position_name_snapshot, unit_name_snapshot,
captured_at)`.

- **Pro:** tidak menyentuh tabel `mail`, sidecar additive, indexable per
  field, query JOIN ringan.
- **Con:** tabel baru = schema change (tapi BUKAN ke 4 tabel inti yang
  hard-constrained), butuh migrasi backfill 1.8M row, butuh JOIN tiap read.

---

## 3. Investigasi yang Wajib Dilakukan (Pra-Decision)

Tahapan ini menghasilkan data konkret untuk acceptance criteria di
`mail-service-293`.

### 3.1 HR Audit Log Coverage Probe
Tujuan: konfirmasi apakah Opsi B feasible.

- [ ] Cek availability endpoint HR untuk historical query:
  `GET {hr-host}/api/employees/{id}/positions?at={timestamp}` atau
  ekuivalen.
- [ ] Cek retensi audit log HR (apakah ada record sebelum 2018?
  legacy mail mulai 2017).
- [ ] Cek granularity: position-level only? unit-level juga?
- [ ] Cek SLA & rate limit endpoint historical query (call cost untuk
  list view 25-row paginated).
- [ ] Sampling: ambil 50 mail random dari `smartoffice@192.168.230.84`
  (rentang 2018, 2021, 2024) → query HR audit untuk
  `(m_created_by, m_created_date)` → ukur hit rate.

**Output:** tabel coverage `(year_bucket, sample_size, hit_rate, miss_reason)`.

### 3.2 Storage Estimation Opsi A
Tujuan: konfirmasi cost penambahan kolom JSON.

- [ ] Hitung average size 1 snapshot JSON dari payload contoh
  (~280 byte serialized, ~320 byte JSON overhead di MariaDB).
- [ ] Estimasi tambahan ukuran tabel `mail`:
  `1.8M × 320 byte ≈ 576 MB` (data only, exclude index).
- [ ] Cek current size tabel `mail` di staging
  (`SELECT data_length, index_length FROM information_schema.tables
  WHERE table_name='mail'`).
- [ ] Estimasi growth tahunan: legacy ±300k mail/tahun → ±96 MB/tahun.
- [ ] Hitung dampak ke backup window & replication lag.

**Output:** angka konkret `current_size + delta + 5y_projection`.

### 3.3 Storage Estimation Opsi D
- [ ] Schema sidecar lebih lean (tidak JSON, kolom typed) → ~80 byte/row
  → `1.8M × 80 ≈ 144 MB` data + index.
- [ ] Hitung storage relatif Opsi A vs D.

### 3.4 Hard Constraint Check
- [ ] Konfirmasi dengan PRD `mail-service-t0v`: apakah "schema change ke
  `mail`" dilarang absolut, atau hanya "perubahan kolom existing"?
  Penambahan kolom NULLABLE biasanya non-blocking di MariaDB 11.4 dengan
  `ALGORITHM=INSTANT`.
- [ ] Cek beads `mail-service-128` (V27 — kalau ada perubahan kolom mail)
  dan plan 24 untuk precedent.

---

## 4. Decision Matrix (untuk diisi setelah investigasi)

| Kriteria              | A: JSON col mail | B: HR reconstruct | C: name extended | D: sidecar table |
|-----------------------|------------------|-------------------|------------------|------------------|
| PRD hard-constraint   | ❓ (depends 3.4)  | ✅                | ✅               | ✅ (bukan 4 inti) |
| Storage cost          | ~576 MB          | 0                 | 0                | ~144 MB          |
| Read latency          | O(1) row         | O(1) HTTP+cache   | O(1) row         | O(1) JOIN        |
| HR dependency         | None             | Hard              | None             | None             |
| Audit immutability    | ✅                | ❌ (mutable HR)   | ⚠️ overload      | ✅                |
| Backfill 1.8M effort  | High             | None (lazy)       | None             | High             |
| Implementation complexity | Low          | High (cache+fallback) | Low          | Medium           |

## 5. Default Recommendation (pra-investigasi)

**Opsi D (sidecar `mail_sender_history`)** adalah default rekomendasi:

1. Tidak melanggar hard-constraint "no change ke 4 tabel inti".
2. Storage 4× lebih kecil dari Opsi A (typed column vs JSON overhead).
3. Tidak depend HR availability runtime.
4. Audit immutable (sidecar = append-only logically).
5. Mudah di-index per `(employee_id, captured_at)` untuk report.

**Fallback:** jika investigasi 3.4 menunjukkan penambahan kolom NULLABLE ke
`mail` tidak melanggar PRD (karena ALGORITHM=INSTANT, non-blocking),
pertimbangkan Opsi A untuk kesederhanaan read path.

**Rejected:** Opsi B (HR reconstruct) — single point of failure, mahal di
list view, dan audit ter-mutate kalau HR audit dihapus. Opsi C (name
extended) — fragile, tidak scalable.

## 6. Acceptance Criteria (mirror beads)

- [ ] HR audit log coverage diverifikasi dengan sampling 50 mail (3.1).
- [ ] Estimasi storage dihitung untuk Opsi A & Opsi D (3.2, 3.3).
- [ ] Hard-constraint PRD dikonfirmasi via cross-check ke
      `mail-service-t0v` & precedent plan 24 (3.4).
- [ ] ADR `docs/adr/002-sender-snapshot-strategy.md` ditulis berisi:
      pilihan final, reasoning, decision matrix terisi, alternatif yang
      ditolak + alasan.
- [ ] PRD Open Question #7 (User Story 9) di `mail-service-t0v`
      di-update dengan link ke ADR.
- [ ] Beads `mail-service-293` di-close dengan link ke ADR.

## 7. Implementation Plan (jika Opsi D Dipilih)

> Plan implementasi konkret hanya disusun setelah ADR final. Section ini
> adalah sketsa skeleton untuk eksekusi follow-up issue.

### 7.1 Schema (Flyway, plan migration baru)
```sql
CREATE TABLE mail_sender_history (
    msh_id           BIGINT PRIMARY KEY AUTO_INCREMENT,
    msh_mail_id      BIGINT NOT NULL,
    msh_employee_id  BIGINT NOT NULL,
    msh_position_id  BIGINT NULL,
    msh_position_name VARCHAR(128) NOT NULL,
    msh_unit_id      BIGINT NULL,
    msh_unit_name    VARCHAR(128) NULL,
    msh_captured_at  TIMESTAMP NOT NULL,
    CONSTRAINT fk_msh_mail FOREIGN KEY (msh_mail_id) REFERENCES mail(m_id),
    INDEX idx_msh_mail (msh_mail_id),
    INDEX idx_msh_employee_captured (msh_employee_id, msh_captured_at)
);
```

### 7.2 Service Hook Points
- `MailCommandService.send()` → tepat saat status DRAFT→SENT, capture
  snapshot dari `MailPrincipal` + HR live position → insert
  `mail_sender_history`.
- `MailQueryService.getSenderInfo(mailId)` → JOOQ JOIN 1× ke
  `mail_sender_history`.

### 7.3 Backfill Strategy 1.8M Legacy
- Job batch (Spring Batch atau script JOOQ) dengan chunk 5k/transaction.
- Untuk legacy mail, populate snapshot dari `(m_created_by,
  m_created_by_name)` + best-effort lookup HR pada `m_created_date`.
- Mark `msh_position_name = 'Pre-migrasi (data tidak tersedia)'` untuk
  miss → tetap valid audit trail.

### 7.4 Out of Scope (untuk issue follow-up terpisah)
- Snapshot recipient (sudah ditangani via `mail_recipient.pos_name`
  legacy column — User Story 9 separate path).
- Signature snapshot pada blob mail history (User Story 10 — issue
  terpisah).

## 8. Risk & Mitigation

| Risk                                                          | Mitigation                                                                          |
|---------------------------------------------------------------|-------------------------------------------------------------------------------------|
| Backfill 1.8M baris bikin DB lag                              | Chunk 5k + sleep 100ms antar chunk, jalan off-hours, monitor replication lag.       |
| HR live lookup gagal saat capture (network, HR down)          | Fallback: capture employee data dari `MailPrincipal` cache, position_name="Unknown" + retry async via outbox. |
| Position rename di HR setelah capture                         | Snapshot adalah immutable point-in-time → expected behaviour, tidak follow rename. |
| ADR diundur, blocking PRD `mail-service-t0v`                  | HITL session di-schedule explicit dengan stakeholder Product + Tech Lead.           |

## 9. Timeline (Estimasi)

- Investigasi (Section 3): 1.5 hari engineer.
- HITL meeting + ADR draft: 0.5 hari.
- Stakeholder approval (Product/Tech Lead): 0.5 hari (async).
- Total decision phase: ~2.5 hari sebelum implementation issue dibuat.

## 10. References

- Beads: `mail-service-293`, `mail-service-t0v`.
- PRD User Story 9, Open Question #7 (sender snapshot).
- Plan 24 (`24-entity-legacy-alignment-and-fk-introduction.md`) — precedent
  untuk additive schema change.
- ADR `001-plt-model-representation.md` — format reference ADR project.
- Memory `legacy-disposisi-no-completion-flag` — context legacy data
  topology (1.8M mail, depth 8-10).

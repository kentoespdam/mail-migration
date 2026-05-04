# Context — Mail Service (Persuratan & Disposisi)

> Domain glossary & bounded context map untuk frontend persuratan/disposisi.
> Backend tetap dengan bahasa & skema existing (project migrasi). FE memakai
> ubiquitous language sendiri lewat Anti-Corruption Layer.

## Constraints

- **Migrasi, bukan greenfield.** Skema DB & kontrak API backend TIDAK boleh
  diubah — data existing harus tetap bisa sync. Penamaan domain di FE boleh
  beda, tapi harus diterjemahkan ke DTO backend lewat ACL.

## Personas

Tidak ada persona statis. Semua user bisa menerima maupun mendisposisikan
surat — kapabilitas muncul dari **jabatan / posisi struktural** user, bukan
dari role aplikasi yang di-assign manual.

Yang membedakan pengalaman user adalah **role-in-context** terhadap setiap
surat yang dilihat (penerima vs pemberi disposisi vs pelaksana vs pendaftar).

## Bounded Contexts (rencana — akan dipertajam)

Bounded context FE dibentuk dari **mental model user**, bukan mirror modul
backend 1:1. Tiap BC memanggil ≥1 modul backend lewat ACL.

Kandidat BC (belum final):
- Inbox & Mail Reading
- Compose & Outgoing Mail
- Disposisi (giving & receiving)
- Arsip
- Publikasi
- Master Data (supporting)

## Glossary

### MailEntry
View per-user terhadap satu surat di folder tertentu (Inbox / Draft / Sent /
Trash / dst), dengan read-state milik user itu sendiri. Dua user yang
menerima surat yang sama punya `MailEntry` yang berbeda meski merujuk ke
`Mail` yang sama.

- **Backend equivalent**: `UserTask` (tabel `sys_user_task`).
- **Why renamed**: istilah `UserTask` di backend mengikuti konvensi workflow
  engine, tapi membingungkan domain expert (mereka kira itu "tugas
  disposisi"). `MailEntry` lebih akurat: ini adalah entry surat di kotak
  seseorang, bukan tugas.

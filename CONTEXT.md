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
- Compose & Outgoing Mail (mode **Memo Internal** atau **Surat Masuk Eksternal** — lihat glossary)
- Disposisi (giving & receiving)
- Arsip
- Publikasi
- Master Data (supporting)

## Glossary

### Disposition (Disposisi)
Salah satu **jenis sirkulasi surat** (`CirculationType.DISPOSISI`) — tag pada
relasi `Mail → Recipient` yang menandakan "surat ini dikirim ke user X
dengan maksud didisposisikan untuk dikerjakan", bukan sekadar CC/copy.

**BUKAN aggregate sendiri.** Disposisi tidak punya state machine
eksplisit di backend; yang ada hanyalah:
- `read_status` pada `MailEntry` (sudah dibaca atau belum).
- Eksistensi `Mail` child (`CirculationType.REPLY` ke atas, atau
  `CirculationType.DISPOSISI`/`FORWARD` cascade ke bawah) yang merujuk
  ke surat asli — menandakan disposisi sudah ditindaklanjuti.
- `MailResponseTime` mencatat selisih waktu original ↔ reply.

Untuk kebutuhan UI ("Disposisi yang menungguku", "yang sudah kujawab"),
FE menghitung **state turunan** dari kombinasi data di atas — tidak boleh
bikin tabel state baru di backend (constraint migrasi).

### Status Disposisi (FE-derived)
Dihitung on-the-fly di FE, **tidak disimpan**. Mengikuti realita legacy
(SmartOffice) yang memang tidak menyimpan flag "selesai" — `m_status`
99% bernilai 1 (soft-delete-like, bukan status disposisi).

Untuk satu Mail dengan ≥1 recipient `DISPOSISI`, dari **sudut pandang
pemberi disposisi**:

| Kondisi | Label FE |
| --- | --- |
| Semua recipient DISPOSISI sudah membuat ≥1 Mail child | **DITINDAKLANJUTI** |
| Sebagian recipient DISPOSISI sudah membuat child | **SEBAGIAN** |
| Tidak ada child sama sekali | **MENUNGGU** |
| MENUNGGU/SEBAGIAN + `m_max_response_date` ter-set & sudah terlewat | **TERLAMBAT** (override label) |

**Cascade-down (DISPOSISI/FORWARD lanjutan) dihitung setara dengan REPLY**
sebagai bukti "sudah ditindaklanjuti". Rasionalnya: di organisasi
struktural, manajer yang meneruskan ke bawahannya = sudah menjalankan
tanggung jawabnya pada level itu. CC tidak dihitung.

CC **tidak** dihitung sebagai bukti tindak lanjut — hanya recipient
ber-`CirculationType.DISPOSISI` yang masuk denominator.

### Status MailEntry (FE-derived, sudut pandang penerima)
Per `MailEntry` milik user X, **dua dimensi independen** yang dihitung
on-the-fly:

**Dimensi 1 — Status baca:**
- `BELUM DIBACA` — `read_status = 0`.
- `SUDAH DIBACA` — `read_status = 1`.

**Dimensi 2 — Status tindak lanjut** (hanya berlaku jika
`MailRecipient.circulation = DISPOSISI` untuk user X — CC/MEMO
**tidak dihitung** karena dianggap informasi, bukan kewajiban):
- `SUDAH DITINDAKLANJUTI` — user X sudah membuat ≥1 Mail child dengan
  `parent = mail` ini. Cascade-down (DISPOSISI/FORWARD lanjutan) **setara**
  dengan REPLY ke atas.
- `BELUM DITINDAKLANJUTI` — belum ada Mail child dari user X.

**Constraint migrasi**: derivasi ini **read-time only** — tidak ada
kolom baru, tidak ada update ke `mail`/`mail_recipient`/`sys_user_task`.
Aman untuk data legacy 1.8M+ baris.

### Deadline Disposisi
Dipetakan dari `mail.m_max_response_date` (legacy) — **opsional**.
Hanya ~11% surat di legacy yang punya deadline real (sisanya
`0000-00-00` sentinel). Pemberi disposisi yang lupa set deadline =
label TERLAMBAT tidak akan muncul. Ini bukan bug FE, ini refleksi
data lama.

FE menampilkan deadline jika ter-set, dan menggunakan-nya untuk
override label menjadi TERLAMBAT (lihat tabel di **Status Disposisi**).

### Read Model vs Command Model (CQRS di FE)
FE memisahkan tipe data **read** dan **write** — satu backend DTO bisa
di-map ke beberapa tipe FE tergantung use case.

- **Read models** (per use case, lightweight):
  - `MailListItem` — ringkasan untuk list view (subject, sender, date,
    hasAttachment, circulationType, readStatus). Tidak ada body, recipients
    lengkap, attachments lengkap.
  - `MailDetail` — full view saat surat diklik (body, recipients,
    attachments metadata, signatures, history).
  - `AttachmentBlob` — fetched lazy saat user klik download/preview.

- **Command models** (untuk submit ke backend):
  - `ComposeMailDraft` — form state saat user mengetik.
  - `SendMailCommand` — payload submit kirim surat.
  - `ReplyCommand`, `MoveEntryCommand`, `MarkReadCommand`, dst.

Implikasi: cache di-key per tipe (mis. list cache ≠ detail cache, tidak
saling invalidate).

### CirculationType (Sirkulasi)
**Konsep paling sentral di domain ini.** Menentukan makna setiap relasi
`Mail → Recipient` dan menentukan UI yang dipresentasikan ke user.

Jenis (`sys_reference.code='sirkulasi'`):
- **DISPOSISI(1)** — atasan → bawahan. Instruksi mengerjakan sesuatu.
  Tombol tersedia hanya jika user punya bawahan struktural.
- **MEMO_MANDIRI(2)** — pelaksana → atasan langsung. Dipakai untuk
  melaporkan hasil disposisi ke 1 atasan langsung. FE auto-suggest
  recipient = atasan langsung user.
- **MEMO(3)** — surat antar unit / peer-level.
- **CC(4)** — tembusan, multi-recipient.
- **REPLY(5)** — balasan (terutama untuk surat masuk eksternal).
- **FORWARD(6)** — teruskan tanpa instruksi formal. Tersedia untuk
  semua pegawai (search ke seluruh pegawai).

**Implikasi FE — Recipient picker tergantung CirculationType yang dipilih:**

| Circulation | Recipient picker FE |
| --- | --- |
| DISPOSISI | **Direct reports saja** (1 level ke bawah, multi-select) |
| MEMO_MANDIRI | Auto-isi atasan langsung (1 orang) |
| MEMO | Search unit/jabatan tujuan |
| CC | Search semua pegawai (multi-select) |
| REPLY | Auto-isi pengirim surat asli |
| FORWARD | Search semua pegawai |

Sumber data struktural (atasan langsung, bawahan, dst) diambil dari
HR service via OpenFeign client di backend.

**DISPOSISI direct-reports-only (rantai komando ketat).**
DISPOSISI tidak boleh skip-level (Dirut tidak bisa langsung disposisi
ke Supervisor) — selalu hanya 1 hop ke direct reports. Multi-level
disposisi terjadi sebagai cascading thread (lihat **Disposisi
Bertingkat**). Untuk instruksi lintas-level/lintas-unit, pakai
`FORWARD`, `MEMO`, atau tambahkan recipient `CC` pada disposisi
direct. Dikonfirmasi dari data legacy: di node thread dengan 30+
recipient, semuanya `circulation=CC(4)` — DISPOSISI tetap direct.

Kombinasi tipikal di UI compose: **Disposisi (ke direct reports)
+ CC opsional (ke siapa saja yang perlu tahu)**.

### CirculationContext (FE-only)
Helper yang menentukan CirculationType apa saja yang valid sebagai
aksi dari sebuah `MailDetail`, berdasarkan:
- Apakah user punya bawahan struktural.
- Apakah surat ini punya parent (sedang dalam thread disposisi).
- Apakah user adalah recipient asli atau hanya bisa membaca.

Output: list `{ type: CirculationType, label: string, enabled: boolean,
defaultRecipients: User[] }` yang dipakai untuk render tombol aksi
("Disposisikan", "Laporkan", "Forward", dst).

### Mail Thread (Thread Surat)
Surat tersusun sebagai pohon: `Mail.parentMail` = induk langsung,
`Mail.rootMail` = akar thread. Mendisposisikan / mem-forward / mereply
sebuah surat = membuat `Mail` baru dengan `parent` di-set ke surat
asli, lalu menambah `MailRecipient` dengan `CirculationType` yang sesuai
(`DISPOSISI`, `FORWARD`, `REPLY`).

### Disposisi Bertingkat (Cascading Disposition)
"Bertingkat" = **emergent property** dari Mail thread, bukan field state
eksplisit. Tiap level hanya melakukan satu hop:

- Direksi membuat `Mail(circulation=DISPOSISI)` ke Manager.
- Manager menerima, lalu **membuat Mail baru** dengan `parent` = Mail
  Direksi, recipient DISPOSISI = Staf.
- Staf menerima, mengerjakan, lalu reply (`Mail` baru, `circulation=REPLY`,
  parent = Mail Manager).

Tiap node thread = satu Mail independen. Tidak ada tabel state lintas-level.
History bertingkat = traversal pohon dari `rootMail` ke daun.

**CC opsional pada tiap level**: saat membuat disposisi, pengirim boleh
menambahkan recipient bercirculation `CC` di Mail yang sama (mis. Manager
disposisi ke Staf, dengan CC ke Supervisor untuk awareness). CC tidak
membentuk cabang baru di thread, hanya menambah audience pada node yang
sama.

Implikasi UI:
- "History sirkulasi" di detail surat = traversal pohon dari root.
- "Disposisi yang menungguku" = filter `MailEntry` di folder INBOX di mana
  `MailRecipient.circulation = DISPOSISI` dan `read_status = UNREAD` (atau
  belum ada child Mail dengan circulation REPLY oleh user ini).

### Tipe Surat (Mail Type) — FE-only Mode
**Mail bukan hanya pesan antar pegawai.** Entity `Mail` jadi wrapper untuk
3 jenis komunikasi: internal antar pegawai, surat fisik dari luar yang
didigitalisasi, dan surat resmi yang dikirim keluar organisasi.

Field opsional di entity `Mail` (semua nullable di backend — tidak ada
`@NotNull`):

| Field | Untuk tipe |
| --- | --- |
| `m_no_surat_masuk` (nomor surat asli) | Surat Masuk |
| `m_asal_surat_masuk` (instansi/vendor pengirim) | Surat Masuk |
| `m_tgl_surat_masuk` (tanggal surat fisik) | Surat Masuk |
| `m_tujuan_surat_keluar` (instansi tujuan) | Surat Keluar |
| `m_penerima_surat_keluar` (nama PIC/orang di instansi tujuan) | Surat Keluar |

Backend netral — **FE yang menentukan** validasi via toggle Tipe Surat di
form compose:

| Tipe Surat | Field wajib (FE rule) | Use case |
| --- | --- | --- |
| **Internal** (Memo) | tidak ada field eksternal — semua di-skip / null | Memo, disposisi, reply antar pegawai |
| **Surat Masuk** (Eksternal) | `noSuratMasuk`, `asalSuratMasuk`, `tglSuratMasuk` **wajib** | TU/Sekretariat men-digitalisasi surat fisik dari luar |
| **Surat Keluar** | `tujuanSuratKeluar`, `penerimaSuratKeluar` **wajib** | Surat resmi keluar organisasi (ke vendor, instansi mitra, dll) |

Aturan FE:
- Tipe Internal → field surat masuk & surat keluar disembunyikan / kosong.
- Tipe Surat Masuk → tampilkan & wajibkan 3 field surat masuk; field surat
  keluar tetap kosong.
- Tipe Surat Keluar → tampilkan & wajibkan 2 field surat keluar; field
  surat masuk tetap kosong.

Tidak ada flag backend yang membedakan ketiga tipe — perbedaan hanya di
**kelengkapan data field opsional**. FE harus enforce validasi sebelum
submit; backend hanya akan menyimpan apa yang dikirim (nullable) tanpa
verifikasi tipe.

**Pendaftar / Pengirim** = role-in-context, bukan persona tetap. Siapa
pun yang memilih mode "Surat Masuk" berperan sebagai pendaftar; siapa
pun yang memilih "Surat Keluar" berperan sebagai pengirim resmi.
Praktiknya didominasi staf TU/Sekretariat karena merekalah yang
menangani surat fisik dua arah, tapi backend tidak membatasi.

Setelah disubmit, alur lanjutan = thread Mail biasa (disposisi/reply/
forward ke recipient internal — lihat **Disposisi Bertingkat**).

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

### MailFolder (System Folders)
Backend punya `SystemFolder` enum hardcoded (sesuai `mail_folder.id`):

| ID | Folder | Peran |
| --- | --- | --- |
| 2 | INBOX | Surat masuk yang belum ditindaklanjuti oleh penerima |
| 3 | DRAFT | Compose draft milik user |
| 4 | READ | Surat yang sudah ditindaklanjuti penerima (forward / re-disposisi / reply) — pindah dari INBOX setelah aksi |
| 5 | SENT | Mail yang dibuat user sebagai **root mail** (compose baru) |
| 6 | DELETED | Trash |
| 10 | PERSONAL_ROOT | Akar folder kustom buatan user (id > 10) |

**Aturan penempatan otomatis (sesuai legacy SmartOffice):**
- **Root mail** (compose baru tanpa parent) → masuk **Sent Items** milik pembuat.
- **Child mail** (mail apa pun yang punya `parent` — forward / re-disposisi /
  reply / memo mandiri / cascading disposisi) → masuk **Read Items** milik
  pembuat. **Sekaligus**, MailEntry milik pembuat untuk **parent mail**
  di-pindah dari **Inbox → Read Items** (menandai parent sudah
  ditindaklanjuti).
- Recipient baru (penerima dari root atau child) → masuk **Inbox** masing-masing.

**Trigger move parent Inbox→Read = eksistensi child mail apa pun.**
Tidak peduli `CirculationType` child (REPLY, DISPOSISI, FORWARD,
MEMO_MANDIRI, MEMO, CC). Selama ada Mail child dengan `parent_id` =
mail ini, dari sudut pandang pembuat child, parent dianggap sudah
ditindaklanjuti.

**Move bersifat per-user (per-MailEntry), bukan global.**
Jika satu Mail dikirim ke 3 recipient (M1, M2, M3) lalu hanya M1 yang
membuat child:
- MailEntry milik **M1** → pindah Inbox → Read.
- MailEntry milik M2 & M3 → **tetap di Inbox** (mereka belum tindak lanjut
  dari sisi masing-masing).

Yang berubah hanya satu row MailEntry (milik aktor). FE boleh menampilkan
indikator info-only ("M1 sudah menindaklanjuti") di view M2/M3 dari
traversal child Mail, tetapi **tidak** memindahkan folder mereka.

Rasional: tiap recipient punya tugas independen — folder harus
merefleksikan tindakan user itu sendiri, bukan agregat global, agar
tidak ada penerima yang kehilangan radar saat anggota lain bertindak
duluan.

Konsekuensi UX: Inbox milik user **hanya berisi surat yang belum
ditindaklanjuti**, sehingga tetap ringkas. "Sudah ditindaklanjuti" =
keberadaan MailEntry di folder Read Items, **bukan** flag eksplisit di
backend — ini selaras dengan keputusan "no completion flag" (lihat
**Status Disposisi**).

**Implikasi performa & search:**
- Listing Inbox cukup query MailEntry dengan `folder=INBOX` (bukan scan
  semua + filter "belum ditindaklanjuti" di app).
- Pencarian default scope **Inbox saja** untuk surat aktif; user bisa
  expand ke Read/Sent eksplisit. Mengurangi beban query yang seiring
  waktu menumpuk di satu folder besar.

**MailEntry pembuat root mail (sender) — hanya di SENT.**
Saat user A compose root mail ke B/C/D:
- A dapat **satu** MailEntry di **Sent Items** (arsip kirim).
- B, C, D masing-masing dapat MailEntry di **Inbox** mereka.
- A **tidak** punya MailEntry di Inbox/Read sendiri (sender bukan recipient
  dirinya sendiri — selaras konvensi email).

Saat B membalas (reply) root A → child Mail dengan parent = root A:
- B dapat MailEntry baru di **Read Items** (dia pembuat child).
- A jadi recipient reply B → A dapat MailEntry baru di **Inbox**
  (notifikasi tindak lanjut).
- MailEntry **SENT** A untuk root **tetap di Sent** — Sent = arsip kirim
  permanen, **immune** dari rule "Inbox→Read on child". Rule itu hanya
  berlaku untuk MailEntry yang sedang berada di **Inbox**.

Konsekuensi: "tindak lanjut yang sampai ke saya" = MailEntry baru di
Inbox A dari reply B, bukan perubahan state pada SENT entry root.

**Trash & Delete (per-user, bukan global).**
- User "delete" surat dari FE → **MailEntry** miliknya dipindah ke
  **DELETED folder** (id=6). Mail aslinya **tetap eksis** untuk
  recipient/sender lain.
- Berlaku sama untuk semua role: recipient delete entri Inbox/Read,
  sender delete entri Sent — semua memindahkan **MailEntry** masing-masing,
  bukan menghapus Mail.
- `Mail.is_deleted=1` (kolom global) **bukan** aksi user biasa — hanya
  dipakai admin/sistem (retention policy, hard purge, atau cleanup setelah
  semua MailEntry sudah di DELETED). Tidak ada single user yang bisa
  menghilangkan jejak surat dari recipient lain.

Rasional: 1.8M mail dengan thread disposisi mendalam → user tidak boleh
bisa menghapus konteks disposisi orang lain. Trash = pandangan personal,
bukan tindakan global. Audit & legal aman.

**Restore dari DELETED.**
- Backend sudah punya kolom `sys_user_task.restore_folder_id` —
  `softDelete()` set `restore_folder_id = current_folder` lalu pindah ke
  DELETED; `restore()` baca `restore_folder_id` dan kembalikan ke folder
  asal (Inbox/Read/Sent). **Tidak perlu kolom baru** atau aturan turunan
  di FE.

**Permanent delete (purge) dari DELETED.**
- User boleh hard-delete MailEntry dari folder DELETED → backend memanggil
  `purge()` yang **memindahkan folder ke `PURGED(-1)`**, bukan DELETE row.
  Row `sys_user_task` tetap eksis (audit-friendly).
- **Aman untuk child disposisi.** Yang di-purge adalah MailEntry milik
  user, bukan `Mail`. `Mail.parent_id → Mail.m_id` tidak terpengaruh,
  recipient lain tetap punya MailEntry utuh, sender root tetap melihat
  thread lengkap. Tidak ada cascade & tidak ada FK error.
- Opsional: auto-purge MailEntry yang sudah di DELETED >90 hari (cron
  level), agar folder tidak membengkak — tapi bukan blocker FE.

**Catatan FE (ACL):**
- "Read Items" di FE bisa di-rename ke yang lebih jelas (mis. **"Sudah
  Ditindaklanjuti"** atau **"Diproses"**) — istilah "Read" mudah
  dikonfusikan dengan read-state. ID folder tetap `4` di backend.

### Thread Visibility & ACL (cascading disposisi)
Skenario: thread A → B → C → D (cascading 4+ level).

**Aturan: full thread visible.** Begitu user punya MailEntry pada salah
satu node thread, dia bisa **traverse ke atas (parent chain) dan ke bawah
(child cascade)** dengan akses penuh — termasuk membaca **subject + isi
(content/note)** dan attachment di semua node.

Contoh — D (penerima level 4 di thread A→B→C→D):
- Bisa lihat root mail A (subject, isi, attachment).
- Bisa lihat disposisi B→C dan C→D (isi memo disposisi tiap level).
- Bisa lihat sibling thread (mis. cabang A→B'→C') **selama** D punya
  MailEntry pada satu node yang menjadikan thread itu masuk radar D.

Rasional: aplikasi yang sedang berjalan saat ini sudah memakai pola full
thread. Migrasi tidak boleh menyempitkan visibility yang sudah dimiliki
1.8M+ thread historical — risiko data hilang dari pandangan user yang
sebelumnya bisa lihat → user complaint + audit gap.

**Konsekuensi:**
- Thread visibility = **boolean** (punya MailEntry di salah satu node atau
  tidak), bukan tier-based.
- Tidak perlu kolom ACL tambahan. Visibility derive dari traversal:
  `Mail.parent_id` ke atas + `Mail` children (lookup by parent_id) ke
  bawah, batas = root (`parent_id = 0`/`null`) dan leaf.
- Confidentiality di level surat (mis. surat rahasia direksi) **bukan**
  diatur via ACL thread — gunakan **MailArchive + secretType** untuk
  dokumen yang benar-benar terbatas.

**Catatan FE:**
- Tampilkan parent chain (breadcrumb) + child tree di detail view, semua
  node clickable & expandable.
- Untuk thread sangat dalam (kasus nyata: 8-10 level, 30+ lebar) FE
  perlu strategi UI: lazy-load child, collapse default beyond level N,
  filter "thread relevan dengan saya" (highlight node tempat user
  punya MailEntry).

### Read Status (`MailEntry.read_status`)
Kolom `sys_user_task.read_status` (0=unread, 1=read) **independen** dari
folder. Folder = "sudah ditindaklanjuti?" (Inbox vs Read Items).
read_status = "sudah dilihat?" (badge bold/regular di list).

**Aturan flip:**
- **0 → 1 auto on open.** Saat user buka detail mail (GET detail
  endpoint), backend set `read_status = 1` untuk MailEntry milik user
  itu. Zero friction, sesuai konvensi email modern (Gmail/Outlook).
- **1 → 0 via "Mark as Unread".** User boleh kembalikan flag ke unread
  untuk menandai "perlu kembali nanti". Tombol eksplisit di UI.
- **Tidak menggeser folder.** Auto-read TIDAK memindahkan MailEntry
  Inbox→Read; perpindahan folder hanya terjadi saat ada child Mail
  (tindak lanjut) sesuai aturan MailFolder.

**Konsekuensi:**
- Inbox bisa berisi item yang sudah dibaca (`read_status=1`) tapi belum
  ditindaklanjuti — UI tampilkan dengan styling regular (non-bold)
  tapi tetap di Inbox.
- Counter "unread Inbox" = `count(MailEntry where folder=INBOX AND read_status=0)`.
- Sender's SENT entry: irrelevan (sender tidak butuh "read" dirinya
  sendiri); FE boleh ignore read_status pada Sent view atau set 1
  default saat compose.

### Attachment Visibility (per-node, bukan aggregate thread)
Attachment terikat ke **node Mail** spesifik via `Attachment.ref_id =
Mail.m_id` + `ref_type=1`. Visibility di thread cascading:

**Aturan: per-node.** Saat user buka detail mail di node X, hanya
attachment milik node X yang ditampilkan dalam list utama. Untuk lihat
attachment node parent/child lain, user navigasi ke node tersebut via
breadcrumb / thread tree (lihat **Thread Visibility & ACL**).

Contoh — thread A(att X) → B → C → D(att Y):
- D buka view child C→D → list attachment hanya **Y**.
- D klik breadcrumb root A → buka detail A → list attachment **X**.
- Tidak ada aggregate "semua attachment thread" di satu list.

Rasional:
- Lokalitas konteks: attachment di tiap level disposisi punya makna
  berbeda (lampiran asli vs lampiran tindak lanjut). Aggregate berisiko
  membingungkan + duplikasi visual jika parent attachment sudah
  di-supersede.
- Backend zero-change: query attachment by `ref_id` per node, no join
  ancestry / no aggregate endpoint baru.
- Selaras dengan Q13 full-thread-visible: akses tetap penuh, tapi
  user **memilih** kapan mau lihat attachment level mana via navigasi
  thread, bukan dipaksa lihat sekaligus.

**Catatan FE:**
- Indikator jumlah attachment di breadcrumb / tree node (mis. badge
  "📎 2") membantu user tahu node mana yang punya lampiran tanpa harus
  buka satu-satu.
- Counter `Mail.m_attachment_qty` sudah dimaintain backend — pakai itu
  untuk badge tanpa query attachment table.

### Notification Fan-out (siapa di-notify saat child Mail dibuat)
Skenario: thread A → B → C, lalu C bikin child Mail (disposisi ke D
atau reply ke B).

**Aturan: hanya recipient langsung child Mail.** Notif (in-app /
email) di-fan-out **hanya** ke entry di `mail_recipient` child tersebut:
- C disposisi ke D → **D** dapat notif. A & B tidak.
- C reply ke B → **B** dapat notif. A tidak.

A / ancestor lain TIDAK di-notify, walau mereka tetap **bisa lihat**
thread (Q13 full thread visible) dan akan menerima MailEntry baru di
Inbox kalau secara recipient termasuk (mis. reply C→B menambah B
sebagai recipient → B dapat MailEntry Inbox + notif).

Rasional:
- Selaras dengan tabel `mail_recipient` existing — fan-out by recipient
  list, bukan thread traversal.
- Hindari notification fatigue di thread dalam (8-10 level, lebar 30+).
  Kalau setiap cascade fan-out ke ancestor → A bisa di-spam puluhan
  notif untuk thread yang dia sudah lupa.
- Konsisten dengan keputusan **no completion flag** — tidak ada
  agregasi state ke atas; ancestor harus aktif lihat thread kalau
  butuh follow.

**Konsekuensi:**
- Sender (A) ingin tahu progress thread dalam → buka detail root,
  lihat child tree (Q13). Bukan via notif.
- "Tindak lanjut sampai ke saya" = MailEntry baru di Inbox dari
  reply yang menjadikan saya recipient (bukan dari notif fan-out
  ancestor).
- FE tidak perlu endpoint baru — backend cukup fan-out sesuai
  `mail_recipient` rows untuk child Mail.

### "Disposisi yang menungguku" (derive view, no schema change)
Tidak ada kolom `completed_at` / flag selesai eksplisit di backend
(lihat memori `legacy-disposisi-no-completion-flag`). View "tugas
disposisi yang belum kuselesaikan" **derive dari Inbox** + circulation
type:

**Aturan:** `MailEntry where user_id=me AND folder=INBOX
AND circulation_type=DISPOSISI`.

Mekanisme auto-clear: begitu user bikin child Mail (forward / reply /
disposisi) yang merefer mail ini sebagai parent → backend pindahkan
MailEntry Inbox→Read (rule Q9) → otomatis lenyap dari list "menungguku".

Rasional & batas:
- **Inbox by definition = belum tindak lanjut** (Q9). Filter
  `circulation_type=DISPOSISI` hanya untuk pisahkan dari CC/info-only;
  kalau FE OK gabung, tab Inbox umum sudah cukup.
- **JANGAN gabung dengan read_status / open event.** Read = sudah
  dilihat, bukan sudah ditindaklanjuti (Q14). Memindah folder
  Inbox→Read on open akan menghilangkan disposisi dari radar user
  begitu sekali dibuka untuk dibaca.
- **JANGAN tambah kolom `completed_at`** — melanggar konstrain
  migrasi (schema backend tidak berubah).

**Konsekuensi:**
- Counter "disposisi pending" di nav = `count(MailEntry where
  folder=INBOX AND circulation_type=DISPOSISI AND user_id=me)`,
  fast & indexed.
- Tidak ada agregasi atau state machine baru. Pure folder + relasi
  recipient.

### Deadline / SLA (`m_max_response_date`)
Field deadline di backend **opsional** — data legacy hanya 11% (198k
dari 1.8M) yang punya nilai (memori `legacy-disposisi-no-completion-flag`).
89% sisanya null = sender tidak set deadline.

**Aturan FE:**
- **Hide jika null.** View recipient tidak menampilkan badge deadline
  kalau `maxResponseDate = null`. Compose form: deadline = optional
  input (sender boleh kosongkan).
- **Tidak auto-fill default.** Jangan retroaktif memberi makna baru
  ke row historical (mis. +7 hari) — meaning null = "no deadline" harus
  preserved.
- **Badge "Overdue".** Untuk MailEntry dengan `maxResponseDate != null`
  AND `maxResponseDate < today` AND `folder=INBOX` → tampilkan badge
  merah "Terlewat" / overdue indicator. Begitu user tindak lanjut →
  MailEntry pindah ke Read (Q9) → badge hilang dari Inbox view.

Rasional:
- Selaras semantik data legacy & konstrain migrasi (no schema change).
- Highlight visual untuk disposisi prioritas tanpa memaksa sender
  selalu set deadline.
- Hitungan overdue murni client-side (`deadline < today`); tidak perlu
  field/job baru di backend.

**Konsekuensi:**
- "Disposisi menungguku" view (Q17) bisa di-sort: overdue dulu (deadline
  terlewat), lalu dengan deadline (asc), lalu tanpa deadline (oldest
  first).
- Tidak ada SLA reporting otomatis di scope migrasi awal — kalau butuh
  metrik kepatuhan deadline, derive dari `MailResponseTime` (selisih
  original ↔ reply, sudah ada di backend) + `m_max_response_date`.

### Subject Auto-Prefix (child Mail)
Data legacy 1.8M baris: subject anak auto-prefix **`Fwd: `** untuk
semua tipe child (reply, forward, disposisi). Memori
`legacy-disposisi-no-completion-flag` mengkonfirmasi ini.

**Aturan FE:** pertahankan konvensi legacy — **semua child Mail dapat
prefix `Fwd: `** di subject (regardless of CirculationType: REPLY /
FORWARD / DISPOSISI / CC).

Logic compose:
- Cek subject parent, kalau belum diawali `Fwd: ` (case-insensitive)
  → prepend `Fwd: ` di subject child.
- Kalau sudah → biarkan apa adanya (jangan double-prefix `Fwd: Fwd:`).
- User boleh edit subject manual di compose form sebelum kirim.

Rasional:
- **Konsistensi historis.** Mixing `Re:` (baru) dengan `Fwd:` (lama)
  dalam satu thread bikin view membingungkan; lebih baik satu konvensi
  untuk seluruh data set.
- Migrasi project, bukan greenfield — selaras prinsip "skema & semantik
  historis preserved".
- Backend zero-change (logic compose ada di FE / service layer
  existing).

**Catatan FE (display vs storage):**
- Yang **disimpan** ke backend tetap `Fwd: <subject>`.
- FE boleh **render** label berbeda di list view (mis. icon ↩ untuk
  REPLY, → untuk FORWARD/DISPOSISI berdasar `circulationType`) tanpa
  mengubah string subject.

### Recipient Picker — Scope per CirculationType
Aplikasi yang sedang berjalan memberlakukan **scope picker yang
berbeda** sesuai `CirculationType` yang dipilih sender. Migrasi FE
**harus pertahankan rule ini** karena mencerminkan kultur disposisi
berjenjang & pola komunikasi formal organisasi.

| CirculationType   | Scope kandidat recipient                                              |
|-------------------|------------------------------------------------------------------------|
| DISPOSISI (1)     | **Bawahan langsung + turunannya** (subordinate tree dari sender)       |
| MEMO_MANDIRI (2)  | **Atasan langsung saja** (mis. Direktur Umum → Direktur Utama)         |
| MEMO (3)          | **Pegawai setara jabatan + bawahan** (peer-level + subordinate tree)   |
| CC (4)            | **Semua pegawai** (tanpa batas hirarki)                                |
| REPLY (5)         | (otomatis ke pengirim parent — bukan picker manual)                    |
| FORWARD (6)       | (mengikuti DISPOSISI atau MEMO sesuai konteks)                         |

**Mekanisme FE:**
1. Sender pilih `CirculationType` di compose form **lebih dulu**.
2. FE call HR service: resolve scope sesuai tabel di atas dari
   posisi/jabatan sender (lihat `integration/hr/`).
3. Picker autocomplete hanya tampilkan kandidat di scope itu.
4. Saat submit, fan-out ke `mail_recipient` rows: **1 row per
   pegawai resolved** (`user_id` final, `recipient_type =
   circulationType`).

Rasional:
- **Disposisi top-down only** — tidak boleh memberi disposisi ke peer
  atau atasan (memo mandiri / memo dipakai untuk itu).
- **Memo Mandiri** = laporan personal ke atasan langsung (mis. progress
  report, izin) — scope sengaja sempit.
- **Memo** = komunikasi horizontal & instruksi (peer + bawahan), mis.
  antar Manajer kolaborasi atau direksi memberi arahan.
- **CC** = info copy bebas, akhirnya bisa siapa pun.
- **Backend zero-change.** Yang disimpan tetap `mail_recipient.user_id`
  resolved — scope filtering murni di FE compose flow + HR lookup.

**Konsekuensi:**
- FE butuh dua HR endpoint utama: (a) **subordinate tree** dari posisi
  X (rekursif untuk DISPOSISI/MEMO), (b) **direct supervisor** dari
  posisi X (untuk MEMO_MANDIRI), (c) **peer list** (jabatan setara,
  untuk MEMO). Cek ketersediaan di `integration/hr/` — kalau belum
  ada, tambah method di Feign client.
- Compose UI menampilkan helper text per CirculationType supaya
  sender tahu kenapa kandidat list-nya terbatas.
- Mutasi pegawai tidak retroaktif: sekali kirim, recipient resolved
  ke `user_id` saat itu — kalau yang bersangkutan pindah, surat
  tetap di MailEntry-nya (akuntabilitas individu).

### MailArchive (Arsip)
Dokumen internal hasil arsip dari `Mail` yang sudah selesai diproses
(`ma_ref_id` → `mail.m_id`). Akses **dibatasi** per `orgCode`/`officeCode`
melalui `MailArchiveAccess`. Bisa ditandai rahasia (`secretType`). Punya
`keyword` untuk index pencarian internal.

Lifecycle: `DRAFT → ARCHIVED → DELETED`. Saat di-publish (archive), nomor
arsip di-generate dan `MailArchiveNotif` dikirim ke daftar akses.

Audience: pegawai internal yang punya hak akses unit/jabatan tertentu.

### MailArchive — Format Nomor (`ma_no`)
Pemeriksaan data legacy `smartoffice.mail_archive` (40k baris,
2016–2025): format `ma_no` **terpisah** dari `m_no` (Mail) dan
**dua pola koeksis**.

**Pola aktual (per data):**
| Pola                            | Contoh              | Volume |
|---------------------------------|---------------------|--------|
| `<kategori>/<seq>/<year>`       | `027/1524/2025`     | ~24.7k (62%) |
| `<kategori>/<roman>/<seq>/<year>` | `692.1/I/0406/2025` | ~14.9k (37%) |
| Lain (legacy 2016–2017 noise)   | `045-AJB/I/0001/2016`, `/1463/2016` | ~210 (<1%) |

Distribusi tidak per-unit absolut — banyak unit pakai **kedua** pola
(mis. `BA3` punya 20k cat/seq/year + 10k cat/roman/seq/year). Kedua
pola koeksis di tahun yang sama (mis. 2025: 1533 vs 1010). Bukan
"unit X selalu pola Y".

**Komponen:**
- `<kategori>` = `ma_mcat_code` (mis. `027`, `692.1`, `842.3`,
  `423`) — kategori arsip dinas (klasifikasi kearsipan
  pemerintahan).
- `<seq>` = sequence numerik, **bertambah global per tahun per
  pola** (bukan per kategori). Bukti: `692.1/1524/2025` lalu
  `050/1523/2025` lalu `692.1/1522/2025` — seq berurut lintas
  kategori.
- `<roman>` = bulan dalam angka Romawi (I..XII) — opsional,
  hanya pada pola kedua.
- `<year>` = 4-digit tahun arsip.

**Format `m_no` Mail (untuk perbandingan, BERBEDA):**
- Pola: `<seq>/<sub>/<unit>-<sub2>/<roman_month>/<year>`
  (mis. `22732/00/485.1-I/IV/2026`).
- Strategi numbering Mail (`Default/BMS/SMD/BPN`) **tidak applicable**
  ke arsip — komponen, separator, dan urutan beda.

**Aturan numbering arsip (turun dari data):**
- **Strategi terpisah dari Mail.** Tambah `MailArchiveNumberingStrategy`
  baru (atau service khusus di `service/numbering/archive/`),
  jangan pakai `MailNumberGeneratorDelegator` Mail.
- **Dua pola dipertahankan.** Tidak konsolidasi ke satu format —
  data historis 40k baris pakai dua, dan migrasi tidak boleh
  menulis ulang nomor lama. Kedua pola harus didukung untuk
  arsip baru juga (admin pilih saat klik "Arsipkan", default
  ke pola yang lebih banyak dipakai unit-nya).
- **Sequence resolution:** `SELECT FOR UPDATE` per (year, pola)
  untuk hindari race; **bukan** per (kategori, year). Bukti seq
  global lintas kategori.
- **Validasi unik:** `ma_no` tidak unique di tabel legacy (no
  unique constraint), tapi prakteknya unik per (year, pola).
  Tetap enforce uniqueness di service layer untuk arsip baru.

**Konsekuensi:**
- FE form "Arsipkan": **dropdown pilih pola manual** setiap kali
  arsip (admin/sekretariat yang menentukan). Tidak ada
  default-per-unit / default-per-kategori — bukti data: banyak
  unit & kategori pakai dua pola tanpa konsistensi → preset
  otomatis akan salah tebak.
- FE menampilkan kedua opsi side-by-side dengan **preview nomor
  yang akan di-generate** (tahun berjalan + seq berikutnya per
  pola) supaya admin bisa lihat hasilnya sebelum submit.
- Tidak ada validasi "pola wajib X untuk kategori Y" — pure
  pilihan admin, mengikuti data legacy yang permissive.
- Migration data legacy: `ma_no` lama disalin **as-is**, tidak
  re-format.
- Strategi `Default/BMS/SMD/BPN` Mail tidak diutak-atik —
  numbering arsip independen.

### Numbering — Mismatch antara kode existing dan data legacy
Verifikasi langsung ke `smartoffice` (1.8M `mail` + 40k `mail_archive`)
mengungkap **4 mismatch besar** antara implementasi numbering yang
lama dengan kondisi data sebenarnya. Isu ini telah diselesaikan
melalui beads `mail-service-843` (P1).

**Resolusi:**
1. **`AbstractMailNumberGenerator.getNextSequence()`** direfactor menggunakan `MAX(CAST(SUBSTRING_INDEX(m_no, '/', 1) AS UNSIGNED))` dengan menghapus filter `m_status`.
2. **`DefaultArchiveNumberGenerator`** direfactor menggunakan delegator / strategy pattern dengan 2 generator spesifik (`ShortArchiveNumberGenerator` dan `LongRomanArchiveNumberGenerator`). Sequence disimpan pada tabel terpisah `mail_archive_seq` yang mensupport `SELECT FOR UPDATE` (menghindari race conditions).
3. **Pilihan Pola** dikirimkan manual melalui parameter `pattern` pada saat `publishArchive` (misal: "SHORT", "LONG_ROMAN").
4. **Parser Utilitas** (`ArchiveNumberParserUtil`) ditambahkan dan dites untuk mendukung backfill/validasi seq di kemudian hari.

**Konsekuensi migrasi data:**
- Tambah migrasi Flyway `Vxx__create_mail_archive_seq.sql` +
  backfill statement (`INSERT … SELECT MAX(parsed_seq) GROUP BY
  year, pattern FROM mail_archive`).
- Sama untuk Mail kalau scope memang global per tahun (perlu
  konfirmasi tambahan: cek 2-3 tahun data Mail apakah seq tidak
  reset per kategori secara konsisten).
- Implementasi parser `ma_no` regex untuk dua pola — kalau
  parsing gagal (legacy 2016–2017 noise), skip dari backfill,
  ambil max dari yang sukses.
- Re-pakai existing `DefaultArchiveNumberGenerator` sebagai
  fallback hanya untuk transition; remove setelah dual-strategy
  GA.

**Open question — TERVERIFIKASI (2026-05-05):**
- **Mail seq scope** = **per-(YEAR, m_category)**, BUKAN global.
  Bukti agregat 2025: `cat=1` max=338730, `cat=86` max=22460,
  `cat=595` max=1480, `cat=199` max=1747, dan banyak kategori
  mulai dari `min_seq=1`. Generator existing yang sudah memfilter
  `m_category` SUDAH BENAR scope-nya — yang perlu diperbaiki cuma
  `COUNT(*) → MAX(parsed_seq)` dan ganti `YEAR(m_created_date)=?`
  jadi range `BETWEEN '<yr>-01-01' AND '<yr+1>-01-01'` agar
  sargable. Tabel counter `mail_seq` jadi key composite
  `(year, category, last_seq)`.
- **Mail tidak punya kolom office/unit** — hanya `m_created_by`
  (user FK). Scope office tidak relevan untuk Mail seq.
- **`MailStatus` enum** = `DRAFT(0)`, `SENT(1)`. Filter
  `m_status=1` di `AbstractMailNumberGenerator` BENAR — exclude
  draft, hitung yang terkirim.
- **`ArchiveStatus` enum** = `DRAFT(1)`, `ARCHIVED(2)`,
  `DELETED(3)`. Cocok dengan distribusi legacy (NULL=1, 1=2188,
  2=37559, 3=145). `@SQLRestriction("ma_status != 3")` di entity
  sudah benar untuk soft delete.

### MailArchive — Trigger (kapan Mail "naik" jadi Arsip)
Aplikasi berjalan: **manual oleh petugas admin / sekretariat unit**.
Migrasi pertahankan rule ini.

**Aturan:**
- Mail tidak otomatis menjadi MailArchive. Tidak ada job batch / event
  listener yang meng-arsipkan thread "selesai".
- Petugas admin/sekretariat membuka Mail (biasanya root atau salah
  satu node thread), klik aksi **"Arsipkan"**, lalu mengisi metadata
  arsip: `mailCategory`, `secretType`, `keyword`, daftar
  `MailArchiveAccess` (orgCode/officeCode yang berhak), nomor arsip
  (di-generate strategi numbering arsip).
- `MailArchive.ma_ref_id` = `Mail.m_id` node yang dipilih sebagai
  representasi arsip (umumnya root, tapi tidak dipaksa).
- Setelah submit, lifecycle `DRAFT → ARCHIVED`, `MailArchiveNotif`
  fan-out ke daftar akses (lihat **MailArchive (Arsip)**).

Rasional:
- **Tidak ada completion flag** di backend (memori
  `legacy-disposisi-no-completion-flag`). "Selesai" emergent dari
  traversal thread — tidak akurat untuk auto-trigger.
- Arsip = **keputusan kuratorial** (pilih yang layak jadi arsip
  resmi unit + akses + metadata + level kerahasiaan). Bukan output
  otomatis dari thread state.
- Owner arsip = **admin/sekretariat unit**, bukan leaf user. Kultur
  tata kelola dokumen pemerintahan: arsip resmi keluar atas
  pengetahuan bagian umum/sekretariat, bukan staf level operasional.
- Backend zero-change: endpoint create MailArchive sudah ada;
  trigger otomatis tidak ditambah.

**Konsekuensi:**
- FE perlu role check: aksi "Arsipkan" hanya muncul untuk user
  ber-role admin/sekretariat (cek via `MailPrincipal` /
  `@PreAuthorize`).
- Tidak ada SLA / reminder otomatis "thread ini layak diarsipkan".
  Kalau butuh bantuan kurasi, buat **dashboard daftar Mail kandidat
  arsip** (mis. root mail dengan child terbaru > N hari, tidak ada
  follow-up baru) sebagai derive view, bukan auto-archive.
- Satu Mail thread bisa menghasilkan **0, 1, atau lebih** MailArchive
  (mis. cabang thread yang substansinya beda — admin boleh arsip
  dua kali dengan `ma_ref_id` berbeda). Tidak ada constraint unique
  di backend.

### Publication (Publikasi / Area Publik)
Dokumen yang dipublikasikan ke **area publik** internal (semua pegawai
login). File berdiri sendiri (tidak ter-link ke `Mail`), dengan
`documentType` sebagai jenis dokumen (bukan kategori surat).

Lifecycle: `DRAFT → PUBLISHED → DELETED`. Tidak ada access list — terbuka
untuk semua user yang login.

Audience: semua pegawai. Bukan eksternal di luar organisasi.

**Beda Arsip vs Publikasi:**
- Arsip terkait Mail; Publikasi independen.
- Arsip akses dibatasi per unit; Publikasi terbuka.
- Arsip pakai `MailCategory`; Publikasi pakai `DocumentType`.
- Arsip bisa rahasia; Publikasi tidak ada konsep rahasia.

### MailCategory vs DocumentType — Boundary & Tata Kelola
Dua master klasifikasi dokumen yang **terpisah penuh** secara semantik
dan referensi. Tidak ada overlap.

**MailCategory (`mail_category`)**
- Klasifikasi formal **Tata Naskah Dinas** untuk Mail & MailArchive.
- Field: `code` (kode formal, mis. "485.1"), `name`, `sort`,
  `mailType` FK (Internal/Masuk/Keluar), unique
  `(mail_type_id, mcat_code)`.
- Status: `ENABLED / DISABLED / DELETED`.
- Dipakai di **numbering** (`#m_cat#` placeholder) dan filter
  search/folder.
- Referenced by: `Mail.m_category`, `MailArchive.ma_mcat_id` +
  `ma_mcat_type` + `ma_mcat_code`.

**DocumentType (`jenis_dokumen`)**
- Tag jenis dokumen Publikasi (mis. "Pengumuman", "SOP", "Notulen").
- Field: `name` saja. **Tidak ada code, tidak ada parent type**.
- Status: `ACTIVE/INACTIVE` + `is_deleted` boolean.
- **Bukan** input numbering — Publication tidak punya nomor formal.
- Referenced by: `Publication.documentType`.

**Aturan boundary:**
- Mail / MailArchive **tidak pakai** DocumentType.
- Publication **tidak pakai** MailCategory.
- Tidak ada plan konsolidasi merge dua master jadi satu.

**MailType (`mail_type`)**
- Enum master 3 baris: 1=Internal, 2=Masuk, 3=Keluar.
- Tidak punya kolom code; **inisial tipe untuk numbering = huruf
  pertama nama** (`I`/`M`/`K`).
- Distribusi data legacy (mail_archive): Keluar 24757, Masuk 11495,
  Internal 3495.

**Numbering placeholder verifikasi data legacy 2025**
Template tenant BMS = `#seq#/#org_code#/#m_cat#-#type#/#MR#/#YYYY#`.
Sample real `m_no`:
- Internal: `22460/00/485.1-I/X/2025` → `#type#=I`, `#MR#=X` (Oktober).
- Masuk: `049/1421002/SEKRE-M/VII/2025-690` → `#type#=M`, `#MR#=VII`.

Resolusi placeholder yang harus didukung generator:
- `#seq#` = nomor urut per (year, m_category) — sudah benar di
  843.
- `#org_code#` = `tenantConfig.officeCode()` — sudah benar.
- `#m_cat#` = `MailCategory.code` — sudah benar.
- `#type#` = `mail.getMailType().getName().substring(0,1).toUpperCase()`
  — **belum diimplementasi**, ditracking di beads `mail-service-s31`.
- `#MR#` = roman of `MONTH(m_created_date)` (I-XII) —
  **belum diimplementasi**, masih literal "MR" di kode existing.
  Ditracking di beads `mail-service-s31`.
- `#YYYY#`, `#MM#` = tahun/bulan dari `m_created_date` — sudah benar.

**Validasi MailCategory di MailArchive**
- Backend **permissive**: semua kategori bisa dipakai untuk arsip
  (sesuai data legacy — semua 3 tipe ter-arsip dengan distribusi
  ~55-60% kategori benar-benar dipakai, sisanya tidak ada surat-nya).
- FE optional filter dropdown by context (mis. saat arsip surat
  masuk, tampilkan hanya kategori MailType=Masuk). Bukan validasi
  backend.

**Lifecycle master**
- **DISABLE** (`mcat_status=DISABLED` / `status_new=INACTIVE`):
  filter dari dropdown form create-baru/arsip-baru. Mail/Archive/
  Publication lama tetap render kategori-nya normal (history kekal).
- **DELETE** (`mcat_status=DELETED` / `is_deleted=1`): backend
  cek FK referensi sebelum hapus. Jika ada referensi → return
  HTTP 409 dengan pesan "masih dipakai oleh N record". Tidak ada
  cascade re-assign.

**Publication — minimal fields**
- `title`, `content`, `documentType` (FK), tanggal terbit.
- **Tidak ada nomor formal**. Tidak butuh numbering generator.

### Search — Cakupan & Strategi Indexing
Pencarian **scope per modul**: search bar di Inbox cari Mail saja,
di MailArchive cari arsip saja. Tidak ada satu kotak global lintas
modul — user pindah modul untuk ganti scope.

**Mail search (Inbox/Sent/Draft/Archive folder)**
- Field text: `m_no` (exact/prefix), `m_subject`, `m_content`,
  pengirim & external metadata (`m_created_by_name`,
  `m_no_surat_masuk`, `m_asal_surat_masuk`).
- Date sebagai **filter range terpisah** (`m_date` & `m_created_date`),
  bukan token di search bar.
- Filter sidebar: date range, read/unread (via `user_task.read_status`),
  kategori (`m_category`) & tipe (`m_type`).
- **Authorization**: filter via `user_task` user. Search join:
  `SELECT m.* FROM mail m JOIN user_task ut ON ut.mail_id=m.m_id
  WHERE ut.user_id=? AND MATCH(...) AGAINST(...)`.

**MailArchive search**
- Field text: `ma_no`, `ma_subject`, `ma_keyword` (TEXT khusus search),
  `ma_content`, `ma_sent_to`, `ma_ref_no`.
- Filter sidebar: date range (`ma_archive_date` / `ma_mail_date`),
  unit (`ma_org_id`) + kategori (`ma_mcat_id`), lokasi fisik
  (`ma_loc_building/floor/room/rack/tier/box`), klasifikasi rahasia
  (`ma_secret_type`).
- **Authorization hybrid**: `WHERE (ma_org_id = user.org_id) OR
  EXISTS (SELECT 1 FROM mail_archive_access WHERE archive_id=ma_id
  AND user_id=?)`. Anggota unit lihat arsip unit-nya; non-anggota
  butuh grant explicit.

**Publication search**
- **Tidak disediakan** — chronological feed saja. Volume kecil,
  user scroll. Filter date di FE cukup.

**Indexing strategy (MVP)**
- **MariaDB FULLTEXT** native. Index:
  - `mail`: `FULLTEXT(m_subject, m_content)` + B-tree biasa untuk
    `m_no`, `m_no_surat_masuk`, `m_asal_surat_masuk`,
    `m_created_by_name`.
  - `mail_archive`: `FULLTEXT(ma_subject, ma_keyword, ma_content)` +
    B-tree untuk `ma_no`, `ma_ref_no`, `ma_sent_to`.
- Query pakai `MATCH ... AGAINST(... IN BOOLEAN MODE)` untuk text
  fields, plus exact predicate untuk `m_no`/`ma_no`.
- Tidak ada Indonesian stemmer / synonym di MVP — diterima sebagai
  trade-off agar aplikasi tidak terlalu rumit.

**Roadmap engine eksternal (NOT NOW)**
- Elasticsearch / OpenSearch direncanakan untuk fase berikutnya
  (fuzzy match, ranking lebih baik, highlight, possibly attachment
  full-text). **Tidak diimplementasi sekarang.**
- Persiapan: bungkus akses pencarian di interface
  `MailSearchService` / `MailArchiveSearchService` (CQRS Query side
  saja). Implementasi MVP = JOOQ + FULLTEXT; nanti swap ke
  ElasticsearchSearchService tanpa mengubah caller.
- Sync strategy ditentukan saat implementasi (event listener
  on-write atau Debezium CDC) — out of scope sekarang.

**Yang belum di-search di MVP**
- Attachment full-text (OCR/PDF parse) — tidak diimplementasi.
- Cross-module global search — tidak ada.
- Saved search / search history — tidak ada.

**Open question (untuk fase Elasticsearch nanti):**
- Sync lag yang dapat diterima (real-time vs eventual ≤5s)?
- Index lifecycle: rebuild full vs incremental?
- Highlight di hasil search wajib atau cukup nice-to-have?

### Personal Folder Management — Tata Kelola

Personal folder = folder yang dibuat user untuk mengorganisir mail-nya sendiri,
disimpan di tabel `mail_folder` dengan `owner_id > 0`. Bedakan dari **system
folder** (`owner_id = 0`, hardcoded di enum `SystemFolder`).

**Hierarki**
- Root personal: `SystemFolder.PERSONAL_ROOT` (id=10, owner_id=0).
- Top-level personal folder: `parent_folder_id = 10`.
- Sub-folder: `parent_folder_id = <folder_id personal lain milik owner yang sama>`.
- **Cap depth = 3** (dihitung dari PERSONAL_ROOT sebagai depth=0; folder
  langsung di bawahnya = depth 1; max anak = depth 3). Sesuai pola legacy
  (max yang teramati di DB legacy: depth 3, mis. `2020 / spk / pengadaan`).
  Backend reject create/move bila melewati cap (HTTP 400).

**Validasi nama**
- Trim whitespace, max 45 char (sesuai kolom DB `folder_name VARCHAR(45)`).
- **Unique per (owner_id, parent_folder_id)** — tidak boleh ada 2 folder
  dengan nama sama dalam parent yang sama untuk satu user. Dicek pakai
  case-insensitive compare. Kalau bentrok → HTTP 409.
- Karakter: izinkan huruf/angka/spasi/`_`/`-`/`.`/`/` (slash perlu kalau
  user mau imitasi path seperti `2024 / Q1`).

**Lifecycle**
- **Create**: validasi nama unique, validasi parent valid (parent harus
  milik owner sama atau PERSONAL_ROOT), validasi depth cap, set
  `folder_status=1`, `folder_created_date=NOW()`.
- **Rename**: hanya owner. Re-validasi unique nama dalam parent.
- **Delete**: **block delete jika folder berisi mail ATAU memiliki child folder**
  → HTTP 409 dengan pesan "folder masih berisi mail/subfolder, kosongkan dulu".
  User harus rapikan dari leaf ke root. Tidak ada cascade. Setelah aman,
  soft-delete (`folder_status=3`).

**Auto-rules / filter** — **TIDAK ADA di MVP.** User pindah mail manual
via UI. Tidak ada rule engine "kalau pengirim X masuk folder Y". Sederhana
dan sesuai legacy.

**Move mail ke personal folder**
- Source folder yang diizinkan: hanya **INBOX(2)** dan **READ(4)**.
  Draft, Sent, Deleted, Purged tidak boleh di-pindah ke personal folder.
  Sent items immutable di folder Sent (it's sent metadata).
- Backend: `UPDATE user_task SET folder_id=<personal_folder_id> WHERE
  user_id=? AND mail_id IN (...)`. Mail satu-folder-per-user (single
  membership), tidak ada label-style.
- Validasi: target folder harus milik user yang sama (`owner_id` cocok).

**System vs Personal — perbedaan kapabilitas**

| Operasi          | System Folder | Personal Folder |
|------------------|---------------|-----------------|
| Create           | ❌ (hardcoded) | ✅ (cap depth=3) |
| Rename           | ❌            | ✅              |
| Delete           | ❌            | ✅ (block jika tidak kosong) |
| Move target      | INBOX→READ→DELETED via aksi eksplisit | ✅ dari INBOX/READ saja |
| Tampil di tree   | ✅            | ✅              |
| Counter badge    | ✅ (semua kecuali ROOT/PERSONAL_ROOT/PURGED) | ✅ |

**Open question (tindak lanjut)**
- Migrasi: 609 folder dengan `folder_status=3` di legacy (deleted) — apakah
  di-skip total atau tetap di-migrasi sebagai DELETED untuk audit history?
- Mail orphan di legacy: ada `user_task.folder_id` yang menunjuk ke folder
  deleted? Perlu data fix sebelum import.
- Counter badge personal folder: total saja, atau juga unread (semantik
  unread untuk personal folder belum jelas — biasanya unread = INBOX-only)?

### Role-in-context UX — Tata Kelola

**Asumsi inti (klarifikasi user)**: Tidak ada jabatan rangkap. 1 user = 1
posisi aktif pada saat tertentu. Plt = jabatan lama dikosongkan, user
diberi posisi plt sebagai satu-satunya posisi aktif.

**Validasi data legacy**:
- `sys_user.user_role_id` — single kolom (1 role per user).
- `employee.emp_pos_id` — single kolom (1 position per employee).
- Tidak ada tabel join `employee_position` / `user_role` (M:N).
- `mail_recipient` 100% terisi (`user_id`, `emp_id`, `pos_id`, `pos_name`)
  pada 2.24M baris → snapshot per-recipient sudah disediakan legacy.

**Model role aktif**
- Backend: `MailPrincipal.activePosId` di-resolve dari `employee.emp_pos_id`
  saat JWT divalidasi (via HR integration / cache `hrEmployee` 60m).
- Tidak ada role-switcher UI. Tidak ada header `X-Active-Role-Id`. Role
  aktif = posisi terkini employee (single source of truth: HR).
- Saat mutasi/promosi (HR update `emp_pos_id`), cache invalidation
  `hrEmployee::{empId}` triggered. Sesi user lanjut tanpa re-login (read-
  through cache). Mail lama tidak di-rewrite.

**Resolusi posisi per aksi (auto dari konteks, TANPA snapshot kolom baru)**
- **Compose / kirim mail**: hanya simpan `m_created_by` (user id) +
  `m_created_by_name` (nama). Posisi pengirim tidak di-snapshot di mail.
- **Disposisi (forward)**: child mail tetap hanya simpan `m_created_by`.
  Recipient row child di-isi `mail_recipient.pos_id` + `pos_name`
  snapshot dari target saat itu (kolom sudah ada).
- **Numbering scope**: numbering generator resolve runtime
  `mail.created_by → employee.emp_pos_id → position.pos_org_id` untuk
  scope per-unit (BMS/SMD/BPN). Karena resolve via posisi terkini,
  numbering yang sudah-jadi tidak ter-pengaruh (mail_number kolom tetap).
- **Signature**: signature pejabat di-pick runtime dari posisi aktif
  user saat sign. Mail history menampilkan signature image yg di-attach
  saat itu (signature di-render ke PDF/blob → blob immutable). Jadi
  walau user mutasi, signature di blob mail lama tetap historis.
- **Approval / read-status**: `user_task` di-resolve via `user_id` saja
  (1 user = 1 posisi aktif).

**Tampilan history (asimetris: recipient snapshot, sender live)**
- **Recipient side (snapshot — by legacy schema)**: setiap row mail
  recipient menampilkan `pos_name` apa adanya dari
  `mail_recipient.pos_name` (varchar 64 sudah snapshot legacy). Mail
  lama tetap bertuliskan "Staf Sub Bag Pengadaan" walau user recipient
  kini Manajer. ✅ History terjaga untuk recipient.
- **Sender side (live — by legacy schema)**: tabel `mail` hanya punya
  `m_created_by_name` (nama orang) — TIDAK punya pos snapshot. Posisi
  pengirim di-resolve runtime via `m_created_by → employee → position`.
  Saat sender dimutasi, posisi sender di mail lama ikut berubah.
  ⚠️ Konsekuensi yang diterima karena schema tidak boleh berubah.
- Mitigasi: jika audit posisi sender historis dibutuhkan kelak, pakai
  `snapshot_employee` / `snapshot_position` legacy (sudah ada — perlu
  audit logic, bukan schema change). Tidak di-MVP.
- Tidak ada tooltip "sekarang: ..." di MVP.

**Implikasi entitas Mail (write path) — TANPA SCHEMA CHANGE**
- Hard constraint: tabel `mail` legacy TIDAK punya kolom sender pos/org
  snapshot. Tidak boleh ditambah supaya migrasi 1.8M baris aman.
- Sender role HANYA di-resolve runtime saat read:
  `mail.m_created_by → employee.emp_pos_id (current) → position.pos_name`.
  Dengan kata lain: sender role TIDAK snapshot — ini konsekuensi yg
  diterima.
- Konsisten dengan legacy: legacy juga tidak snapshot sender role; saat
  user dimutasi, tampilan sender di mail lama akan ikut posisi baru.
  Field yang TETAP snapshot: `m_created_by_name` (varchar 64) — minimal
  nama orang tetap historis.
- Saat add recipient: `mail_recipient.pos_id` + `pos_name` SUDAH snapshot
  by legacy schema (kolom sudah ada). Tetap dipakai apa adanya — ini
  yang jadi sumber tampilan "sebagai: <pos_name>" di history.
- Saat disposisi forward: child mail tetap pakai `m_created_by` saja;
  posisi forwarder tampil via resolve runtime, bukan snapshot.

**Capability per posisi (akses fitur)**
- Tidak ada permission matrix per posisi di MVP. Semua user dapat:
  compose, kirim, terima, disposisi, archive view (kecuali yang
  dibatasi `MailArchiveAccess`).
- Pejabat (signature) dibedakan via flag `employee.has_signature` atau
  master `signature_pejabat` (lookup runtime), bukan capability matrix.

**Edge case: user dimutasi saat ada draft / mail in-flight**
- Draft (status=0): ketika user mutasi, draft tetap milik user; saat dia
  kirim, snapshot pos_name = posisi BARU. Diterima karena draft = belum
  terkirim.
- Mail sudah terkirim (status=1): immutable. Snapshot lama tetap.
- Disposisi yang ditujukan ke user di posisi LAMA: tetap di Inbox user
  (karena user_task.user_id matched), badge pos_name = posisi lama
  recipient row. User tetap bisa proses (forward / archive) — atasan
  baru-nya tidak otomatis terima takeover. Manual handover jika perlu.

**Konsekuensi diterima (no schema change)**
- Sender role di mail history TIDAK historis. Kalau user dimutasi,
  posisi sender di mail lama berubah. Mitigasi: nama orang
  (`m_created_by_name`) tetap snapshot, sehingga "siapa" terjaga
  walau "sebagai apa" tidak.
- Recipient role di mail history HISTORIS (snapshot). Asimetri ini
  diterima sebagai trade-off zero-migration-risk.
- FE harus tampilkan label sender sebagai "Nama Orang" (snapshot) +
  optional "(posisi terkini)" runtime — bukan "(posisi saat kirim)".

**Open question (tindak lanjut)**
- [RESOLVED] Plt: menggunakan Opsi 1 (direct update `employee.emp_pos_id`). Lihat `docs/adr/001-plt-model-representation.md`.
- Mutasi inflight: apakah perlu notifikasi otomatis ke atasan baru saat
  ada mail belum-diproses milik user yang dimutasi? Tidak di MVP, tapi
  worth tracked.
- Cache invalidation HR: HR service harus emit event `EmployeePositionChanged`
  ke mail-service (via webhook / message bus) supaya `hrEmployee` cache
  invalidate. Saat ini hanya TTL 60m → ada window inkonsistensi 60m.
- Audit historis sender: jika legal/audit minta "siapa kirim sebagai
  apa di tanggal X", pakai `snapshot_employee` / `snapshot_position`
  legacy + reconstruct via timestamp `mail.created_date`. Tidak di MVP.

### MailArchiveAccess / MailArchiveNotif Fan-out

**Data legacy** (`smartoffice@192.168.230.84:3307`):
- `mail_archive`: 39893 row. Status: 1=DRAFT(2188), 2=ARCHIVED(37559),
  3=DELETED(145).
- `mail_archive_access`: 119619 row, 37612 archive, 89 distinct pos_id.
  Granular flag (access/download/history). Distribusi:
  - (1,1,1): 119579 row = 99.97% full access.
  - (0,1,1): 30 row — anomali legacy (kemungkinan data error: view
    forbidden tapi download allowed).
  - (1,1,0): 7 row — real use case (hide audit history).
  - (1,0,1): 2 row — real use case (view-only, no download).
  - (0,0,1): 1 row — only history.
- `mail_archive_notif`: 40922 row, 37578 archive, 100% `notif_flag=1`.
  Per-archive flag (apakah notif sudah diproses), bukan recipient list.
- `mail_archive_notif_log`: 336560 row, 37269 archive, 186 user.
  Avg ~9 user per archive. Ini adalah fan-out actual ke user.

**Model akses**
- ACL granted ke `pos_id` (bukan user_id). 89 posisi di legacy.
- User akses archive: resolve `user.activePosId` runtime → cek
  `mail_archive_access WHERE mail_archive_id=? AND pos_id=?`.
- Single-position assumption (1 user = 1 posisi) cocok dengan model ini.
- TIDAK include ancestors (org tree). Legacy 99.97% full access →
  cascading tidak diperlukan untuk MVP.

**Enforce flag (backend, defense in depth)**
- 3 flag dipisah karena legacy punya 40 row granular (real use case).
- Endpoint mapping:
  - `GET /archives/{id}` → cek `access=1`. HTTP 403 jika tidak.
  - `GET /archives/{id}/download` atau `/attachments/*` → cek
    `download=1`.
  - `GET /archives/{id}/history` (audit trail / log perubahan) → cek
    `history=1`.
- Implementasi: `MailArchiveAccessChecker` service dipanggil dari
  `@PreAuthorize` SpEL atau manual check di controller. Cache per-
  request (sama archive sering dicek 2-3x dalam 1 req chain).
- FE hide button untuk flag yg 0 (UX), tapi BE tetap enforce (security).

**Fan-out notif (publish flow eksplisit)**
- Step 1 (sync, command service): user buat arsip → save `mail_archive`
  (status DRAFT=1 atau langsung ARCHIVED=2).
- Step 2 (sync, command service): user pilih pos_id list (manual atau
  auto dari kategori) → save `mail_archive_access (mail_archive_id,
  pos_id, access, download, history)`. Multiple pos per archive.
- Step 3 (event publish, on commit): `ArchivePublishedEvent` di-fire
  saat status transit ke ARCHIVED=2.
- Step 4 (listener async @TransactionalEventListener+@Async):
  a. SELECT pos_id FROM `mail_archive_access` WHERE
     `mail_archive_id=?` AND `access='1'` (skip view-forbid 30 row
     anomali legacy).
  b. Untuk tiap pos_id: fetch pegawai by pos_id via HrServiceClient
     atau JOIN `employee WHERE emp_pos_id IN (...)` → list user_id.
  c. INSERT BATCH ke `mail_archive_notif_log (mail_archive_id, user_id,
     notif_date=NOW())`. Avg ~9 user/archive di legacy.
  d. UPSERT `mail_archive_notif (mail_archive_id, notif_flag=1,
     processed_date=NOW())` sebagai per-archive marker (1 row/archive).
- Schema `mail_archive_notif` (selaras legacy): kolom `id`,
  `mail_archive_id` (BIGINT, FK ke `mail_archive.ma_id`),
  `notif_flag`, `insert_date`, `processed_date`, `updated_at`.
  TIDAK ada kolom `user_id` / `notif_date` / `status` — kolom
  `user_id` + `notif_date` hanya ada di `mail_archive_notif_log`
  (fan-out per user). V1 baseline pre-2026-05-09 sempat keliru
  menyalin bentuk `_log` ke tabel ini; sudah dikoreksi.
- Implementasi saat ini (`ArchivePublishedEventListener.java`) BUGGY:
  insert ke `mail_archive_notif` dengan kolom `user_id` (kolom tidak
  ada di schema), pakai positionId langsung sebagai user_id, tidak
  insert ke `mail_archive_notif_log`. Tracked di beads issue
  `mail-service-a3s` (P1 bug).
- Soft delete archive (status 3) → tidak fan-out, tidak hapus log.
  Re-archive (3→2) edge case: cek apakah perlu re-fan-out (legacy
  tidak clear).

**Read-status notif log**
- `mail_archive_notif_log` saat ini tidak punya kolom `read_at`.
  Untuk badge "baru" di FE: query log row mana yg user belum klik
  archive-nya (cek via timestamp `notif_date` vs last view di
  `audit_trail` atau new tabel — TBD).
- MVP: tampilkan list notif by user, klik → mark as read (perlu
  schema change → ditolak constraint). Alternatif: hitung "new"
  via heuristik `notif_date > user.last_login` atau session-side.

**Edge cases**
- Pos_id berubah di mail_archive_access setelah archive published:
  user yg dulu eligible bisa hilang akses. Acceptable (snapshot tidak
  diperlukan untuk ACL).
- User dimutasi setelah dapat notif_log: tetap bisa lihat log (log
  punya user_id eksplisit), tapi saat klik archive cek pos baru →
  bisa 403. Acceptable.
- Performance: 1 archive × 9 user fan-out = INSERT 9 row. 40k archive
  total = 336k row. Tidak ada masalah skala saat ini. Future: batch
  INSERT, indeks `(user_id, notif_date)` untuk inbox query.

**Open question**
- Re-archive (status 3→2): perlu re-fan-out atau skip?
- Mark-as-read notif: schema `mail_archive_notif_log` tidak punya
  `read_at`. Mau pakai heuristik atau terima limitation?
- Bulk-grant access: ada UI admin yg set access ke banyak pos sekaligus?
  Atau hanya per-archive manual?

---

## MailResponseTime SLA — Tata Kelola

**Tujuan**
Track waktu respons antar surat (parent → child) untuk monitoring
performa unit/kategori. **Bukan** SLA enforcement — tidak ada
breach detection proaktif.

**Validasi data legacy**
- Tabel `mail_respontime` ada di legacy. Entity `MailResponseTime`
  sudah wired ke listener `MailResponseTimeListener` (dipanggil
  via `MailSentEvent`).
- `m_max_response_date` (deadline) terisi hanya 11% dari mail
  (~198k / 1.8M). 89% mail tidak punya deadline.
- Threading legacy cascading 8-10 level (root→sup→manajer→…→staf).

**Model perhitungan response time**
- Trigger: `MailSentEvent` saat reply (child) dikirim.
- Formula: `Duration.between(parent.createdDate, child.createdDate)`
  → simpan dalam detik di kolom `respon_time`.
- **First-reply-wins**: cek `findByOriginalMailId(parentId)`. Kalau
  sudah ada record → skip. Hanya child PERTAMA yang di-track.
- Forward (Fwd:) **dihitung sebagai response** (decision user). Setiap
  child = response. Tidak ada flag reply-vs-forward di legacy.
  Konsekuensi: cascading thread (root→sup→…→staf) → response time
  yg ter-record adalah delta root→sup (anak pertama), bukan root→staf.
  Acceptable.

**SLA breach detection — DROP**
- Tidak ada job/cron untuk flag mail overdue (`m_max_response_date <
  today AND no_child`).
- Alasan: 89% mail tidak punya deadline → fitur jadi noise.
- Konsekuensi: tidak ada notif eskalasi. FE tidak menampilkan
  "Overdue" badge proaktif. Operator harus query manual kalau perlu.

**Reporting SLA — dua report terpisah**

1. **Per kategori + per tipe** (stable historical)
   - Source: `mail_respontime.m_type, m_category, respon_time`.
   - Aggregat: AVG/MIN/MAX/P50/P95 response_time per (type, category).
   - Filter: range tanggal `created_at`.
   - Stable: kolom snapshot dari mail saat tracking.

2. **Per unit/posisi pengirim** (live snapshot)
   - Source: `mail_respontime.reply_m_id` → `mail.m_created_by` →
     resolve `pos_id` LIVE dari HR (employee.emp_pos_id current).
   - Bias terdokumentasi: kalau user pindah pos antara saat reply
     dan saat report dibuat → reply ter-attribute ke pos baru, bukan
     pos saat reply. **Acceptable** (sesuai konsekuensi diterima
     Role-in-context branch — sender role live, bukan snapshot).
   - Mitigasi FE: tampilkan caveat "data berdasarkan posisi user
     saat ini, bukan saat surat dikirim".

**Edge cases**
- Reply dari user yang sudah resign: pos tidak ketemu di HR →
  fallback ke "Unknown" atau skip dari report unit.
- Cascading reply (root→sup→…→staf): hanya root→sup yang masuk
  `mail_respontime` (first-reply-wins). Sup→manajer→…→staf TIDAK
  ter-track. Acceptable — fokus root response only.
- Forward sebagai delegasi (bukan response real): tetap di-count.
  Akibat: response time bisa terlalu cepat (menit) karena forward
  cuma klik tombol. Operator harus baca konteks subject ("Fwd:")
  untuk filter.
- Negative duration (clock skew / backdate): `responseSeconds < 0`
  → skip (sudah ada di listener).
- Existing record check: kalau child kedua datang setelah child
  pertama → di-skip. Tapi tidak ada UPDATE flow → kalau child
  pertama dibatalkan/dihapus, record tetap pointing ke child lama.
  Acceptable (soft-delete tidak rewind tracking).

**Konsekuensi diterima (no schema change)**
- Tidak ada flag `is_breached` atau `escalated_at` di mail.
- Tidak ada cron/scheduler baru.
- Tidak ada audit trail "pindah pos" untuk akurasi report unit.
- FE harus tampilkan disclaimer pos live di report unit.

**Open question**
- Backfill `mail_respontime` dari legacy: legacy tabel sudah ada
  data atau perlu rebuild? Cek count + skema saat migrasi data.
- Filter Forward dari report: butuh heuristik subject `LIKE 'Fwd:%'`
  atau biarkan apa adanya?
- Report range default: per bulan, per quarter, atau per tahun?

---

## Signature / Print-Verification — Tata Kelola

**Scope sekarang**: print-verification only. BUKAN crypto digital
signature (PKI/BSrE/PrivyID). Setiap kali user cetak surat,
sistem generate `auth_code` (16-char UUID hex) + simpan
`PrintLog`. QR di dokumen cetak berisi URL verifikasi
`/api/mails/verify-sign/{auth_code}` → endpoint return
{mailNumber, subject, printDate, printedBy, ipAddress}.

**Keterbatasan jaminan**: ini bukti *cetak tercatat*, BUKAN
bukti *isi tidak diubah*. Tidak ada hash konten, tidak ada
sertifikat penanda tangan. Kalau user ubah PDF setelah cetak,
QR tetap valid karena cuma point ke record cetak.

**Kemungkinan masa depan: crypto signature (BSrE/PrivyID)**
- Rencana terbuka, belum diimplementasi.
- Saat itu tiba akan butuh schema baru: `signer_id`,
  `cert_serial`, `signed_hash`, `signed_at`, `provider`
  (BSrE/PrivyID/internal), multi-signer table kalau perlu
  approval berjenjang.
- Konflik dengan no-schema-change → butuh approval eksplisit.
- API verify nanti dipisah: `/verify-print` (now) vs
  `/verify-sign` (future crypto). Atau union response dengan
  field `verificationType`.
- Jangan paksa migrasi data lama ke crypto — backward compat
  via `verificationType=PRINT_LOG_LEGACY`.

**Validasi data legacy `print_log` (192.168.230.84:3307)**
- Total: 95.740 row, range 2017-12-17 → 2025-11-07 (8 tahun).
- Unique `auth_code`: 95.739 (1 dup: `61fb4ce3c6f80` muncul
  2x — kolisi `uniqid()` PHP, deterministic-ish).
- 72.206 mail unik dicetak. Top reprint: 31x untuk 1 mail
  (mail_id=244441). Reprint normal, bukan anomali.
- `auth_code` legacy = 13-char hex (uniqid format), baru =
  16-char hex (UUID truncated).
- `ip_address` varchar(32), banyak null. IPv4 cukup, IPv6
  full (39 char) tidak muat — terima sebagai keterbatasan.

**Keputusan migrasi**

- *Backfill legacy 95k row*: **SKIP**. Dokumen cetak sebelum
  migrasi tidak bisa diverifikasi via app baru.
  - Konsekuensi: QR di kertas lama (8 tahun arsip) jadi mati.
    User yang scan akan dapat "kode tidak ditemukan".
  - Mitigasi opsional: redirect endpoint `/verify-sign/{code}`
    untuk auth_code 13-char ke "Dokumen pre-migrasi, hubungi
    arsip" (bukan invalid). Belum diputuskan, masuk open Q.
- *Schema `print_log`*: pertahankan apa adanya. Field cocok
  legacy (mail_id, auth_code, username, date, ip_address).
  Field rename `date` → `print_date` di entity (mapping
  via `@Column(name="date")`) sudah benar.
- *IP storage*: terima null + varchar(32). Tidak widen ke
  IPv6 full. New record tetap diisi via X-Forwarded-For
  chain (`AppWriteAuthFilter` proxy-aware).

**Edge cases**

- Mail dihapus (soft delete) tapi print_log masih ada:
  `verifySignature` fetch via `mailRepository.findById`
  yang ke-restrict `@SQLRestriction status != DELETED` →
  throw `EntityNotFoundException`. Saat ini mengembalikan
  500. **Open Q**: harusnya return invalid response, bukan
  exception (kasih pesan "dokumen tidak tersedia").
- Auth_code dup legacy (1 row): kalau akhirnya backfill
  diaktifkan, butuh handling: append suffix `-1`/`-2` atau
  skip duplicate.
- `getClientIpAddress()` baca `X-Forwarded-For` tanpa allow-
  list IP proxy → bisa di-spoof. Acceptable karena ip_address
  cuma untuk audit, bukan auth. Catat sebagai info, jangan
  jadi indikator otoritatif.
- Reprint counter: tidak ada limit re-print per user/per
  mail. Top legacy 31x cetak. Acceptable (kebutuhan operasi).
- Username field varchar(128) simpan `principal.name()` —
  saat user resign username masih ada di history (snapshot,
  bukan FK). Sengaja: audit log harus tetap valid post-
  resignation.

**Konsekuensi diterima**
- Tidak ada jaminan integritas isi PDF.
- QR cuma "ya/tidak ada record cetak", bukan "konten asli".
- Backfill skip → user butuh edukasi: dokumen lama tidak
  diverifikasi via QR baru.
- Tidak ada audit trail "siapa lihat verifikasi" (verify
  endpoint readonly, tidak nge-log).

**Open question**
- Behavior verify untuk auth_code 13-char (legacy format):
  invalid biasa, atau pesan khusus "pre-migrasi"?
- Endpoint verify perlu di-rate-limit? (mencegah brute-force
  enumeration auth_code).
- Mail soft-deleted + print_log ada: response invalid atau
  return mailNumber dari snapshot (perlu tambah kolom)?
  Rekomendasi: invalid + pesan netral, tidak bocor info.
- Crypto signature provider mana yang prioritas (BSrE
  punya BSSN gratis vs PrivyID berbayar)? Belum saatnya
  diputus, tapi catat untuk pemilihan vendor.

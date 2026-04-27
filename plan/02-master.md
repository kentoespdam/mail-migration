# Plan: Master Data Layer — Index

> **Urutan pengerjaan: TAHAP 2 (setelah `01-infrastructure.md` selesai)**
> File ini adalah **indeks** layer Master Data. Buka sub-file sesuai urutan di bawah.

---

## Urutan Pengerjaan Master Data

```
MailType → MailCategory → MailFolder → PesanSingkat
  2A          2B              2C            2D
```

**Alasan urutan:**
- `MailCategory` memiliki FK ke `mail_type` → **MailType harus ada lebih dulu**
- `MailFolder` bisa mulai kapan saja setelah MailType selesai, tapi dibutuhkan di Tahap 3B (Inbox)
- `PesanSingkat` tidak bergantung pada apapun → bisa dikerjakan kapan saja secara paralel

---

## Daftar Sub-File

| File                                                       | Entity       | Prioritas   |
|------------------------------------------------------------|--------------|-------------|
| [02a-master-mail-type.md](02a-master-mail-type.md)         | MailType     | **Pertama** |
| [02b-master-mail-category.md](02b-master-mail-category.md) | MailCategory | Setelah 02a |
| [02c-master-mail-folder.md](02c-master-mail-folder.md)     | MailFolder   | Setelah 02a |
| [02d-master-quick-message.md](02d-master-quick-message.md) | PesanSingkat | Kapan saja  |

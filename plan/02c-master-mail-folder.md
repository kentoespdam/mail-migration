# Plan: Master Data — MailFolder (Folder Surat)

> **Urutan: TAHAP 2C — setelah MailType dan MailCategory selesai**
> MailFolder berdiri sendiri secara FK, tapi dibutuhkan oleh semua operasi Inbox (Tahap 3B ke atas).

---

## ⚡ Wajib: Gunakan Context7

> [!IMPORTANT]
> Query Context7 sebelum implementasi:
> - `"Spring Boot REST CRUD with ownership validation"`
> - `"Spring Data JPA findByIdAndOwnerId"`
> - `"Spring Security get authenticated user id"`
> - `"Java build tree from flat list using Map"`

---

## Fungsi

Folder adalah "kotak" tempat surat diorganisir. Ada dua jenis:

| Jenis | `owner_id` | Contoh |
|---|---|---|
| Folder sistem | `0` | Inbox, Sent, Draft, Read, Deleted |
| Folder personal | `user_id` | Dibuat oleh user sendiri |

**Tabel DB:** `mail_folder`

---

## Alur Kerja: Ambil Daftar Folder (Tree)

```
GET /api/mail/folders

MailFolderService.getFolders(userId):
  1. Query: WHERE owner_id IN (0, userId) AND status = ACTIVE
            ORDER BY parent_folder_id ASC, folder_id ASC
  2. Build tree di Service:
     → Map<Long, MailFolderTreeNode> index by id
     → Iterasi: taruh setiap node di parent.children
     → Collect node tanpa parent → roots
  3. Return: List<MailFolderTreeNode>  ← nested structure
```

> [!TIP]
> Build tree menggunakan `Map<Long, node>` → O(n). Jangan gunakan rekursi bertingkat yang O(n²).

---

## Alur Kerja: Buat Folder Baru

```
POST /api/mail/folders
  Body: { name, parentFolderId }

MailFolderService.create(request, userId):
  1. Cek duplikasi: findByOwnerIdAndNameAndStatus(userId, name, ACTIVE)
     → jika ada → throw DuplicateResourceException (HTTP 409)
  2. Insert folder baru (owner_id = userId, iconCls = "email", status = ACTIVE)
  3. Return MailFolderDto
```

---

## Alur Kerja: Update Folder

```
PUT /api/mail/folders/{id}
  Body: { name }

MailFolderService.update(id, request, userId):
  1. findByIdAndOwnerId(id, userId) → 404 jika tidak ada / bukan milik user
  2. Cek duplikasi nama (exclude id ini sendiri)
  3. Update nama
  4. Return MailFolderDto
```

---

## Alur Kerja: Hapus Folder (Soft Delete)

```
DELETE /api/mail/folders/{id}

MailFolderService.delete(id, userId):
  1. findByIdAndOwnerId(id, userId) → 404 jika bukan milik user
  2. Set status = DELETED
  3. Return 204 No Content
```

---

## Endpoints

| HTTP | URL | Deskripsi |
|---|---|---|
| GET | `/api/mail/folders` | Daftar folder sebagai tree |
| POST | `/api/mail/folders` | Buat folder baru |
| PUT | `/api/mail/folders/{id}` | Update nama folder |
| DELETE | `/api/mail/folders/{id}` | Soft delete folder |

---

## Error Handling

| Kondisi | Exception | HTTP |
|---|---|---|
| Nama duplikat | `DuplicateResourceException` | 409 |
| Folder tidak ditemukan / bukan milik user | `ResourceNotFoundException` | 404 |
| Input kosong/invalid | `MethodArgumentNotValidException` | 400 |

---

## Package

```
controller/ MailFolderController.java
service/    MailFolderService.java
repository/ MailFolderRepository.java
dto/        MailFolderDto.java
            MailFolderTreeNode.java    ← nested tree DTO
```

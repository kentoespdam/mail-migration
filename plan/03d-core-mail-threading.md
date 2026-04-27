# Plan: Core Mail — Threading (Threaded View)

> **Urutan: TAHAP 3A-4 — setelah `03c-core-mail-query.md` dapat menghasilkan flat list**
> Threading adalah post-processing dari hasil query `readFolder` saat `threaded=true`.

---

## ⚡ Wajib: Gunakan Context7

> [!IMPORTANT]
> Query Context7 sebelum implementasi:
> - `"Java build tree from flat list using HashMap"`
> - `"Java recursive tree structure DTO"`
> - `"Spring Boot service unit test with JUnit 5"`

---

## Fungsi

Mengubah **flat list** surat menjadi **struktur tree bertingkat** berdasarkan relasi `m_parent_id` dan `m_root_id`. Digunakan saat user memilih mode "Threaded View" di inbox.

---

## Konsep Threading

Setiap surat memiliki dua field penting untuk threading:

| Field | Deskripsi |
|---|---|
| `m_root_id` | ID surat pertama dalam satu thread (root) |
| `m_parent_id` | ID surat yang langsung dibalas (`0` jika surat root) |

**Contoh thread:**
```
Surat A (m_id=1, root_id=1, parent_id=0)   ← root
  └─ Surat B (m_id=2, root_id=1, parent_id=1)   ← reply ke A
       └─ Surat C (m_id=3, root_id=1, parent_id=2)  ← reply ke B
  └─ Surat D (m_id=4, root_id=1, parent_id=1)   ← reply ke A juga
```

---

## Alur Kerja: Build Tree

```
MailThreadingService.buildTree(List<MailSummaryDto> flatList):

  1. Buat Map<Long, MailThreadNodeDto> nodeMap — keyed by m_id
     → Konversi setiap item ke MailThreadNodeDto
     → Set iconCls: "email-attach" jika attachmentQty > 0, else "email"

  2. Buat List<MailThreadNodeDto> roots (surat tanpa parent)

  3. Iterasi flatList:
     node = nodeMap.get(item.mId)
     parent = nodeMap.get(item.mParentId)

     if parent != null:
       → parent.children.add(node)
       → parent.leaf = false
     else:
       → Fallback: cari node di roots dengan rootId yang sama
       → Jika ketemu → taruh di children node tersebut
       → Jika tidak → taruh di roots (level tertinggi)

  4. Return roots
```

> [!IMPORTANT]
> **Pendekatan Map ini menggantikan `find_node()` rekursif dari kode lama.**
> Kode lama menggunakan rekursi tanpa explicit `return false` sehingga tidak deterministik.
> Pendekatan Map: O(n) vs O(n²) dari rekursi nested kode lama. Lebih cepat dan prediktabel.

---

## Perbandingan: Kode Lama vs Spring Boot

| Aspek | Kode Lama (CI2) | Spring Boot |
|---|---|---|
| Algoritma | Rekursi `find_node()` | Iterasi dengan `HashMap` |
| Return type | `NULL` jika tidak ketemu (bug) | Eksplisit, tidak ada ambiguitas |
| Kompleksitas | O(n²) worst case | O(n) |
| Testability | Sulit (method private di controller) | Mudah ditest sebagai unit test terpisah |

---

## DTO Structure

```
MailThreadNodeDto {
  mId          : Long
  subject      : String
  createdBy    : String
  createdDate  : LocalDateTime
  readStatus   : ReadStatus
  attachmentQty: Integer
  iconCls      : String           ← "email" atau "email-attach"
  leaf         : boolean          ← true jika tidak punya children
  expanded     : boolean          ← default false
  children     : List<MailThreadNodeDto>  ← kosong jika leaf
}
```

---

## Package

```
service/    MailThreadingService.java    ← stateless, bisa @Component biasa
dto/        MailThreadNodeDto.java
```

> [!TIP]
> `MailThreadingService` tidak perlu akses database — hanya transformasi data.
> Ini membuatnya sangat **mudah diuji** dengan JUnit 5 tanpa perlu mock repository apapun.
> Tulis unit test untuk semua edge case: parent tidak ditemukan, thread tunggal, tree dalam (>3 level).

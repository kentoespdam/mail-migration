# Fix AttachmentMapper — Error Analysis & Repair Plan

## Latar Belakang

`AttachmentMapper.java` menggunakan MapStruct dengan beberapa custom mapping.
Terdapat beberapa potensi error compile-time maupun runtime yang perlu diperbaiki.

---

## Temuan Error

### ❌ Error 1 — `entity.getId()` bertipe `Integer`, bukan `Long`

**Lokasi**: `mapAttachmentId()`, baris 30.

```java
// Entity field:
private Integer id;  // ← Integer

// Mapper:
return encoder.encode(Attachment.class, entity.getId().longValue());
```

`entity.getId()` mengembalikan `Integer`. Pemanggilan `.longValue()` valid pada `Integer`,
tapi jika `SqidsEncoder.encode()` sudah menerima `long`, ini menyebabkan **boxing NPE potensial**
jika `id` bernilai `null` (meski sudah ada null-check di baris 29). Jika signature `encode()`
hanya menerima `Long` (bukan primitive `long`), konversi ini redundan.

**Perbaikan**: Jadikan null-check lebih eksplisit dan gunakan `entity.getId().longValue()` 
hanya setelah null terkonfirmasi (sudah dilakukan, tapi perlu konfirmasi signature `encode()`).

---

### ❌ Error 2 — `getRefType()` mengembalikan `AttachmentRefType`, bukan `Integer`

**Lokasi**: `@Mapping` untuk `toResponse`, baris 20.

```java
@Mapping(target = "refType", expression = "java(entity.getRefType().getDbValue())")
```

Entity `Attachment` memiliki **field** `refType: Integer` dan **method** `getRefType(): AttachmentRefType`.
MapStruct menggunakan getter untuk membaca nilai — sehingga ia mendapatkan `AttachmentRefType` bukan `Integer`.
Target `AttachmentResponse.refType` adalah `Integer`.

Penggunaan `expression` sudah benar, **tapi** jika `refType` di-DB bernilai `null` atau tidak valid,
`fromDbValue()` bisa melempar exception saat diakses lewat `getDbValue()`.

**Perbaikan**: Tambahkan null-guard atau penanganan default di `AttachmentRefType.fromDbValue()`.

---

### ❌ Error 3 — Potensi NPE di `mapRefId()` saat `refType` null/tidak dikenal

**Lokasi**: `mapRefId()`, baris 36.

```java
if (entity.getRefType() == AttachmentRefType.MAIL) {
```

`entity.getRefType()` memanggil `AttachmentRefType.fromDbValue(refType)`.
Jika `refType` di database `null` atau nilai tidak dikenali dan `fromDbValue()` melempar exception,
maka `mapRefId()` akan crash saat mapping.

**Perbaikan**: Tambahkan null-check sebelum membandingkan:
```java
AttachmentRefType type = entity.getRefType();
if (type == null) return String.valueOf(entity.getRefId());
if (type == AttachmentRefType.MAIL) { ... }
```

---

### ⚠️ Warning 4 — Unmapped properties pada `toDetailResponse`

**Lokasi**: `toDetailResponse()`, baris 25.

`AttachmentDetailResponse` tidak memiliki `refType` dan `refId`, sehingga
MapStruct mungkin mengeluarkan warning "Unmapped source properties: refType, refId".

**Perbaikan**: Tambahkan explicit `@Mapping(target = "...", ignore = true)` atau
tambahkan `@BeanMapping(ignoreByDefault = false)` dengan `unmappedSourcePolicy = ReportingPolicy.IGNORE`
di level mapper.

---

### ⚠️ Warning 5 — Tidak ada `@Mapping` ignore untuk field entity yang tidak ada di response

Fields entity yang tidak ada di `AttachmentResponse` maupun `AttachmentDetailResponse`:
`systemFilename`, `status`, `recFlag`, `approveDate`, `approveBy`.

MapStruct akan meng-ignore source properties yang tidak punya target secara default,
tapi jika project dikonfigurasi dengan `unmappedSourcePolicy = ERROR`, ini akan gagal compile.

**Perbaikan**: Tambahkan `@BeanMapping(ignoreUnmappedSourceProperties = {...})` atau
konfigurasikan policy di level `@Mapper`.

---

## Rencana Perbaikan

### Langkah 1 — Perbaiki `mapRefId()`: tambah null-guard

```java
@Named("mapRefId")
protected String mapRefId(Attachment entity) {
    if (entity.getRefId() == null) return null;
    AttachmentRefType type = entity.getRefType(); // ← simpan dulu
    if (type == AttachmentRefType.MAIL) {
        return encoder.encode(Mail.class, entity.getRefId());
    }
    return String.valueOf(entity.getRefId());
}
```

### Langkah 2 — Tambahkan `unmappedSourcePolicy` di `@Mapper`

```java
@Mapper(
    componentModel = MappingConstants.ComponentModel.SPRING,
    unmappedSourcePolicy = ReportingPolicy.IGNORE
)
```

### Langkah 3 — Tambahkan ignore untuk source fields yang tidak relevan pada `toDetailResponse`

```java
@Mapping(target = "id", source = "entity", qualifiedByName = "mapAttachmentId")
// refType dan refId tidak ada di AttachmentDetailResponse — MapStruct
// otomatis ignore unmapped *source* jika policy = IGNORE (lihat langkah 2)
public abstract AttachmentDetailResponse toDetailResponse(Attachment entity);
```

### Langkah 4 — Konfirmasi signature `SqidsEncoder.encode()`

Pastikan `encode(Class<?>, long)` menerima `long` primitive atau `Long`.
Jika menerima `Long`, panggilan `.longValue()` tetap valid (auto-unbox).
Jika menerima `long`, casting dari `Integer` via `.longValue()` juga valid.
**Tidak ada perubahan kode diperlukan**, hanya konfirmasi.

### Langkah 5 — (Opsional) Pastikan `AttachmentRefType.fromDbValue()` aman untuk nilai null

```java
public static AttachmentRefType fromDbValue(Integer value) {
    if (value == null) return null; // ← tambahkan guard
    for (AttachmentRefType t : values()) {
        if (t.getDbValue().equals(value)) return t;
    }
    throw new IllegalArgumentException("Unknown AttachmentRefType: " + value);
}
```

---

## File yang Perlu Diubah

| File | Perubahan |
|------|-----------|
| `AttachmentMapper.java` | Tambah `unmappedSourcePolicy`, perbaiki `mapRefId()` |
| `AttachmentRefType.java` | Tambah null-guard di `fromDbValue()` (opsional tapi disarankan) |

---

## Verifikasi

```bash
# Build dan cek tidak ada MapStruct warning/error
./mvnw clean compile -pl mail-service

# Atau dengan Gradle
./gradlew :mail-service:compileJava
```

Pastikan output compile **tidak mengandung** kata "Unmapped" atau "NullPointerException".

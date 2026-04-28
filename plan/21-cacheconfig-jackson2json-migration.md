# Plan Update CacheConfig - Jackson2JsonRedisSerializer Migration

## Executive Summary
Project menggunakan Spring Boot 4.0.4 (Spring Data Redis 3.x) dimana `Jackson2JsonRedisSerializer` sudah deprecated. Migrasi ke `JacksonJsonRedisSerializer` (Jackson 3 based) diperlukan untuk kompatibilitas jangka panjang.

## Problem Statement
- **Issue:** `Jackson2JsonRedisSerializer` deprecated di Spring Data Redis 3.x
- **Root Cause:** Branding "Jackson2" mereferensikan Jackson 2.x, sedangkan Spring Boot 4 menggunakan Jackson 3
- **Impact:** Potensi break di future Spring Data Redis releases

## Solution Overview
Migrasi dari `Jackson2JsonRedisSerializer` → `JacksonJsonRedisSerializer` (Jackson 3 based)

## Scope of Changes

### Primary Files
| File | Changes |
|------|---------|
| `CacheConfig.java` | Update import, instantiation, method signatures |
| `CacheSerializationTest.java` | Update import dan instantiation serializer |

### Cache Names Affected
- hrEmployee:v2
- mailFolder:v2  
- tenantConfig:v2
- mailStats:v2
- appwrite-tokens:v2
- publications:v2
- mailThread:v2
- mailTracking:v2
- mailReadStatus:v2
- attachments
- allowedFileTypes

## High-Level Implementation Steps

### 1. Import Updates
**Before:**
```java
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
```
**After:**
```java
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer;
```

### 2. Method Signature Updates
**Before:**
```java
private RedisCacheConfiguration buildConfig(Duration ttl, Jackson2JsonRedisSerializer<?> serializer)
```
**After:**
```java
private RedisCacheConfiguration buildConfig(Duration ttl, JacksonJsonRedisSerializer<?> serializer)
```

### 3. Serializer Instantiation Updates
**Before:**
```java
new Jackson2JsonRedisSerializer<>(objectMapper, Object.class)
```
**After:**
```java
new JacksonJsonRedisSerializer<>(Object.class)
```

### 4. Helper Method Updates
**Before:**
```java
private <T> Jackson2JsonRedisSerializer<List<T>> createListSerializer(ObjectMapper mapper, Class<T> type)
```
**After:**
```java
private <T> JacksonJsonRedisSerializer<List<T>> createListSerializer(ObjectMapper mapper, Class<T> type)
```

### 5. RedisTemplate Configuration
**Before:**
```java
var serializer = new Jackson2JsonRedisSerializer<>(objectMapper, Object.class);
```
**After:**
```java
var serializer = new JacksonJsonRedisSerializer<>(Object.class);
```

## Backward Compatibility Strategy
- **Cache Flush:** Semua cache di Redis akan di-flush karena format serialized berbeda
- **Version Suffix:** Cache names sudah menggunakan suffix `:v2` untuk handle breaking changes
- **No Migration Logic:** Tidak perlu handling khusus untuk data lama

## Verification Steps
1. **Build:** `gradle build` - pastikan compile success
2. **Test:** `gradle test` - pastikan test pass
3. **Manual Test:** Test CRUD operasi yang menggunakan cache
4. **Redis Check:** Verifikasi keys di Redis memiliki format baru

## Estimated Effort
- **Complexity:** Low - perubahan mechanical
- **Files:** 2 file utama
- **Time:** 30-60 menit
- **Risk:** Minimal - tidak ada perubahan logic bisnis

## Post-Migration Checklist
- [ ] Build berhasil
- [ ] Test pass  
- [ ] Manual testing cache operations
- [ ] Redis keys menggunakan format baru
- [ ] Performance impact assessment (optional)

## Notes
- Spring Boot 4.0.4 uses Spring Data Redis 3.x
- JacksonJsonRedisSerializer adalah Jackson 3 based
- Tidak perlu ObjectMapper parameter di constructor baru
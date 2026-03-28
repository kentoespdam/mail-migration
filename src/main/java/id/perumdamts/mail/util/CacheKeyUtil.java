package id.perumdamts.mail.util;

/**
 * Utility konstanta dan helper untuk Redis cache key generation.
 *
 * <p>Semua pola key didefinisikan di sini — tidak ada konstruksi string key
 * secara ad-hoc di service layer.
 *
 * <p>Format key: {@code {cacheName}::{prefix}:{id}}
 * sesuai dengan default Spring Cache key generator.
 */
public final class CacheKeyUtil {

    private CacheKeyUtil() {}

    // ── Key Prefix Constants ──────────────────────────────────────────────────

    /** Prefix untuk key employee di cache hrEmployee. */
    public static final String EMP_PREFIX    = "emp:";

    /** Prefix untuk key user folders di cache mailFolder. */
    public static final String USER_PREFIX   = "user:";

    /** Prefix untuk key tenant config di cache tenantConfig. */
    public static final String TENANT_PREFIX = "tenant:";

    // ── Key Builder Methods ───────────────────────────────────────────────────

    /**
     * Key untuk data employee HR Service.
     * Pola: {@code emp:{employeeId}}
     * Contoh penggunaan: {@code @Cacheable(value = "hrEmployee", key = CacheKeyUtil.EMP_PREFIX + " + #employeeId")}
     */
    public static String empKey(long employeeId) {
        return EMP_PREFIX + employeeId;
    }

    /**
     * Key untuk daftar folder user.
     * Pola: {@code user:{userId}}
     */
    public static String userKey(String userId) {
        return USER_PREFIX + userId;
    }

    /**
     * Key untuk tenant config.
     * Pola: {@code tenant:{tenantCode}}
     */
    public static String tenantKey(String tenantCode) {
        return TENANT_PREFIX + tenantCode;
    }
}

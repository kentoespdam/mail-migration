package id.perumdamts.mail.security;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import id.perumdamts.mail.config.AppWriteProperties;
import id.perumdamts.mail.config.CacheConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;

/**
 * Validator token AppWrite dengan caching Redis.
 *
 * <p>Cache menggunakan {@link RedisTemplate} langsung (bukan {@code @Cacheable})
 * agar TTL per-entry bisa diset dinamis berdasarkan {@code exp} claim dari JWT token.
 *
 * <p>Cache key: karakter pertama token sepanjang {@code props.tokenCacheKeyLength()} (default 20).
 */
@Slf4j
@Component
public class AppWriteTokenValidator {

    private static final String ACCOUNT_ENDPOINT          = "/v1/account";
    private static final String APPWRITE_JWT_HEADER       = "X-Appwrite-JWT";
    private static final String APPWRITE_PROJECT_HEADER   = "X-Appwrite-Project";
    private static final String APPWRITE_FORMAT_HEADER    = "X-Appwrite-Response-Format";
    private static final Duration MIN_CACHE_TTL           = Duration.ofSeconds(10);
    private static final ObjectMapper MAPPER = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private final WebClient appWriteClient;
    private final AppWriteProperties props;
    private final StringRedisTemplate redisTemplate;

    public AppWriteTokenValidator(AppWriteProperties props,
                                  WebClient.Builder webClientBuilder,
                                  StringRedisTemplate redisTemplate) {
        this.props = props;
        this.appWriteClient = webClientBuilder.baseUrl(props.endpoint()).build();
        this.redisTemplate = redisTemplate;
    }

    /**
     * Validasi token ke AppWrite — hasil dicache di Redis agar tidak HTTP call setiap request.
     *
     * <p>TTL cache diambil dari {@code exp} claim JWT token, sehingga cache otomatis
     * expired saat token expired. Jika {@code exp} tidak bisa dibaca, fallback ke
     * {@code tokenCacheTtlMinutes} dari konfigurasi.
     *
     * @param token JWT token dari header Authorization
     * @return {@link CachedUserInfo} jika token valid
     * @throws UnauthorizedException jika token invalid atau expired
     */
    public CachedUserInfo validateToken(String token) {
        String cacheKey = buildCacheKey(token);

        // Cek cache dulu
        String cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            log.debug("Token cache hit for key: {}...", cacheKey);
            try {
                return MAPPER.readValue(cached, CachedUserInfo.class);
            } catch (Exception e) {
                log.warn("Gagal deserialize CachedUserInfo dari Redis: {}", e.getMessage());
            }
        }

        // Cache miss — validasi ke AppWrite
        AppWriteUser user = appWriteClient.get()
                .uri(ACCOUNT_ENDPOINT)
                .header(APPWRITE_PROJECT_HEADER, props.projectId())
                .header(APPWRITE_JWT_HEADER, token)
                .header(APPWRITE_FORMAT_HEADER, props.responseFormat())
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError,
                        r -> Mono.error(new UnauthorizedException("Token invalid or expired")))
                .bodyToMono(AppWriteUser.class)
                .block();
        assert user != null;
        log.debug("Token valid, user ID: {}", user.id());

        CachedUserInfo info = CachedUserInfo.from(user);

        // Simpan ke cache dengan TTL dari exp token
        Duration ttl = extractTtlFromToken(token);
        try {
            redisTemplate.opsForValue().set(cacheKey, MAPPER.writeValueAsString(info), ttl);
            log.debug("Token cached with TTL: {}", ttl);
        } catch (Exception e) {
            log.warn("Gagal serialize CachedUserInfo ke Redis: {}", e.getMessage());
        }

        return info;
    }

    private String buildCacheKey(String token) {
        int keyLen = Math.min(token.length(), props.tokenCacheKeyLength());
        return CacheConfig.CacheNames.APPWRITE_TOKENS + "::" + token.substring(0, keyLen);
    }

    /**
     * Decode JWT payload (tanpa verifikasi signature) untuk ambil claim {@code exp}.
     * Mengembalikan durasi dari sekarang sampai token expired.
     * Fallback ke {@code tokenCacheTtlMinutes} jika gagal.
     */
    @SuppressWarnings("unchecked")
    private Duration extractTtlFromToken(String token) {
        Duration fallback = Duration.ofMinutes(props.tokenCacheTtlMinutes());
        try {
            String[] parts = token.split("\\.");
            if (parts.length < 2) return fallback;

            String payload = new String(Base64.getUrlDecoder().decode(parts[1]));
            Map<String, Object> claims = MAPPER.readValue(payload, Map.class);
            Object expObj = claims.get("exp");
            if (expObj == null) return fallback;

            long expEpoch = ((Number) expObj).longValue();
            Duration ttl = Duration.between(Instant.now(), Instant.ofEpochSecond(expEpoch));

            // Jika token hampir expired, gunakan minimal TTL
            return ttl.compareTo(MIN_CACHE_TTL) > 0 ? ttl : MIN_CACHE_TTL;
        } catch (Exception e) {
            log.warn("Gagal extract exp dari JWT, fallback ke TTL {} menit: {}",
                    props.tokenCacheTtlMinutes(), e.getMessage());
            return fallback;
        }
    }
}

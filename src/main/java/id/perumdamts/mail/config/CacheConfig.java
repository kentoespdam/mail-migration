package id.perumdamts.mail.config;

import tools.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import tools.jackson.databind.jsontype.PolymorphicTypeValidator;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.Map;

/**
 * Konfigurasi Redis Cache.
 *
 * <p>Setiap cache name memiliki TTL yang dikonfigurasi secara eksplisit —
 * tidak ada magic number TTL di service layer.
 *
 * <p>Cache names dan TTL sesuai planning-arch.md §Redis Caching Strategy:
 * <ul>
 *   <li>{@code hrEmployee}    — 60 menit, evict by TTL only</li>
 *   <li>{@code mailFolder}    — 10 menit, evict saat folder CRUD</li>
 *   <li>{@code tenantConfig}  —  6 jam,   evict saat config update</li>
 *   <li>{@code mailStats}     —  5 menit, evict via domain event MailSentEvent</li>
 *   <li>{@code appwrite-tokens} — 5 menit, evict by TTL only</li>
 * </ul>
 *
 * <p>Semua nama cache didefinisikan di {@link CacheNames} — tidak ada magic string.
 */
@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * Konstanta nama cache — dipakai di {@code @Cacheable}, {@code @CacheEvict},
     * dan {@link org.springframework.data.redis.cache.RedisCacheManager}.
     * Tidak boleh ada string literal cache name di luar class ini.
     *
     * <p>Versi (:v2) ditambahkan untuk menghindari ClassCastException akibat
     * perubahan serializer ke type-aware.
     */
    public static final class CacheNames {
        private CacheNames() {}

        public static final String HR_EMPLOYEE     = "hrEmployee:v2";
        public static final String MAIL_FOLDER     = "mailFolder:v2";
        public static final String TENANT_CONFIG   = "tenantConfig:v2";
        public static final String MAIL_STATS      = "mailStats:v2";
        public static final String APPWRITE_TOKENS = "appwrite-tokens:v2";
        public static final String PUBLICATIONS    = "publications:v2";
    }

    /**
     * Konstanta TTL per cache — semua durasi didefinisikan di sini, bukan di service.
     */
    public static final class CacheTtl {
        private CacheTtl() {}

        public static final Duration HR_EMPLOYEE     = Duration.ofMinutes(60);
        public static final Duration MAIL_FOLDER     = Duration.ofMinutes(10);
        public static final Duration TENANT_CONFIG   = Duration.ofHours(6);
        public static final Duration MAIL_STATS      = Duration.ofMinutes(5);
        public static final Duration APPWRITE_TOKENS = Duration.ofMinutes(5);
        public static final Duration PUBLICATIONS    = Duration.ofMinutes(10);
        public static final Duration DEFAULT         = Duration.ofMinutes(30);
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());

        var serializer = buildSerializer();
        template.setValueSerializer(serializer);
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(serializer);

        return template;
    }

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration defaultConfig = buildConfig(CacheTtl.DEFAULT);

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(perCacheConfigs())
                .build();
    }

    /**
     * TTL spesifik per cache name.
     * Semua cache name diambil dari {@link CacheNames} — no magic string.
     */
    private Map<String, RedisCacheConfiguration> perCacheConfigs() {
        return Map.of(
                CacheNames.HR_EMPLOYEE,     buildConfig(CacheTtl.HR_EMPLOYEE),
                CacheNames.MAIL_FOLDER,     buildConfig(CacheTtl.MAIL_FOLDER),
                CacheNames.TENANT_CONFIG,   buildConfig(CacheTtl.TENANT_CONFIG),
                CacheNames.MAIL_STATS,      buildConfig(CacheTtl.MAIL_STATS),
                CacheNames.APPWRITE_TOKENS, buildConfig(CacheTtl.APPWRITE_TOKENS),
                CacheNames.PUBLICATIONS,    buildConfig(CacheTtl.PUBLICATIONS)
        );
    }

    /**
     * Base cache configuration: JSON serialization + null value disallow.
     */
    private RedisCacheConfiguration buildConfig(Duration ttl) {
        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(ttl)
                .disableCachingNullValues()
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(buildSerializer())
                );
    }

    private GenericJacksonJsonRedisSerializer buildSerializer() {
        PolymorphicTypeValidator validator = BasicPolymorphicTypeValidator.builder()
                .allowIfBaseType(Object.class)
                .allowIfSubType("id.perumdamts.mail")
                .allowIfSubType("java.util")
                .allowIfSubType("java.time")
                .allowIfSubType("org.springframework.data.domain")
                .build();

        return GenericJacksonJsonRedisSerializer.builder()
                .enableDefaultTyping(validator)
                .build();
    }
}


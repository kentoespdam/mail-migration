package id.perumdamts.mail.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.module.SimpleModule;
import id.perumdamts.mail.dto.core.attachment.AttachmentDetailResponse;
import id.perumdamts.mail.dto.core.folder.MailFolderResponse;
import id.perumdamts.mail.dto.core.mail.MailSummaryResponse;
import id.perumdamts.mail.dto.core.mail.MailTrackingResponse;
import id.perumdamts.mail.dto.core.mail.RecipientReadStatusResponse;
import id.perumdamts.mail.dto.core.publication.PublicationResponse;
import id.perumdamts.mail.dto.master.allowedFileType.AllowedFileTypeDto;
import id.perumdamts.mail.dto.master.quickMessage.QuickMessageResponse;
import id.perumdamts.mail.integration.hr.EmployeeDto;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.List;
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
@SuppressWarnings("removal")
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
        public static final String MAIL_THREAD     = "mailThread:v2";
        public static final String MAIL_TRACKING   = "mailTracking:v2";
        public static final String MAIL_READ_STATUS = "mailReadStatus:v2";
        public static final String ATTACHMENTS     = "attachments";
        public static final String ALLOWED_FILE_TYPES = "allowedFileTypes";
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
        public static final Duration MAIL_THREAD     = Duration.ofMinutes(30);
        public static final Duration MAIL_TRACKING   = Duration.ofMinutes(15);
        public static final Duration MAIL_READ_STATUS = Duration.ofMinutes(15);
        public static final Duration ATTACHMENTS     = Duration.ofMinutes(30);
        public static final Duration ALLOWED_FILE_TYPES = Duration.ofHours(1);
        public static final Duration DEFAULT         = Duration.ofMinutes(30);
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory, ObjectMapper objectMapper) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());

        // Untuk RedisTemplate generic, kita gunakan JacksonJsonRedisSerializer<Object>
        // Tanpa activateDefaultTyping agar aman dan tidak deprecated.
        var serializer = new JacksonJsonRedisSerializer<>(objectMapper, Object.class);
        template.setValueSerializer(serializer);
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(serializer);

        return template;
    }

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory, ObjectMapper objectMapper) {
        RedisCacheConfiguration defaultConfig = buildConfig(CacheTtl.DEFAULT, new JacksonJsonRedisSerializer<>(objectMapper, Object.class));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(perCacheConfigs(objectMapper))
                .build();
    }

    /**
     * TTL spesifik per cache name.
     * Semua cache name diambil dari {@link CacheNames} — no magic string.
     */
    private Map<String, RedisCacheConfiguration> perCacheConfigs(ObjectMapper mapper) {
        return Map.ofEntries(
                Map.entry(CacheNames.HR_EMPLOYEE, buildConfig(CacheTtl.HR_EMPLOYEE, new JacksonJsonRedisSerializer<>(mapper, EmployeeDto.class))),
                Map.entry(CacheNames.MAIL_FOLDER, buildConfig(CacheTtl.MAIL_FOLDER, createListSerializer(mapper, MailFolderResponse.class))),
                Map.entry(CacheNames.TENANT_CONFIG, buildConfig(CacheTtl.TENANT_CONFIG, createListSerializer(mapper, QuickMessageResponse.class))),
                Map.entry(CacheNames.MAIL_STATS, buildConfig(CacheTtl.MAIL_STATS, new JacksonJsonRedisSerializer<>(mapper, Object.class))),
                Map.entry(CacheNames.APPWRITE_TOKENS, buildConfig(CacheTtl.APPWRITE_TOKENS, new JacksonJsonRedisSerializer<>(mapper, Object.class))),
                Map.entry(CacheNames.PUBLICATIONS, buildConfig(CacheTtl.PUBLICATIONS, createPageSerializer(mapper, PublicationResponse.class))),
                Map.entry(CacheNames.MAIL_THREAD, buildConfig(CacheTtl.MAIL_THREAD, createListSerializer(mapper, MailSummaryResponse.class))),
                Map.entry(CacheNames.MAIL_TRACKING, buildConfig(CacheTtl.MAIL_TRACKING, createListSerializer(mapper, MailTrackingResponse.class))),
                Map.entry(CacheNames.MAIL_READ_STATUS, buildConfig(CacheTtl.MAIL_READ_STATUS, createListSerializer(mapper, RecipientReadStatusResponse.class))),
                Map.entry(CacheNames.ATTACHMENTS, buildConfig(CacheTtl.ATTACHMENTS, new JacksonJsonRedisSerializer<>(mapper, AttachmentDetailResponse.class))),
                Map.entry(CacheNames.ALLOWED_FILE_TYPES, buildConfig(CacheTtl.ALLOWED_FILE_TYPES, createListSerializer(mapper, AllowedFileTypeDto.class)))
        );
    }

    /**
     * Base cache configuration: JSON serialization + null value disallow.
     */
    private RedisCacheConfiguration buildConfig(Duration ttl, JacksonJsonRedisSerializer<?> serializer) {
        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(ttl)
                .disableCachingNullValues()
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(serializer)
                );
    }

    private <T> JacksonJsonRedisSerializer<List<T>> createListSerializer(ObjectMapper mapper, Class<T> type) {
        JavaType javaType = mapper.getTypeFactory().constructCollectionType(List.class, type);
        return new JacksonJsonRedisSerializer<>(mapper, javaType);
    }

    private <T> JacksonJsonRedisSerializer<PageImpl<T>> createPageSerializer(ObjectMapper mapper, Class<T> type) {
        JavaType javaType = mapper.getTypeFactory().constructParametricType(PageImpl.class, type);
        return new JacksonJsonRedisSerializer<>(mapper, javaType);
    }



    // ── Jackson Mixins for Spring Data Page ──────────────────────────────────

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static abstract class PageImplMixin<T> extends PageImpl<T> {
        @JsonCreator(mode = com.fasterxml.jackson.annotation.JsonCreator.Mode.PROPERTIES)
        public PageImplMixin(@JsonProperty("content") List<T> content,
                             @JsonProperty("pageable") Pageable pageable,
                             @JsonProperty("totalElements") long total) {
            super(content, pageable, total);
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static abstract class PageRequestMixin {
        @JsonCreator
        public static PageRequest of(@JsonProperty("pageNumber") int page,
                                   @JsonProperty("pageSize") int size) {
            return PageRequest.of(page, size);
        }

        @com.fasterxml.jackson.annotation.JsonIgnore
        public abstract org.springframework.data.domain.Sort getSort();
    }

    public static class PageJacksonModule extends SimpleModule {
        public PageJacksonModule() {
            setMixInAnnotation(PageImpl.class, PageImplMixin.class);
            setMixInAnnotation(PageRequest.class, PageRequestMixin.class);
            setMixInAnnotation(Pageable.class, PageRequestMixin.class);
            addAbstractTypeMapping(Pageable.class, PageRequest.class);
        }
    }
}


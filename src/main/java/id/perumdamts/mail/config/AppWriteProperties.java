package id.perumdamts.mail.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Konfigurasi AppWrite self-hosted.
 *
 * <p>Semua property dibaca dari prefix {@code appwrite.*} di application.yml.
 * Gunakan record untuk immutability dan constraint validation saat startup.
 *
 * <p>Contoh YAML:
 * <pre>
 * appwrite:
 *   endpoint: http://192.168.230.254:82
 *   project-id: 65cd62cc3385d8434a53
 *   api-key: ${APPWRITE_API_KEY}
 *   response-format: "1.0.0"
 *   token-cache-name: appwrite-tokens
 *   token-cache-key-length: 20
 *   token-cache-ttl-minutes: 5
 * </pre>
 */
@Validated
@ConfigurationProperties(prefix = "appwrite")
public record AppWriteProperties(

        /** Base URL AppWrite self-hosted, contoh: http://192.168.230.254:82 */
        @NotBlank String endpoint,

        /** Project ID AppWrite. */
        @NotBlank String projectId,

        /**
         * API key server-to-server (hanya untuk admin call seperti list users).
         * Untuk validasi user JWT tidak dibutuhkan — cukup X-Appwrite-JWT header.
         */
        String apiKey,

        /**
         * Versi response format AppWrite yang di-request.
         * default: "1.0.0"
         */
        @NotNull String responseFormat,

        /** Nama cache Redis untuk token yang sudah divalidasi. */
        @NotBlank String tokenCacheName,

        /**
         * Panjang prefix token yang dijadikan Redis cache key.
         * Cukup 20 karakter pertama JWT — unik dan tidak menyimpan full token.
         */
        int tokenCacheKeyLength,

        /** TTL cache token dalam menit. */
        int tokenCacheTtlMinutes

) {
    /** Default values via compact constructor. */
    public AppWriteProperties {
        if (responseFormat == null || responseFormat.isBlank()) {
            responseFormat = "1.0.0";
        }
        if (tokenCacheName == null || tokenCacheName.isBlank()) {
            tokenCacheName = "appwrite-tokens";
        }
        if (tokenCacheKeyLength <= 0) {
            tokenCacheKeyLength = 20;
        }
        if (tokenCacheTtlMinutes <= 0) {
            tokenCacheTtlMinutes = 5;
        }
    }
}

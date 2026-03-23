package id.perumdamts.mail.infrastructure.security;

import id.perumdamts.mail.config.AppWriteProperties;
import id.perumdamts.mail.config.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * Validator token AppWrite dengan caching Redis.
 *
 * <p>Dipisahkan dari {@link AppWriteAuthFilter} karena {@code @Cacheable}
 * membutuhkan Spring proxy — tidak bisa self-invoke dalam class yang sama.
 *
 * <p>Cache: {@link CacheConfig.CacheNames#APPWRITE_TOKENS}, TTL 5 menit.
 * Cache key: karakter pertama token sepanjang {@code props.tokenCacheKeyLength()} (default 20).
 */
@Component
public class AppWriteTokenValidator {

    private static final String ACCOUNT_ENDPOINT          = "/v1/account";
    private static final String APPWRITE_JWT_HEADER       = "X-Appwrite-JWT";
    private static final String APPWRITE_PROJECT_HEADER   = "X-Appwrite-Project";
    private static final String APPWRITE_FORMAT_HEADER    = "X-Appwrite-Response-Format";

    private final WebClient appWriteClient;
    private final AppWriteProperties props;

    public AppWriteTokenValidator(AppWriteProperties props, WebClient.Builder webClientBuilder) {
        this.props = props;
        this.appWriteClient = webClientBuilder.baseUrl(props.endpoint()).build();
    }

    /**
     * Validasi token ke AppWrite — hasil dicache di Redis agar tidak HTTP call setiap request.
     *
     * <p>Cache key menggunakan prefix token (bukan full token) untuk efisiensi Redis storage.
     *
     * @param token JWT token dari header Authorization
     * @return {@link AppWriteUser} jika token valid
     * @throws UnauthorizedException jika token invalid atau expired
     */
    @Cacheable(
        cacheNames = CacheConfig.CacheNames.APPWRITE_TOKENS,
        key = "#token.substring(0, @appWriteProperties.tokenCacheKeyLength())"
    )
    public AppWriteUser validateToken(String token) {
        return appWriteClient.get()
                .uri(ACCOUNT_ENDPOINT)
                .header(APPWRITE_PROJECT_HEADER, props.projectId())
                .header(APPWRITE_JWT_HEADER, token)
                .header(APPWRITE_FORMAT_HEADER, props.responseFormat())
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError,
                        r -> Mono.error(new UnauthorizedException("Token invalid or expired")))
                .bodyToMono(AppWriteUser.class)
                .block();
    }
}

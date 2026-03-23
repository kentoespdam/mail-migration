package id.perumdamts.mail.infrastructure.security;

import id.perumdamts.mail.config.AppWriteProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

import static jakarta.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;

/**
 * Filter autentikasi AppWrite JWT.
 *
 * <p>Cara kerja:
 * <ol>
 *   <li>Ekstrak Bearer token dari header {@code Authorization}</li>
 *   <li>Cek Redis cache — jika hit, skip HTTP call ke AppWrite</li>
 *   <li>Jika miss, validasi via {@code GET /v1/account} ke AppWrite</li>
 *   <li>Set {@link MailPrincipal} ke {@link SecurityContextHolder}</li>
 * </ol>
 *
 * <p>Cache key: 20 karakter pertama token JWT (cukup unik, tidak menyimpan full token).
 * Token cache TTL: 5 menit (dikonfigurasi via {@link AppWriteProperties}).
 */
@Component
public class AppWriteAuthFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(AppWriteAuthFilter.class);
    private static final String BEARER_PREFIX = "Bearer ";

    private final AppWriteTokenValidator tokenValidator;

    public AppWriteAuthFilter(AppWriteProperties props,
                              AppWriteTokenValidator tokenValidator) {
        this.tokenValidator = tokenValidator;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        String token = extractBearerToken(request);
        if (token == null) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // Validasi token (dengan Redis cache TTL 5 menit)
            CachedUserInfo cachedUser = tokenValidator.validateToken(token);
            MailPrincipal principal = cachedUser.toMailPrincipal();

            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(auth);

        } catch (Exception ex) {
            log.warn("[AUTH] Token validation failed: {}", ex.getMessage());
            SecurityContextHolder.clearContext();
            response.sendError(SC_UNAUTHORIZED, "Invalid or expired token");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private String extractBearerToken(HttpServletRequest request) {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header != null && header.startsWith(BEARER_PREFIX)) {
            return header.substring(BEARER_PREFIX.length()).trim();
        }
        return null;
    }
}

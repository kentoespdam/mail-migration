package id.perumdamts.mail.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

import static jakarta.servlet.http.HttpServletResponse.SC_FORBIDDEN;

/**
 * Filter untuk menangani header X-Active-Position override.
 *
 * <p>Runner filter setelah {@link AppWriteAuthFilter}:
 * <ol>
 *   <li>Extract header X-Active-Position</li>
 *   <li>Call {@link MailRoleContextResolver} untuk validasi & resolve</li>
 *   <li>Jika posisi tidak milik user → 403 Forbidden</li>
 *   <li>Update {@link MailPrincipal} dengan activePosId yangeresolved</li>
 * </ol>
 *
 * <p>Priority: Header X-Active-Position > JWT claim > default (sudah diisi di token validation)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ActivePositionFilter extends OncePerRequestFilter {

    private static final String X_ACTIVE_POSITION = "X-Active-Position";

    private final MailRoleContextResolver roleResolver;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof MailPrincipal principal)) {
            filterChain.doFilter(request, response);
            return;
        }

        String headerPosStr = request.getHeader(X_ACTIVE_POSITION);
        Long headerPosition = null;
        if (headerPosStr != null && !headerPosStr.isBlank()) {
            try {
                headerPosition = Long.parseLong(headerPosStr.trim());
            } catch (NumberFormatException e) {
                log.warn("[ROLE] Invalid X-Active-Position header: {}", headerPosStr);
                response.sendError(SC_FORBIDDEN, "Invalid position format");
                return;
            }
        }

        // Resolve dengan priority: header > JWT claim (from cached) > default
        Long currentActivePos = principal.activePosId();
        Long resolvedPosId = roleResolver.resolveActivePosition(
                principal.getUsername(),
                currentActivePos, // JWT claim atau default sebelumnya
                headerPosition
        );

        if (headerPosition != null && resolvedPosId == null) {
            log.warn("[ROLE] User {} mencoba posisi tidak authorized: {}",
                    principal.getUsername(), headerPosition);
            response.sendError(SC_FORBIDDEN, "Position not owned by user");
            return;
        }

        if (!resolvedPosId.equals(currentActivePos)) {
            log.info("[ROLE] User {} switched to position: {} (was: {})",
                    principal.getUsername(), resolvedPosId, currentActivePos);
        }

        // Create updated principal with resolved activePosId
        MailPrincipal updatedPrincipal = MailPrincipal.withActivePosition(
                principal,
                resolvedPosId
        );

        var updatedAuth = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                updatedPrincipal,
                null,
                updatedPrincipal.getAuthorities()
        );
        SecurityContextHolder.getContext().setAuthentication(updatedAuth);

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/actuator") || path.startsWith("/v3/api");
    }
}
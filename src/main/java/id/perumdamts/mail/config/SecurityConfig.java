package id.perumdamts.mail.config;

import id.perumdamts.mail.infrastructure.security.AppWriteAuthFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Konfigurasi Spring Security.
 *
 * <ul>
 *   <li>Stateless — tidak ada session (JWT-based via AppWrite).</li>
 *   <li>{@link AppWriteAuthFilter} dijalankan sebelum {@code UsernamePasswordAuthenticationFilter}.</li>
 *   <li>{@code @EnableMethodSecurity} mengaktifkan {@code @PreAuthorize} di service layer.</li>
 * </ul>
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final AppWriteAuthFilter appWriteAuthFilter;

    public SecurityConfig(AppWriteAuthFilter appWriteAuthFilter) {
        this.appWriteAuthFilter = appWriteAuthFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Stateless — tidak ada session
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // Disable CSRF — REST API stateless, tidak perlu CSRF protection
            .csrf(csrf -> csrf.disable())

            // Disable form login dan HTTP basic — pakai JWT
            .formLogin(form -> form.disable())
            .httpBasic(basic -> basic.disable())

            // Aturan akses
            .authorizeHttpRequests(auth -> auth
                // Public endpoints
                .requestMatchers(HttpMethod.GET, "/actuator/health").permitAll()
                .requestMatchers(HttpMethod.GET, "/actuator/info").permitAll()

                // Semua API endpoint membutuhkan autentikasi
                .requestMatchers("/api/**").authenticated()

                // Swagger / OpenAPI (jika ditambahkan nanti)
                .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()

                // Default: deny all lainnya
                .anyRequest().denyAll()
            )

            // Tambahkan AppWrite filter sebelum default authentication filter
            .addFilterBefore(appWriteAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}

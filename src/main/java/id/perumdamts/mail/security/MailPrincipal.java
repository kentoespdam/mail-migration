package id.perumdamts.mail.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * Principal yang di-set ke
 * {@link org.springframework.security.core.context.SecurityContext}
 * setelah token AppWrite berhasil divalidasi.
 *
 * <p>
 * Implementasi {@link UserDetails} agar kompatibel dengan Spring Security
 * dan {@code @PreAuthorize} expressions.
 *
 * <p>
 * Gunakan {@link #from(AppWriteUser)} factory method — bukan constructor
 * langsung.
 *
 * @param userId      AppWrite user ID (== pegawaiId di HR Service)
 * @param name        Nama lengkap
 * @param email       Email
 * @param authorities Granted authorities dari prefs.roles
 */
public record MailPrincipal(
        String userId,
        String name,
        String email,
        List<SimpleGrantedAuthority> authorities) implements UserDetails {

    /**
     * Factory method — konversi {@link AppWriteUser} ke {@link MailPrincipal}.
     * Roles dari {@code prefs.roles} dikonversi ke {@code ROLE_*} format.
     */
    public static MailPrincipal from(AppWriteUser user) {
        List<SimpleGrantedAuthority> grantedAuthorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .toList();
        return new MailPrincipal(user.id(), user.name(), user.email(), grantedAuthorities);
    }

    /**
     * Factory method — konversi {@link CachedUserInfo} ke {@link MailPrincipal}.
     * Dipakai saat data diambil dari Redis cache.
     */
    public static MailPrincipal fromCachedInfo(CachedUserInfo info) {
        List<SimpleGrantedAuthority> grantedAuthorities = info.roles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .toList();
        return new MailPrincipal(info.userId(), info.name(), info.email(), grantedAuthorities);
    }

    // ── UserDetails ───────────────────────────────────────────────────────────

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return null; // Tidak dipakai — auth via AppWrite JWT
    }

    @Override
    public String getUsername() {
        return userId; // userId dipakai sebagai username (== pegawaiId)
    }

    public Long userIdLong() {
        return Long.parseLong(userId);
    }

}

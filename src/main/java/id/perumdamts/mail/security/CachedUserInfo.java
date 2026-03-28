package id.perumdamts.mail.security;

import java.io.Serializable;
import java.util.List;

/**
 * DTO sederhana untuk cache di Redis — hanya data primitif/String/List.
 *
 * <p>Dipakai sebagai pengganti {@link AppWriteUser} atau {@link MailPrincipal}
 * yang mengandung class Spring Security ({@code SimpleGrantedAuthority})
 * yang bermasalah saat deserialisasi dari Redis cache.
 */
public record CachedUserInfo(
        String userId,
        String name,
        String email,
        List<String> roles
) implements Serializable {

    public static CachedUserInfo from(AppWriteUser user) {
        return new CachedUserInfo(
                user.id(),
                user.name(),
                user.email(),
                user.getRoles()
        );
    }

    public MailPrincipal toMailPrincipal() {
        return MailPrincipal.fromCachedInfo(this);
    }
}

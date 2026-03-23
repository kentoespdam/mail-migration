package id.perumdamts.mail.infrastructure.security;

import java.util.List;

/**
 * Preferences AppWrite user — tempat roles disimpan.
 *
 * <p>Role contoh: {@code ["USER", "ADMIN", "SYSTEM"]}.
 * Lihat {@link AppWriteRole} untuk konstanta role yang valid.
 */
public record AppWritePrefs(List<String> roles) {}

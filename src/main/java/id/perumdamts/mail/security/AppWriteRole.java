package id.perumdamts.mail.security;

import lombok.Getter;

/**
 * Konstanta role AppWrite — digunakan di {@link AppWriteUser}, {@link MailPrincipal},
 * dan {@code @PreAuthorize} expressions.
 *
 * <p>Tidak ada magic string role di kode — semua referensi role pakai enum ini.
 */
@Getter
public enum AppWriteRole {

    USER("USER"),
    ADMIN("ADMIN"),
    SYSTEM("SYSTEM");

    /**
     * -- GETTER --
     * Nilai string yang di-store di AppWrite prefs.roles.
     */
    private final String value;

    AppWriteRole(String value) {
        this.value = value;
    }

    /** Format yang dipakai Spring Security: {@code ROLE_USER}, {@code ROLE_ADMIN}. */
    public String toGrantedAuthority() {
        return "ROLE_" + value;
    }
}

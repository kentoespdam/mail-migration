package id.perumdamts.mail.infrastructure.security;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Response body dari AppWrite GET /v1/account.
 *
 * <p>PERHATIAN: roles ada di {@code prefs.roles}, BUKAN di field {@code labels}.
 * Ini adalah keputusan AppWrite v1.3.4 self-hosted — jangan diubah.
 *
 * <p>Field {@code $id} dari JSON dijadikan {@code id} via {@code @JsonProperty}.
 */
public record AppWriteUser(

        /** User ID AppWrite — sama dengan pegawaiId di HR Service. */
        @JsonProperty("$id") String id,

        String name,
        String email,
        String phone,
        Boolean status,
        Boolean emailVerification,
        AppWritePrefs prefs

) {
    /** Convenience method — null-safe role accessor. */
    public List<String> getRoles() {
        return prefs != null ? prefs.roles() : List.of();
    }

    /** true jika user memiliki role ADMIN. */
    public boolean isAdmin() {
        return getRoles().contains(AppWriteRole.ADMIN.getValue());
    }

    /** true jika user memiliki role SYSTEM. */
    public boolean isSystem() {
        return getRoles().contains(AppWriteRole.SYSTEM.getValue());
    }
}

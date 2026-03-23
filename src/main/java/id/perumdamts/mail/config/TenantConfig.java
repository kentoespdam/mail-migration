package id.perumdamts.mail.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/*
 * Konfigurasi tenant single-instance.
 *
 * <p>Menggantikan pola {@code CLIENT_CODE if-else} di PHP legacy.
 * Setiap deployment memiliki satu tenant — nilai dikonfigurasi via YAML,
 * bukan hardcode di kode.
 *
 * <p>Contoh YAML:
 * <pre>
 * app:
 *   tenant:
 *     code: PERUMDAM_MTS
 *     display-name: "PDAM Musi Timur Selatan"
 *     office-code: MTS
 *     mail-number-format-ref: mail_number_format_mts
 *     archive-number-format-ref: ma_number_format_mts
 *     inbox-sort-ascending: false
 *     default-mail-type-id: 1
 *     default-mail-category-id: 1
 * </pre>
 */
@Validated
@ConfigurationProperties(prefix = "app.tenant")
public record TenantConfig(

        /* Kode unik tenant, contoh: PERUMDAM_MTS, BMS, SMD. */
        @NotBlank String code,

        /* Nama tampilan instansi, contoh: "PDAM Musi Timur Selatan". */
        @NotBlank String displayName,

        /*
         * Kode kantor pendek, contoh: MTS.
         * Dipakai sebagai prefix nomor surat.
         */
        @NotBlank String officeCode,

        /*
         * Referensi kode format nomor surat dari tabel sys_reference.
         * contoh: mail_number_format_mts
         */
        String mailNumberFormatRef,

        /*
         * Referensi kode format nomor arsip dari tabel sys_reference.
         * contoh: ma_number_format_mts
         */
        String archiveNumberFormatRef,

        /*
         * Urutan tampilan inbox: true = ascending (lama dulu), false = descending (terbaru dulu).
         */
        boolean inboxSortAscending,

        /* ID mail type default saat membuat surat baru. */
        @Positive int defaultMailTypeId,

        /* ID mail category default saat membuat surat baru. */
        @Positive int defaultMailCategoryId

) {}

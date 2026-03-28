package id.perumdamts.mail.dto.core.mail;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

public record MailCreateRequest(
        @NotBlank String subject,
        String content,
        String note,
        Integer mailTypeId,
        Integer mailCategoryId,
        LocalDate mailDate,
        LocalDate maxResponseDate,
        Integer rootMailId,
        Integer parentMailId,
        String noSuratMasuk,
        String asalSuratMasuk,
        String tglSuratMasuk,
        String tujuanSuratKeluar,
        String penerimaSuratKeluar
) {}

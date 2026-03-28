package id.perumdamts.mail.dto.core.mail;

import java.time.LocalDate;

public record MailUpdateRequest(
        String subject,
        String content,
        String note,
        Integer mailTypeId,
        Integer mailCategoryId,
        LocalDate mailDate,
        LocalDate maxResponseDate,
        String noSuratMasuk,
        String asalSuratMasuk,
        String tglSuratMasuk,
        String tujuanSuratKeluar,
        String penerimaSuratKeluar
) {}

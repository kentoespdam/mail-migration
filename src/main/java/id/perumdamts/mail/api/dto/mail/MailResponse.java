package id.perumdamts.mail.api.dto.mail;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record MailResponse(
        Integer id,
        String mailNumber,
        LocalDate mailDate,
        Integer mailTypeId,
        String mailTypeName,
        Integer mailCategoryId,
        String mailCategoryName,
        String subject,
        String content,
        String note,
        LocalDate maxResponseDate,
        Integer status,
        Integer rootMailId,
        Integer parentMailId,
        Integer attachmentQty,
        String toStr,
        LocalDateTime createdDate,
        LocalDateTime updatedDate,
        Integer createdBy,
        String createdByName,
        String noSuratMasuk,
        String asalSuratMasuk,
        String tglSuratMasuk,
        String tujuanSuratKeluar,
        String penerimaSuratKeluar
) {}

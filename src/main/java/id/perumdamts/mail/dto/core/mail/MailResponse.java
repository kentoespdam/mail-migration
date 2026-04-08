package id.perumdamts.mail.dto.core.mail;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record MailResponse(
                String id,
                String mailNumber,
                LocalDate mailDate,
                String mailTypeId,
                String mailTypeName,
                String mailCategoryId,
                String mailCategoryName,
                String subject,
                String content,
                String note,
                LocalDate maxResponseDate,
                Integer status,
                String rootMailId,
                String parentMailId,
                Integer attachmentQty,
                String toStr,
                LocalDateTime createdDate,
                LocalDateTime updatedDate,
                String createdBy,
                String createdByName,
                String noSuratMasuk,
                String asalSuratMasuk,
                String tglSuratMasuk,
                String tujuanSuratKeluar,
                String penerimaSuratKeluar) {
}

package id.perumdamts.mail.dto.core.mail;

import id.perumdamts.mail.dto.id.MailCategoryId;
import id.perumdamts.mail.dto.id.MailId;
import id.perumdamts.mail.dto.id.MailTypeId;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record MailCreateRequest(
                @NotBlank String subject,
                String content,
                String note,
                @NotNull MailTypeId mailTypeId,
                @NotNull MailCategoryId mailCategoryId,
                LocalDate mailDate,
                LocalDate maxResponseDate,
                MailId rootMailId,
                MailId parentMailId,
                String noSuratMasuk,
                String asalSuratMasuk,
                LocalDate tglSuratMasuk,
                String tujuanSuratKeluar,
                String penerimaSuratKeluar) {
}

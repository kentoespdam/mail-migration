package id.perumdamts.mail.dto.core.mail;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;

public record MailCreateRequest(
                @NotBlank String subject,
                String content,
                String note,
                @NotBlank String mailTypeId,
                @NotBlank String mailCategoryId,
                LocalDate mailDate,
                LocalDate maxResponseDate,
                String rootMailId,
                String parentMailId,
                String noSuratMasuk,
                String asalSuratMasuk,
                LocalDate tglSuratMasuk,
                String tujuanSuratKeluar,
                String penerimaSuratKeluar) {
}

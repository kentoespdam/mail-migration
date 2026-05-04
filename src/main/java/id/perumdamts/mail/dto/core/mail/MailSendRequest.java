package id.perumdamts.mail.dto.core.mail;

import id.perumdamts.mail.dto.core.recipient.RecipientBatchRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.time.LocalDate;
import java.util.List;

public record MailSendRequest(
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
                String penerimaSuratKeluar,
                @NotEmpty @Valid List<RecipientBatchRequest> recipients) {
}

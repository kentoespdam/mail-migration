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
        String penerimaSuratKeluar,
        @NotEmpty @Valid List<RecipientBatchRequest> recipients
) {}

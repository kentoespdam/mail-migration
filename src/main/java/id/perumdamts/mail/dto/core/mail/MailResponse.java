package id.perumdamts.mail.dto.core.mail;

import id.perumdamts.mail.dto.common.HasSqid;
import id.perumdamts.mail.dto.core.attachment.AttachmentResponse;
import id.perumdamts.mail.dto.id.MailId;
import id.perumdamts.mail.dto.master.mailCategory.MailCategoryLookup;
import id.perumdamts.mail.dto.master.mailType.MailTypeLookup;

import java.time.LocalDate;
import java.util.List;

public record MailResponse(
        MailId id,
        String mailNumber,
        LocalDate mailDate,
        MailTypeLookup type,
        MailCategoryLookup category,
        String subject,
        String content,
        String note,
        LocalDate maxResponseDate,
        Integer status,
        MailComponentDto.MailThreadInfoDto thread,
        MailComponentDto.MailSummaryInfoDto summary,
        MailComponentDto.MailAuditInfoDto audit,
        String noSuratMasuk,
        String asalSuratMasuk,
        LocalDate tglSuratMasuk,
        String tujuanSuratKeluar,
        String penerimaSuratKeluar,
        List<AttachmentResponse> attachments
) implements HasSqid {
    @Override
    public MailId getId() {
        return id;
    }
}

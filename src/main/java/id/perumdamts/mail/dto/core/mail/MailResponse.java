package id.perumdamts.mail.dto.core.mail;

import id.perumdamts.mail.dto.common.HasSqid;
import id.perumdamts.mail.dto.master.mailCategory.MailCategoryLookup;
import id.perumdamts.mail.dto.master.mailType.MailTypeLookup;
import lombok.Value;

import java.time.LocalDate;

@Value
public class MailResponse implements HasSqid {
        String id;
        String mailNumber;
        LocalDate mailDate;
        MailTypeLookup type;
        MailCategoryLookup category;
        String subject;
        String content;
        String note;
        LocalDate maxResponseDate;
        Integer status;
        MailComponentDto.MailThreadInfoDto thread;
        MailComponentDto.MailSummaryInfoDto summary;
        MailComponentDto.MailAuditInfoDto audit;
        String noSuratMasuk;
        String asalSuratMasuk;
        String tglSuratMasuk;
        String tujuanSuratKeluar;
        String penerimaSuratKeluar;
}

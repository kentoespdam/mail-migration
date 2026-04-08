package id.perumdamts.mail.dto.core.mail;

import id.perumdamts.mail.dto.common.HasSqid;
import id.perumdamts.mail.dto.core.folder.MailFolderLookup;
import id.perumdamts.mail.dto.master.mailCategory.MailCategoryLookup;
import id.perumdamts.mail.dto.master.mailType.MailTypeLookup;
import lombok.Value;

import java.time.LocalDate;

@Value
public class MailSummaryResponse implements HasSqid {
    String id;
    String mailNumber;
    LocalDate mailDate;
    String subject;
    MailComponentDto.MailAuditInfoDto audit;
    MailComponentDto.MailSummaryInfoDto summary;
    Integer readStatus;
    String folderId;
    MailTypeLookup type;
    MailCategoryLookup category;
    String circulationName;
    MailFolderLookup restoreFolder;
    MailComponentDto.MailThreadInfoDto thread;
    Long totalCount;
}

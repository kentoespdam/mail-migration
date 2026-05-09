package id.perumdamts.mail.dto.core.mail;

import id.perumdamts.mail.dto.common.HasSqid;
import id.perumdamts.mail.dto.core.folder.MailFolderLookup;
import id.perumdamts.mail.dto.id.MailFolderId;
import id.perumdamts.mail.dto.id.MailId;
import id.perumdamts.mail.dto.master.mailCategory.MailCategoryLookup;
import id.perumdamts.mail.dto.master.mailType.MailTypeLookup;

import java.time.LocalDate;

public record MailSummaryResponse(
    MailId id,
    String mailNumber,
    LocalDate mailDate,
    String subject,
    MailComponentDto.MailAuditInfoDto audit,
    MailComponentDto.MailSummaryInfoDto summary,
    Integer readStatus,
    MailFolderId folderId,
    MailTypeLookup type,
    MailCategoryLookup category,
    String circulationName,
    MailFolderLookup restoreFolder,
    MailComponentDto.MailThreadInfoDto thread,
    Long totalCount
) implements HasSqid {
    @Override
    public MailId getId() {
        return id;
    }
}

package id.perumdamts.mail.dto.core.mail;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record MailSummaryResponse(
        Integer id,
        String mailNumber,
        LocalDate mailDate,
        String subject,
        String createdByName,
        String toStr,
        Integer readStatus,
        Integer folderId,
        Integer attachmentQty,
        LocalDateTime createdDate,
        String mailTypeName,
        String mailCategoryName,
        String circulationName,
        Integer restoreFolderId,
        String restoreFolderName,
        Integer rootMailId,
        Integer parentMailId,
        Long totalCount
) {}

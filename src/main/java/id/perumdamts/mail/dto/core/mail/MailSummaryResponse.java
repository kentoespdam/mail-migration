package id.perumdamts.mail.dto.core.mail;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record MailSummaryResponse(
        String id,
        String mailNumber,
        LocalDate mailDate,
        String subject,
        String createdByName,
        String toStr,
        Integer readStatus,
        String folderId,
        Integer attachmentQty,
        LocalDateTime createdDate,
        String mailTypeName,
        String mailCategoryName,
        String circulationName,
        String restoreFolderId,
        String restoreFolderName,
        String rootMailId,
        String parentMailId,
        Long totalCount
) {}

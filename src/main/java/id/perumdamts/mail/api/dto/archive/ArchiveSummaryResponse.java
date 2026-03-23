package id.perumdamts.mail.api.dto.archive;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record ArchiveSummaryResponse(
        Long id,
        String archiveNumber,
        LocalDate archiveDate,
        String subject,
        String categoryName,
        Integer status,
        Short year,
        String officeCode,
        Integer attachmentQty,
        LocalDateTime createdDate,
        String createdByName
) {}

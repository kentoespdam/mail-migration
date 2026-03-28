package id.perumdamts.mail.dto.core.archive;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record ArchiveResponse(
        Long id,
        String archiveNumber,
        LocalDate archiveDate,
        Long mailId,
        Integer categoryId,
        String categoryName,
        String subject,
        String content,
        Integer status,
        Short year,
        String officeCode,
        String rack,
        String shelf,
        String box,
        String folderPosition,
        String keywordFlag,
        Integer attachmentQty,
        LocalDateTime createdDate,
        LocalDateTime updatedDate,
        LocalDateTime publishedAt,
        Integer createdBy,
        String createdByName
) {}

package id.perumdamts.mail.api.dto.archive;

import java.time.LocalDate;

public record ArchiveUpdateRequest(
        String subject,
        String content,
        Integer categoryId,
        Long mailId,
        LocalDate archiveDate,
        Short year,
        String rack,
        String shelf,
        String box,
        String folderPosition,
        String keywordFlag
) {}

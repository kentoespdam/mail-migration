package id.perumdamts.mail.dto.core.archive;

import java.time.LocalDate;

public record ArchiveUpdateRequest(
        String subject,
        String content,
        Integer categoryId,
        Long mailId,
        LocalDate archiveDate,
        String rack,
        String shelf,
        String box,
        String keywordFlag
) {}

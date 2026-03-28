package id.perumdamts.mail.dto.core.archive;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

public record ArchiveCreateRequest(
        @NotBlank String subject,
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

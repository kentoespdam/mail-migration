package id.perumdamts.mail.dto.core.archive;

public record ArchiveReportResponse(
        String categoryName,
        Short year,
        long totalArchives,
        long totalDraft,
        long totalArchived
) {}

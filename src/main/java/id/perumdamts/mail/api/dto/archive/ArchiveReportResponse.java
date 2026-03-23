package id.perumdamts.mail.api.dto.archive;

public record ArchiveReportResponse(
        String categoryName,
        Short year,
        long totalArchives,
        long totalDraft,
        long totalArchived
) {}

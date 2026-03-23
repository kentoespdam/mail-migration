package id.perumdamts.mail.api.dto.archive;

import java.time.LocalDate;

public record ArchiveReportRequest(
        Integer categoryId,
        Short year,
        LocalDate startDate,
        LocalDate endDate
) {}

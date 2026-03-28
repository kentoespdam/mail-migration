package id.perumdamts.mail.dto.core.archive;

import java.time.LocalDate;

public record ArchiveReportRequest(
        Integer categoryId,
        Short year,
        LocalDate startDate,
        LocalDate endDate
) {}

package id.perumdamts.mail.dto.core.archive;

import java.time.LocalDate;

public record ArchiveSearchRequest(
        String keyword,
        Integer categoryId,
        Short year,
        LocalDate startDate,
        LocalDate endDate,
        String sortBy,
        String sortDir,
        Integer status,
        int page,
        int size
) {
    public ArchiveSearchRequest {
        if (page < 0) page = 0;
        if (size <= 0 || size > 100) size = 20;
    }

    public int offset() {
        return page * size;
    }
}

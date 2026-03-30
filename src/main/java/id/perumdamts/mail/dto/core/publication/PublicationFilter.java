package id.perumdamts.mail.dto.core.publication;

import id.perumdamts.mail.enums.PublicationStatus;

import java.time.LocalDate;

public record PublicationFilter(
        PublicationStatus status,
        String keyword,
        Integer typeId,
        String sortBy,
        String sortDir,
        LocalDate startDate,
        LocalDate endDate
) {}

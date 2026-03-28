package id.perumdamts.mail.dto.core.publication;

import id.perumdamts.mail.enums.PublicationStatus;

public record PublicationFilter(
        PublicationStatus status,
        String keyword,
        Integer typeId
) {}

package id.perumdamts.mail.api.dto.publication;

import id.perumdamts.mail.domain.enums.PublicationStatus;

public record PublicationFilter(
        PublicationStatus status,
        String keyword,
        Integer typeId
) {}

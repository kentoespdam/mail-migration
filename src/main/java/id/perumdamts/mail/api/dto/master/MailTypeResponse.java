package id.perumdamts.mail.api.dto.master;

import id.perumdamts.mail.domain.enums.RecordStatus;

public record MailTypeResponse(
        Integer id,
        String name,
        RecordStatus status,
        long categoryCount
) {}

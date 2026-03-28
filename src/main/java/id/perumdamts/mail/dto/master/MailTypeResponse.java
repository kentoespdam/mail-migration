package id.perumdamts.mail.dto.master;

import id.perumdamts.mail.enums.RecordStatus;

public record MailTypeResponse(
        Integer id,
        String name,
        RecordStatus status,
        long categoryCount
) {}

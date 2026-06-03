package id.perumdamts.mail.dto.core.mail;

import id.perumdamts.mail.dto.id.MailId;

import java.time.LocalDate;

public record MailLookupResponse(
        MailId id,
        LocalDate mailDate,
        String createdByName,
        String subject,
        String typeName,
        String categoryName,
        String circulationName,
        LocalDate maxResponseDate,
        Boolean isRead
) {
}

package id.perumdamts.mail.dto.core.mail;

import java.time.LocalDate;

public record MailLookupResponse(
        String id,
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

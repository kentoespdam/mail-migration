package id.perumdamts.mail.dto.core.mail;

import id.perumdamts.mail.dto.id.MailId;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record MailTrackingItemResponse(
        MailId id,
        String mailNumber,
        LocalDate mailDate,
        String subject,
        String createdByName,
        LocalDateTime createdDate
) {
}

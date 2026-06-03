package id.perumdamts.mail.dto.core.mail;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record MailTrackingItemResponse(
        String id,
        String mailNumber,
        LocalDate mailDate,
        String subject,
        String createdByName,
        LocalDateTime createdDate
) {
}

package id.perumdamts.mail.dto.core.mail;

import java.time.LocalDateTime;

public record MailTrackingResponse(String recipientId, String empName, String posName, String circulationName,
                                   Boolean isRead, LocalDateTime readDate) {
}

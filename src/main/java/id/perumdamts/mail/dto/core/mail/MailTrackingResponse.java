package id.perumdamts.mail.dto.core.mail;

import lombok.Value;

import java.time.LocalDateTime;

@Value
public class MailTrackingResponse {
    String recipientId;
    String empName;
    String posName;
    String circulationName;
    Boolean isRead;
    LocalDateTime readDate;
}
